package com.lyc.TicketManager_Backend.controller;

import com.lyc.TicketManager_Backend.bean.*;
import com.lyc.TicketManager_Backend.config.SkipSession;
import com.lyc.TicketManager_Backend.config.StatusCode;
import com.lyc.TicketManager_Backend.db.bean.Ticket;
import com.lyc.TicketManager_Backend.db.bean.User;
import com.lyc.TicketManager_Backend.db.bean.UserMovieRating;
import com.lyc.TicketManager_Backend.db.repo.TicketRepository;
import com.lyc.TicketManager_Backend.db.repo.UserMovieRatingRepository;
import com.lyc.TicketManager_Backend.db.repo.UserRepository;
import com.lyc.TicketManager_Backend.util.BindingResultHandler;
import com.lyc.TicketManager_Backend.util.PageUtil;
import com.lyc.TicketManager_Backend.util.SessionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Resource
    private UserRepository userRepository;
    @Resource
    private TicketRepository ticketRepository;
    @Resource
    private UserMovieRatingRepository userMovieRatingRepository;

    @SkipSession
    @PostMapping("/user/register")
    public ResponseMessage<Long> register(@Valid @RequestBody UserRegister userRegister, BindingResult bindingResult) throws RequestException, NoSuchAlgorithmException {
        BindingResultHandler.checkRequest(bindingResult);
        String passwordHash = sha256(userRegister.getPassword());
        if (userRepository.existsByUsername(userRegister.getUsername())) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "用户名已存在");
        }

        try {
            return ResponseMessage.success(userRepository.save(new User(0, userRegister.getUsername(), userRegister.getNickname(), passwordHash)).getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestException(StatusCode.INNER_ERROR, "注册失败：" + e.getMessage());
        }
    }

    @SkipSession
    @GetMapping("/user/check_username")
    public ResponseMessage<Boolean> checkUsername(@RequestParam(value = "username") String username) throws RequestException {
        if (username == null || (username = username.trim()).length() == 0) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "用户名不能为空");
        }

        if (!Pattern.matches("^[a-zA-Z0-9_]{1,21}$", username)) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "用户名由1~21位大小写字母、下划线、数字组成");
        }

        try {
            boolean exist = userRepository.existsByUsername(username);
            if (exist) {
                return new ResponseMessage<>(StatusCode.SUCCESS, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestException(StatusCode.INNER_ERROR, "查询失败");
        }

        return new ResponseMessage<>(StatusCode.SUCCESS, false);
    }

    @SkipSession
    @PostMapping("/user/login")
    public ResponseMessage login(@Valid @RequestBody LoginReq loginReq, BindingResult bindingResult, HttpSession session) throws RequestException, NoSuchAlgorithmException {
        BindingResultHandler.checkRequest(bindingResult);
        if (!userRepository.existsByUsername(loginReq.getUsername())) {
            throw new RequestException(StatusCode.NOT_FOUND, "用户不存在");
        }

        Optional<User> userOptional = userRepository.findByUsernameAndPasswordHash(loginReq.getUsername(), sha256(loginReq.getPassword()));
        if (!userOptional.isPresent()) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "用户名或密码错误");
        }

        User user = userOptional.get();
        session.setAttribute("user", user);
        System.out.println("put " + session.getId() + " " + user);
        return ResponseMessage.success(new UserInfo(user.getId(), user.getUsername(), user.getNickname()));
    }

    @PostMapping("/user/logout")
    public ResponseMessage logout(HttpSession session) throws RequestException {
        SessionUtil.checkLogin(session, false);
        SessionUtil.logout(session);

        return ResponseMessage.success(null);
    }

    @PostMapping("/user/alter_info")
    public ResponseMessage alterInfo(@RequestBody @Valid UserInfo userInfo, BindingResult bindingResult, HttpSession session) throws RequestException {
        BindingResultHandler.checkRequest(bindingResult);
        User user = SessionUtil.checkLogin(session, false);
        try {
            user.setNickname(userInfo.getNickname());
            user = userRepository.save(user);
            session.setAttribute("user", user);
            return ResponseMessage.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestException(StatusCode.INNER_ERROR, "修改信息失败");
        }
    }

    @PostMapping("/user/alter_password")
    public ResponseMessage alterPassword(@RequestBody @Valid UserPassword userPassword, BindingResult bindingResult, HttpSession session) throws RequestException {
        BindingResultHandler.checkRequest(bindingResult);
        User user = SessionUtil.checkLogin(session, false);
        try {
            user.setPasswordHash(sha256(userPassword.getPassword()));
            user = userRepository.save(user);
            session.setAttribute("user", user);
            return ResponseMessage.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestException(StatusCode.INNER_ERROR, "修改密码失败");
        }
    }

    @GetMapping("/user/ratings")
    public ResponseMessage<PageContent<UserMovieRating>> getAllMyRatings(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            HttpSession session
    ) {
        User user = SessionUtil.checkLogin(session, false);
        Pageable pageable = PageUtil.getPageable(page, size, "rateTime", false);
        return ResponseMessage.success(
                PageContent.of(userMovieRatingRepository.findAllByUser(user, pageable))
        );
    }

    @GetMapping("/user/ticket/all")
    public ResponseMessage<PageContent<TicketResponse>> getAllMyTickets(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            HttpSession session
    ) {
        User user = SessionUtil.checkLogin(session, false);
        Pageable pageable = PageUtil.getPageable(page, size, "createTime", false);
        return ResponseMessage.success(
                PageContent.of(mapToTicketResponsePage(ticketRepository.findAllByUser(user, pageable)))
        );
    }

    @GetMapping("/user/ticket/get")
    public ResponseMessage getMyTickets(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "refund", required = false) Boolean refund,
            HttpSession session
    ) {
        if (refund == null) refund = false;
        User user = SessionUtil.checkLogin(session, false);
        Pageable pageable;
        if (!refund) {
            pageable = PageUtil.getPageable(page, size, "createTime", false);
        } else {
            pageable = PageUtil.getPageable(page, size, "refundTime", false);
        }
        return ResponseMessage.success(
                PageContent.of(mapToTicketResponsePage(ticketRepository.findAllByUserAndRefund(user, refund, pageable)))
        );
    }


    private String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(input.getBytes());
        return new BigInteger(messageDigest.digest()).toString(16).toUpperCase();
    }

    private Page<TicketResponse> mapToTicketResponsePage(Page<Ticket> ticketPage) {
        List<TicketResponse> ticketResponses = ticketPage.get().map(TicketResponse::fromTicket).collect(Collectors.toList());
        return new PageImpl<>(
                ticketResponses,
                ticketPage.getPageable(),
                ticketPage.getTotalElements()
        );
    }
}
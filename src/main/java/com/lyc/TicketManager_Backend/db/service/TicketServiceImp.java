package com.lyc.TicketManager_Backend.db.service;

import com.lyc.TicketManager_Backend.bean.PayMethod;
import com.lyc.TicketManager_Backend.bean.RequestException;
import com.lyc.TicketManager_Backend.config.StatusCode;
import com.lyc.TicketManager_Backend.db.bean.MovieShow;
import com.lyc.TicketManager_Backend.db.bean.Seat;
import com.lyc.TicketManager_Backend.db.bean.Ticket;
import com.lyc.TicketManager_Backend.db.bean.User;
import com.lyc.TicketManager_Backend.db.repo.MovieShowRepository;
import com.lyc.TicketManager_Backend.db.repo.SeatRepository;
import com.lyc.TicketManager_Backend.db.repo.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service("ticket_service")
public class TicketServiceImp implements TicketService {

    @Resource
    private MovieShowRepository movieShowRepository;
    @Resource
    private SeatRepository seatRepository;
    @Resource
    private TicketRepository ticketRepository;

    @Override
    @Transactional
    public Ticket purchaseTicket(User user, long movieShowId, Set<Integer> seatLocations, int payMethodId) {
        PayMethod[] payMethods = PayMethod.values();
        if (payMethodId >= payMethods.length || payMethodId < 0) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "未知支付方式：" + payMethodId);
        }
        PayMethod payMethod = payMethods[payMethodId];
        MovieShow movieShow = checkMovieShowOperable(movieShowId);
        Set<Seat> seats = new HashSet<>();
        for (Integer seatLocation : seatLocations) {
            if (seatLocation == null) {
                throw new RequestException(StatusCode.REQUEST_ERROR, "请求参数不合法");
            }
            int x = seatLocation >> 16;
            int y = seatLocation & 0xffff;
            Optional<Seat> seatOptional = seatRepository.findSeatByMovieShowAndXAndY(movieShow, x, y);
            if (!seatOptional.isPresent()) {
                throw new RequestException(StatusCode.NOT_FOUND, "座位(" + x + ", " + y + ")" + "不存在");
            }
            Seat seat = seatOptional.get();
            if (seat.getUserId() == null) {
                int count = seatRepository.lockSeat(user.getId(), seat.getId());
                if (count == 1) {
                    seats.add(seat);
                    // success
                    continue;
                }
            } else if (seat.getUserId() == user.getId()) {
                throw new RequestException(StatusCode.ORDER_LOCK_FAILED, "座位(" + x + ", " + y + ")已经买过了");
            }
            throw new RequestException(StatusCode.ORDER_LOCK_FAILED, "座位(" + x + ", " + y + ")似乎被其他人抢走了>_<");
        }
        return ticketRepository.save(Ticket.generateTicket(user, seats, movieShow, payMethod, movieShow.getPrice()));
    }

    @Override
    @Transactional
    public void returnTicket(User user, String ticketId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (!ticketOptional.isPresent()) {
            throw new RequestException(StatusCode.NOT_FOUND, "订单" + ticketId + "不存在");
        }
        Ticket ticket = ticketOptional.get();
        if (ticket.getUser().getId() != user.getId()) {
            throw new RequestException(StatusCode.FORBIDDEN, "订单" + ticketId + "不属于请求用户");
        }
        checkMovieShowOperable(ticket.getMovieShow().getId());

        int count = ticketRepository.refund(new Timestamp(System.currentTimeMillis()), ticketId);
        if (count != 1) {
            throw new RequestException(StatusCode.FORBIDDEN, "订单不能重复取消");
        }
        for (Seat seat : ticket.getSeats()) {
            seatRepository.returnSeat(seat.getId(), user.getId());
        }
    }

    private MovieShow checkMovieShowOperable(long movieShowId) {
        Optional<MovieShow> movieShowOptional = movieShowRepository.findById(movieShowId);
        if (!movieShowOptional.isPresent()) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "电影放映场次不存在: " + movieShowId);
        }

        MovieShow movieShow = movieShowOptional.get();
        Calendar now = Calendar.getInstance();
        Calendar ddl = Calendar.getInstance();
        ddl.setTimeInMillis(movieShow.getStartTime().getTime());
        ddl.add(Calendar.MINUTE, -10);
        if (now.after(ddl)) {
            throw new RequestException(StatusCode.MOVIE_SHOW_EXPIRED, "放映开始前10分钟不能退/订票");
        }
        return movieShow;
    }
}

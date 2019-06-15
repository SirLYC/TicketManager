package com.lyc.TicketManager_Backend.controller;

import com.lyc.TicketManager_Backend.bean.*;
import com.lyc.TicketManager_Backend.config.SkipSession;
import com.lyc.TicketManager_Backend.config.StatusCode;
import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.UserMovieRatingRepository;
import com.lyc.TicketManager_Backend.db.repo.UserRepository;
import com.lyc.TicketManager_Backend.db.service.KeywordService;
import com.lyc.TicketManager_Backend.db.service.MovieService;
import com.lyc.TicketManager_Backend.util.BindingResultHandler;
import com.lyc.TicketManager_Backend.util.PageUtil;
import com.lyc.TicketManager_Backend.util.SessionUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class MovieController {

    @Resource
    private MovieService movieService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private UserMovieRatingRepository userMovieRatingRepository;
    @Resource
    private KeywordService keywordService;

    @SkipSession
    @GetMapping("/movie/top")
    public ResponseMessage<PageContent<MovieWithRating>> getTopMovies(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        if (page == null) page = 0;
        if (size == null) size = 10;
        return ResponseMessage.success(
                PageContent.of(movieService.search(null, null, null, null, null, null, page, size, false))
        );
    }


    @SkipSession
    @GetMapping("/movie/search")
    public ResponseMessage<PageContent<MovieWithRating>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Boolean blur
    ) {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 10;
        }
        if (blur == null) {
            blur = true;
        }
        ResponseMessage<PageContent<MovieWithRating>> result = ResponseMessage.success(PageContent.of(movieService.search(
                keyword,
                name,
                type,
                actor,
                director,
                country,
                page,
                size,
                blur
        )));
        // success
        // save keywords to database
        Set<String> keywords = new HashSet<>();
        keywords.add(keyword);
        keywords.add(name);
        keywords.add(type);
        keywords.add(actor);
        keywords.add(director);
        keywords.add(country);
        List<Movie> movies = result.getContent().getList().stream().map(MovieWithRating::getMovie).collect(Collectors.toList());
        keywordService.saveKeyword(movies, keywords);
        return result;
    }

    @SkipSession
    @GetMapping("/movie/keywords")
    public ResponseMessage<PageContent<Keyword>> getHotKeywords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseMessage.success(
                PageContent.of(keywordService.getKeywords(page, size))
        );
    }


    @SkipSession
    @GetMapping("/movie/recent")
    public ResponseMessage<PageContent<MovieWithRating>> recentMovies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 0;
        }
        return ResponseMessage.success(PageContent.of(
                movieService.recentMovies(page, size)
        ));
    }

    @SkipSession
    @GetMapping("/movie/info")
    public ResponseMessage<?> movieInfo(@RequestParam("movie_id") long movieId, HttpSession session) throws RequestException {
        User user = SessionUtil.checkLogin(session, true);
        if (user != null) {
            try {
                SessionUtil.checkPassword(userRepository, user);
            } catch (RequestException e) {
                user = null;
            }
        }

        Optional<MovieWithRating> movieWithRatingOptional = movieService.getMovieWithRatingById(movieId);
        if (!movieWithRatingOptional.isPresent()) {
            throw new RequestException(StatusCode.NOT_FOUND, "电影" + movieId + "不存在");
        }

        MovieWithRating movieWithRating = movieWithRatingOptional.get();

        if (user != null) {
            Optional<Double> ratingOptional = userMovieRatingRepository.queryMyRatingFor(movieId, user.getId());
            MovieWithUserRating movieWithUserRating = new MovieWithUserRating(
                    movieWithRating.getMovie(),
                    movieWithRating.getRating(),
                    movieWithRating.getTotalRatings(),
                    ratingOptional.orElse(null)
            );
            return ResponseMessage.success(movieWithUserRating);
        }

        return ResponseMessage.success(movieWithRating);
    }

    @PostMapping("/movie/rate_movie")
    @Transactional
    public ResponseMessage<Long> rateMovie(
            @RequestBody @Valid RateMovieReq req,
            BindingResult bindingResult,
            HttpSession session
    ) throws RequestException {
        User user = SessionUtil.checkLogin(session, false);

        BindingResultHandler.checkRequest(bindingResult);

        Optional<Movie> movieOptional = movieService.getMovie(req.getMovieId());
        if (!movieOptional.isPresent()) {
            throw new RequestException(StatusCode.NOT_FOUND, "电影" + req.getMovieId() + "不存在");
        }

        Optional<UserMovieRating> userMovieRatingOptional
                = userMovieRatingRepository.queryByMovieAndUser(movieOptional.get(), user);
        UserMovieRating userMovieRating =
                userMovieRatingOptional.orElseGet(() -> new UserMovieRating(0, movieOptional.get(), 0, user, new Timestamp(System.currentTimeMillis())));
        userMovieRating.setRating(req.getRating());
        userMovieRating.setRateTime(new Timestamp(System.currentTimeMillis()));

        return ResponseMessage.success(
                userMovieRatingRepository.save(userMovieRating).getId()
        );
    }

    @PostMapping("/movie/cancel_rate")
    public ResponseMessage<Long> cancelRateMovie(
            @RequestBody @Valid MovieIdBody movieIdBody,
            BindingResult bindingResult,
            HttpSession session
    ) throws RequestException {
        User user = SessionUtil.checkLogin(session, false);
        BindingResultHandler.checkRequest(bindingResult);

        long movieId = movieIdBody.getMovieId();
        Optional<Movie> movieOptional = movieService.getMovie(movieId);
        if (!movieOptional.isPresent()) {
            throw new RequestException(StatusCode.NOT_FOUND, "电影" + movieId + "不存在");
        }

        Optional<UserMovieRating> userMovieRatingOptional
                = userMovieRatingRepository.queryByMovieAndUser(movieOptional.get(), user);
        if (!userMovieRatingOptional.isPresent()) {
            throw new RequestException(StatusCode.NOT_FOUND, "用户没有为电影: " + movieId + "评分过");
        }

        userMovieRatingRepository.delete(userMovieRatingOptional.get());
        return ResponseMessage.success(null);
    }

    @SkipSession
    @GetMapping("/movie/actors")
    public ResponseMessage<PageContent<Actor>> getActors(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Boolean asc
    ) {
        return ResponseMessage.success(
                PageContent.of(movieService.getActors(new PageParam(page, size, order, asc)))
        );
    }

    @SkipSession
    @GetMapping("/movie/directors")
    public ResponseMessage<PageContent<Director>> getDirectors(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Boolean asc
    ) {
        return ResponseMessage.success(
                PageContent.of(movieService.getDirectors(new PageParam(page, size, order, asc)))
        );
    }

    @SkipSession
    @GetMapping("/movie/types")
    public ResponseMessage<PageContent<MovieType>> getMovieTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Boolean asc
    ) {
        return ResponseMessage.success(
                PageContent.of(movieService.getMovieTypes(new PageParam(page, size, order, asc)))
        );
    }

    @SkipSession
    @GetMapping("/movie/countries")
    public ResponseMessage<PageContent<String>> getCountries(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseMessage.success(
                PageContent.of(movieService.getCountries(PageUtil.getPageable(page, size)))
        );
    }
}

package com.lyc.TicketManager_Backend.controller;

import com.lyc.TicketManager_Backend.bean.*;
import com.lyc.TicketManager_Backend.config.SkipSession;
import com.lyc.TicketManager_Backend.config.StatusCode;
import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.MovieShowRepository;
import com.lyc.TicketManager_Backend.db.repo.RoomRepository;
import com.lyc.TicketManager_Backend.db.repo.SeatRepository;
import com.lyc.TicketManager_Backend.db.service.MovieService;
import com.lyc.TicketManager_Backend.db.service.TicketService;
import com.lyc.TicketManager_Backend.util.BindingResultHandler;
import com.lyc.TicketManager_Backend.util.DateFormatUtil;
import com.lyc.TicketManager_Backend.util.PageUtil;
import com.lyc.TicketManager_Backend.util.SessionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class MovieShowController {
    @Resource
    private MovieShowRepository movieShowRepository;
    @Resource
    private MovieService movieService;
    @Resource
    private TicketService ticketService;
    @Resource
    private RoomRepository roomRepository;
    @Resource
    private SeatRepository seatRepository;

    @SkipSession
    @GetMapping("/movie/show/rooms")
    private ResponseMessage<List<Room>> getRooms() {
        return ResponseMessage.success(roomRepository.findAll());
    }

    @SkipSession
    @GetMapping("/movie/show/get_by_movie_and_date")
    public ResponseMessage<PageContent<MovieShowResponse>> getMovieShowByDate(
            @RequestParam(value = "movie_id", required = false) Long movieId,
            @RequestParam(value = "start_date", required = false) String startDateString,
            @RequestParam(value = "end_date", required = false) String endDateString,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) throws RequestException {

        Movie movie = null;
        if (movieId != null) {
            Optional<Movie> movieOptional = movieService.getMovie(movieId);
            if (!movieOptional.isPresent()) {
                throw new RequestException(StatusCode.REQUEST_ERROR, "电影id: " + movieId + "不存在");
            }
            movie = movieOptional.get();
        }

        Calendar calendar = Calendar.getInstance();

        Calendar startDate = DateFormatUtil.getCalendar(startDateString);
        Calendar endDate = DateFormatUtil.getCalendar(endDateString);

        if (startDate == null && startDateString != null) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "开始时间: " + startDateString + "格式错误");
        }

        if (endDate == null && endDateString != null) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "结束时间：" + endDateString + "格式错误");
        }

        Timestamp end;
        Timestamp start;

        if (startDate != null) {
            start = new Timestamp(startDate.getTimeInMillis());
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            roundCalendarToDay(calendar);
            start = new Timestamp(calendar.getTimeInMillis());
        }

        if (endDate != null) {
            end = new Timestamp(endDate.getTimeInMillis());
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, 4);
            roundCalendarToDay(calendar);
            end = new Timestamp(calendar.getTimeInMillis());
        }

        return ResponseMessage.success(
                PageContent.of(
                        mapToMovieShowResponse(
                                movie == null ?
                                        movieShowRepository.findAllByStartTimeBetween(
                                                start, end,
                                                PageUtil.getPageable(page, size, "startTime", true)
                                        ) :
                                        movieShowRepository.findAllByStartTimeBetweenAndMovie(
                                                start, end, movie,
                                                PageUtil.getPageable(page, size, "startTime", true)
                                        )
                        )
                )
        );
    }

    @SkipSession
    @GetMapping("/movie/show/recent")
    public ResponseMessage<PageContent<MovieShowResponse>> getRecentMovieShows(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) throws RequestException {
        Timestamp[] timestamps = recentDates();
        return ResponseMessage.success(
                PageContent.of(
                        mapToMovieShowResponse(
                                movieShowRepository.findAllByStartTimeBetween(
                                        timestamps[0], timestamps[1],
                                        PageUtil.getPageable(page, size, "startTime", true)
                                )
                        )
                )
        );
    }

    @GetMapping("/movie/show/seat")
    public ResponseMessage<SeatResponse> getSeatInfo(
            @RequestParam(value = "movie_show_id") Long movieShowId
    ) {
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
            throw new RequestException(StatusCode.MOVIE_SHOW_EXPIRED, "放映开始前10分钟不能查询位置");
        }
        Set<Seat> seats = seatRepository.findAllByUserIdAndMovieShow(null, movieShow);
        Room room = movieShow.getRoom();
        int cols = room.getColumns();
        int rs = room.getRows();
        int half = cols * rs / 2;

        Set<Integer> returnSeats = new HashSet<>();
        boolean taken;
        if (seats.size() > half) {
            // most are empty, we return taken seats
            taken = true;
            for (int i = 1; i <= cols; i++) {
                for (int j = 1; j <= rs; j++) {
                    returnSeats.add(encodeSeatLocation(i, j));
                }
            }
            for (Seat seat : seats) {
                returnSeats.remove(encodeSeatLocation(seat.getX(), seat.getY()));
            }
        } else {
            // return empty seats
            taken = false;
            for (Seat seat : seats) {
                returnSeats.add(encodeSeatLocation(seat.getX(), seat.getY()));
            }
        }

        return ResponseMessage.success(
                new SeatResponse(
                        room.getId(),
                        room.getName(),
                        cols,
                        rs,
                        movieShow.getStartTime(),
                        movieShow.getEndTime(),
                        taken,
                        returnSeats
                )
        );
    }

    private int encodeSeatLocation(int x, int y) {
        int result = 0;
        result |= y & 0x0000ffff;
        result |= x << 16;
        return result;
    }

    private void roundCalendarToDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @SkipSession
    @GetMapping("/movie/show/get_by_room")
    public ResponseMessage<PageContent<MovieShowResponse>> getMovieShowsOfRoom(
            @RequestParam("room_id") Long roomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) throws RequestException {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (!roomOptional.isPresent()) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "影厅id: " + roomId + "不存在");
        }

        Timestamp[] timestamps = recentDates();
        return ResponseMessage.success(
                PageContent.of(
                        mapToMovieShowResponse(
                                movieShowRepository.findAllByStartTimeBetweenAndRoom(
                                        timestamps[0], timestamps[1], roomOptional.get(),
                                        PageUtil.getPageable(page, size, "startTime", true)
                                )
                        )
                )
        );
    }

    @SkipSession
    @GetMapping("/movie/show/get_by_movie")
    public ResponseMessage<PageContent<MovieShowResponse>> getMovieShowsOfMovie(
            @RequestParam("movie_id") Long movieId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) throws RequestException {
        Optional<Movie> movieOptional = movieService.getMovie(movieId);
        if (!movieOptional.isPresent()) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "电影id: " + movieId + "不存在");
        }

        Timestamp[] timestamps = recentDates();
        return ResponseMessage.success(
                PageContent.of(
                        mapToMovieShowResponse(
                                movieShowRepository.findAllByStartTimeBetweenAndMovie(
                                        timestamps[0], timestamps[1], movieOptional.get(),
                                        PageUtil.getPageable(page, size, "startTime", true)
                                )
                        )
                )
        );
    }

    @PostMapping("movie/show/purchase")
    public ResponseMessage<TicketResponse> purchase(
            @Valid @RequestBody PurchaseParam purchaseParam,
            BindingResult bindingResult,
            HttpSession session
    ) {
        BindingResultHandler.checkRequest(bindingResult);
        User user = SessionUtil.checkLogin(session, false);
        Ticket ticket = ticketService.purchaseTicket(user, purchaseParam.getMovieShowId(), purchaseParam.getSeats(), purchaseParam.getPayMethod());
        return ResponseMessage.success(TicketResponse.fromTicket(ticket));
    }

    @PostMapping("/movie/show/return")
    public ResponseMessage<?> returnTicket(
            @RequestBody @Valid TicketIdBody ticketIdBody,
            BindingResult bindingResult,
            HttpSession session
    ) {
        User user = SessionUtil.checkLogin(session, false);
        BindingResultHandler.checkRequest(bindingResult);
        ticketService.returnTicket(user, ticketIdBody.getTicketId());
        return ResponseMessage.success(null);
    }

    private Page<MovieShowResponse> mapToMovieShowResponse(Page<MovieShow> movieShowPage) {
        List<MovieShowResponse> movieShowResponseList = movieShowPage.get().map(movieShow -> new MovieShowResponse(
                movieShow.getId(),
                movieShow.getMovie().getName(),
                movieShow.getMovie().getId(),
                movieShow.getRoom().getId(),
                movieShow.getRoom().getName(),
                movieShow.getPrice(),
                movieShow.getStartTime(),
                movieShow.getEndTime()
        )).collect(Collectors.toList());
        return new PageImpl<>(movieShowResponseList, movieShowPage.getPageable(), movieShowPage.getTotalElements());
    }

    private Timestamp[] recentDates() {
        Timestamp[] timestamps = new Timestamp[2];
        Calendar calendar = Calendar.getInstance();
        timestamps[0] = new Timestamp(calendar.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_MONTH, 4);
        roundCalendarToDay(calendar);
        timestamps[1] = new Timestamp(calendar.getTimeInMillis());
        return timestamps;
    }
}

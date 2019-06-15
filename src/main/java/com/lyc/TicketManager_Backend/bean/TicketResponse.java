package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lyc.TicketManager_Backend.db.bean.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponse {
    private String id;
    @JsonProperty("movie_name")
    private String movieName;
    private String nickname;
    @JsonProperty("room_name")
    private String roomName;
    private Set<Integer> seats;
    @JsonProperty("pay_method")
    private int payMethod;
    private int price;
    @JsonProperty("start_time")
    private Timestamp startTime;
    @JsonProperty("end_time")
    private Timestamp endTime;
    @JsonProperty("create_time")
    private Timestamp createTime;
    private boolean refund;
    @JsonProperty("refund_time")
    private Timestamp refundTime;

    public static TicketResponse fromTicket(Ticket ticket) {
        MovieShow movieShow = ticket.getMovieShow();
        Room room = movieShow.getRoom();
        Movie movie = movieShow.getMovie();
        User user = ticket.getUser();
        Set<Integer> seats = ticket.getSeats().stream()
                .map(value -> (value.getX() << 16) | (value.getY() & 0xffff))
                .collect(Collectors.toSet());
        return new TicketResponse(
                ticket.getId(),
                movie.getName(),
                user.getNickname(),
                room.getName(),
                seats,
                ticket.getPayMethod().ordinal(),
                ticket.getPrice(),
                movieShow.getStartTime(),
                movieShow.getEndTime(),
                ticket.getCreateTime(),
                ticket.isRefund(),
                ticket.getRefundTime()
        );
    }
}

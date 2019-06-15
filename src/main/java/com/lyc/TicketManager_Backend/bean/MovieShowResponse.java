package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieShowResponse {
    private long id;
    @JsonProperty("movie_name")
    private String movieName;
    @JsonProperty("movie_id")
    private long movieId;
    @JsonProperty("room_id")
    private long roomId;
    @JsonProperty("room_name")
    private String roomName;
    private int price;
    @JsonProperty("start_time")
    private Timestamp startTime;
    @JsonProperty("end_time")
    private Timestamp endTime;
}

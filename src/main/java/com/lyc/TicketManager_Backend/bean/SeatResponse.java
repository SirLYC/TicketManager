package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {
    private long roomId;
    @JsonProperty("room_name")
    private String roomName;
    private int columns;
    private int rows;
    @JsonProperty("start_time")
    private Timestamp startTime;
    @JsonProperty("end_time")
    private Timestamp endTime;
    private boolean taken;
    @JsonProperty("seat_info")
    private Set<Integer> seatInfo;
}

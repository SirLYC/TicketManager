package com.lyc.TicketManager_Backend.db.bean;


import lombok.Data;

@Data
public class AggregateRateResult {
    private final double rating;
    private final long totalRatings;
}
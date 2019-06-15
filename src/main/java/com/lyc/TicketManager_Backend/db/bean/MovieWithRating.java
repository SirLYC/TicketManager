package com.lyc.TicketManager_Backend.db.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MovieWithRating {
    private final Movie movie;
    private final double rating;
    @JsonProperty("total_ratings")
    private final long totalRatings;
}

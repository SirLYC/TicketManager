package com.lyc.TicketManager_Backend.db.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MovieWithUserRating {
    private final Movie movie;
    private final double rating;
    @JsonProperty("total_ratings")
    private final long totalRatings;
    @JsonProperty(value = "user_rating", required = true)
    private final Double userRating;
}

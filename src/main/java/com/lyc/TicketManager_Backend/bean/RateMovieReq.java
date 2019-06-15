package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateMovieReq {
    @JsonProperty("movie_id")
    @NotNull
    private Long movieId;
    @NotNull(message = "未给出评分")
    @Max(value = 10, message = "最多10分")
    @Min(value = 0, message = "至少0分")
    private Double rating;
}

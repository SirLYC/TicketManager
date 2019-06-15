package com.lyc.TicketManager_Backend.db.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_movie_rating")
public class UserMovieRating {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(targetEntity = Movie.class, cascade = {
            CascadeType.REFRESH,
            CascadeType.DETACH
    })
    private Movie movie;
    @DecimalMin(value = "0", message = "分值不能为负数")
    @DecimalMax(value = "10", message = "最大分值为5")
    private double rating;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @JoinColumn(name = "rate_time")
    private Timestamp rateTime;
}

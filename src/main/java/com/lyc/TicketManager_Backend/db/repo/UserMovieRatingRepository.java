package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserMovieRatingRepository extends JpaRepository<UserMovieRating, Long>, JpaSpecificationExecutor<UserMovieRating> {
    @Query("select new com.lyc.TicketManager_Backend.db.bean.AggregateRateResult(AVG(rating) , COUNT(rating)) " +
            "from UserMovieRating where movie_id = :movie_id")
    AggregateRateResult queryAggregateRateResult(@Param("movie_id") long movieId);

    @Query("select new com.lyc.TicketManager_Backend.db.bean.MovieWithRating(umr.movie, AVG(umr.rating) , COUNT(umr.rating)) " +
            "from UserMovieRating as umr where movie_id = :movie_id")
    Optional<MovieWithRating> queryMovieWithRating(@Param("movie_id") long movieId);

    @Query(value = "select new com.lyc.TicketManager_Backend.db.bean.MovieWithRating(umr.movie, avg (umr.rating), count (umr.rating)) from UserMovieRating umr group by movie_id order by avg(rating) DESC ")
    Page<MovieWithRating> findTopRatingMovies(Pageable pageable);

    @Query(
            value = "select rating from user_movie_rating where user_id = :user_id and movie_id = :movie_id",
            nativeQuery = true
    )
    Optional<Double> queryMyRatingFor(@Param("movie_id") long movieId, @Param("user_id") long userId);

    Optional<UserMovieRating> queryByMovieAndUser(Movie movie, User user);

    long countByMovie(Movie movie);

    Page<UserMovieRating> findAllByUser(User user, Pageable pageable);
}

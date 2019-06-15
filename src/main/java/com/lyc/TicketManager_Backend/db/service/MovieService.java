package com.lyc.TicketManager_Backend.db.service;


import com.lyc.TicketManager_Backend.bean.PageParam;
import com.lyc.TicketManager_Backend.db.bean.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MovieService {
    Optional<MovieWithRating> getMovieWithRatingById(long id);

    Optional<Movie> getMovie(long id);

    Page<MovieWithRating> getTopMovies(int page, int size);

    Page<MovieWithRating> recentMovies(int page, int size);

    Page<MovieWithRating> search(String keyword, String name, String type, String actor, String director, String country, int page, int size, boolean blur);

    Page<Actor> getActors(PageParam pageParam);

    Page<Director> getDirectors(PageParam pageParam);

    Page<MovieType> getMovieTypes(PageParam pageParam);

    Page<String> getCountries(Pageable pageable);
}

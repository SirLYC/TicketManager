package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    @Query(
            nativeQuery = true,
            value = "select id from movie order by rand() limit :cnt"
    )
    List<Long> randomMovies(@Param("cnt") int count);
}

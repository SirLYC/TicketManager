package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.Movie;
import com.lyc.TicketManager_Backend.db.bean.MovieShow;
import com.lyc.TicketManager_Backend.db.bean.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface MovieShowRepository extends JpaRepository<MovieShow, Long> {
    Page<MovieShow> findAllByStartTimeBetweenAndMovie(Timestamp startTimeStart, Timestamp startTimeEnd, Movie movie, Pageable pageable);

    void deleteAllByStartTimeLessThanEqual(Timestamp startTime);

    Page<MovieShow> findAllByStartTimeBetween(Timestamp startTimeStart, Timestamp startTimeEnd, Pageable pageable);

    boolean existsByStartTimeAfter(Timestamp startTimeStart);

    Page<MovieShow> findAllByStartTimeBetweenAndRoom(Timestamp startTimeStart, Timestamp startTimeEnd, Room room, Pageable pageable);
}

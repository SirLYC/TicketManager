package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.MovieShow;
import com.lyc.TicketManager_Backend.db.bean.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Set<Seat> findByMovieShow(MovieShow movieShow);

    @Transactional
    @Modifying
    @Query("update Seat seat set seat.userId = :user_id where seat.id = :seat_id and (seat.userId = null or not exists (select 1 from com.lyc.TicketManager_Backend.db.bean.User as user0 where user0.id = :user_id))")
    int lockSeat(@Param("user_id") long userId, @Param("seat_id") long seatId);

    @Transactional
    @Modifying
    @Query("update Seat seat set seat.userId = null where seat.id = :id and seat.userId = :user_id")
    int returnSeat(@Param("id") long id, @Param("user_id") long userId);

    Optional<Seat> findSeatByMovieShowAndXAndY(MovieShow movieShow, int x, int y);

    Set<Seat> findAllByUserIdAndMovieShow(Long userId, MovieShow movieShow);
}


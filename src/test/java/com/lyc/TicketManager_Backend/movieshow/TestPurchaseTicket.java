package com.lyc.TicketManager_Backend.movieshow;

import com.lyc.TicketManager_Backend.db.bean.MovieShow;
import com.lyc.TicketManager_Backend.db.bean.Seat;
import com.lyc.TicketManager_Backend.db.bean.Ticket;
import com.lyc.TicketManager_Backend.db.bean.User;
import com.lyc.TicketManager_Backend.db.repo.MovieShowRepository;
import com.lyc.TicketManager_Backend.db.repo.SeatRepository;
import com.lyc.TicketManager_Backend.db.repo.UserRepository;
import com.lyc.TicketManager_Backend.db.service.TicketService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPurchaseTicket {
    @Autowired
    private TicketService ticketService;
    @Autowired
    private MovieShowRepository movieShowRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Test
    public void testPurchaseTicket() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        User user = userRepository.findAll().get(0);
        Page<MovieShow> movieShowPage = movieShowRepository.findAllByStartTimeBetween(new Timestamp(System.currentTimeMillis()), new Timestamp(calendar.getTimeInMillis()), PageRequest.of(0, 1, Sort.Direction.DESC, "startTime"));
        MovieShow movieShow = movieShowPage.getContent().get(0);

        Set<Integer> seats = new HashSet<>();
        seats.add(makeSeat(1, 1));
        seats.add(makeSeat(1, 2));
        Ticket ticket = ticketService.purchaseTicket(user, movieShow.getId(), seats, 0);

        for (Integer seat : seats) {
            int[] ints = extractCoords(seat);
            Optional<Seat> seatOptional = seatRepository.findSeatByMovieShowAndXAndY(movieShow, ints[0], ints[1]);
            Assert.assertTrue(seatOptional.isPresent());
            Assert.assertEquals(seatOptional.get().getUserId(), (Long) user.getId());
        }
        ticketService.returnTicket(user, ticket.getId());
        for (Integer seat : seats) {
            int[] ints = extractCoords(seat);
            Optional<Seat> seatOptional = seatRepository.findSeatByMovieShowAndXAndY(movieShow, ints[0], ints[1]);
            Assert.assertTrue(seatOptional.isPresent());
            Assert.assertNull(seatOptional.get().getUserId());
        }


        seats.clear();
        seats.add(makeSeat(2, 1));
        seats.add(makeSeat(0, 2));

        try {
            ticketService.purchaseTicket(user, movieShow.getId(), seats, 0);
        } catch (Exception e) {
            Optional<Seat> seatOptional = seatRepository.findSeatByMovieShowAndXAndY(movieShow, 2, 1);
            Assert.assertTrue(seatOptional.isPresent());
            Assert.assertNull(seatOptional.get().getUserId());
        }
    }

    private int makeSeat(int x, int y) {
        return (x << 16) | (y & 0xffff);
    }

    private int[] extractCoords(int integer) {
        int[] result = new int[2];
        result[0] = integer >> 16;
        result[1] = integer & 0xffff;
        return result;
    }
}

package com.lyc.TicketManager_Backend.db;


import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.*;
import com.lyc.TicketManager_Backend.db.service.MovieService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MovieShowTest {
    @Autowired
    private MovieShowRepository movieShowRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private MovieService movieService;

    @Transactional
    @Rollback
    @Test
    public void test() {
        List<Room> roomList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            roomList.add(new Room(0, (i + 1) + "号厅", 10, 10));
        }
        roomRepository.saveAll(roomList);
        Assert.assertEquals(10, roomRepository.count());
    }

    @Test
    public void testOrder() {
        try {
            Room room = roomRepository.save(new Room(0, "1号厅", 10, 10));
            Movie movie = movieRepository.save(new Movie(0, "", "", new Date(System.currentTimeMillis()), "",
                    Duration.ZERO));
            MovieShow movieShow = movieShowRepository.save(new MovieShow(0, movie, room, 2000, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis())));

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    seatRepository.save(new Seat(0, i + 1, j + 1, movieShow, null));
                }
            }

            List<User> users = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                users.add(new User(0, "user" + i, "1", "1"));
            }
            userRepository.saveAll(users);
            Optional<Seat> seatOptional = seatRepository.findSeatByMovieShowAndXAndY(movieShow, 2, 2);
            Assert.assertTrue(seatOptional.isPresent());
            Seat seat = seatOptional.get();

            Assert.assertEquals(100, seatRepository.findByMovieShow(movieShow).size());
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger weiredCount = new AtomicInteger(0);
            CountDownLatch countDownLatch = new CountDownLatch(users.size());
            Thread[] threads = new Thread[users.size()];
            for (int i = 0; i < threads.length; i++) {
                User user = users.get(i);
                threads[i] = new Thread(() -> {
                    long order = order(user.getId(), seat);
                    if (order == 1) {
                        successCount.incrementAndGet();
                    } else if (order != 0) {
                        weiredCount.incrementAndGet();
                    }
                    countDownLatch.countDown();
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Assert.assertEquals(1, successCount.get());
            Assert.assertEquals(0, weiredCount.get());
        } finally {
            seatRepository.deleteAll();
            userRepository.deleteAll();
            movieShowRepository.deleteAll();
            roomRepository.deleteAll();
            movieRepository.deleteAll();
        }
    }

    @Test
    @Transactional
    @Rollback
    public void testMovieShowQuery() {
        Room room = roomRepository.save(new Room(0, "1号厅", 10, 10));
        int add = 9000 * 1000;
        long duration = 7200 * 1000;
        Movie movie = movieRepository.save(new Movie(0, "", "", new Date(System.currentTimeMillis()), "",
                Duration.ofSeconds(7200)));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // today 10:00
        System.out.println(calendar);
        Timestamp start = new Timestamp(calendar.getTimeInMillis());
        Timestamp end = new Timestamp(calendar.getTimeInMillis() + duration);
        movieShowRepository.save(new MovieShow(0, movie, room, 2000, start, end, new Timestamp(System.currentTimeMillis())));

        // today 12:30
        calendar.add(Calendar.MILLISECOND, add);
        start = new Timestamp(calendar.getTimeInMillis());
        end = new Timestamp(calendar.getTimeInMillis() + duration);
        movieShowRepository.save(new MovieShow(0, movie, room, 2000, start, end, new Timestamp(System.currentTimeMillis())));

        // today 15:00
        calendar.add(Calendar.MILLISECOND, add);
        start = new Timestamp(calendar.getTimeInMillis());
        end = new Timestamp(calendar.getTimeInMillis() + duration);
        movieShowRepository.save(new MovieShow(0, movie, room, 2000, start, end, new Timestamp(System.currentTimeMillis())));

        // today 17:30
        calendar.add(Calendar.MILLISECOND, add);
        start = new Timestamp(calendar.getTimeInMillis());
        end = new Timestamp(calendar.getTimeInMillis() + duration);
        movieShowRepository.save(new MovieShow(0, movie, room, 2000, start, end, new Timestamp(System.currentTimeMillis())));

        // today 20:00
        calendar.add(Calendar.MILLISECOND, add);
        start = new Timestamp(calendar.getTimeInMillis());
        end = new Timestamp(calendar.getTimeInMillis() + duration);
        movieShowRepository.save(new MovieShow(0, movie, room, 2000, start, end, new Timestamp(System.currentTimeMillis())));

        // today 22:30
        calendar.add(Calendar.MILLISECOND, add);
        start = new Timestamp(calendar.getTimeInMillis());
        end = new Timestamp(calendar.getTimeInMillis() + duration);
        movieShowRepository.save(new MovieShow(0, movie, room, 2000, start, end, new Timestamp(System.currentTimeMillis())));

        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        Timestamp date1 = new Timestamp(calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        Timestamp date2 = new Timestamp(calendar.getTimeInMillis());
        Assert.assertEquals(2, movieShowRepository.findAllByStartTimeBetweenAndMovie(date1, date2, movie, Pageable.unpaged()).getContent().size());

        calendar.set(Calendar.HOUR_OF_DAY, 20);
        movieShowRepository.deleteAllByStartTimeLessThanEqual(new Timestamp(calendar.getTimeInMillis()));
        Assert.assertEquals(1, movieShowRepository.findAll().size());
    }

    private long order(long id, Seat seat) {
        return seatRepository.lockSeat(id, seat.getId());
    }

    @Test
    public void testExitsRecent() {
        List<MovieWithRating> content = movieService.recentMovies(0, 100).getContent();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 4);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Timestamp end = new Timestamp(calendar.getTimeInMillis());
        for (MovieWithRating movieWithRating : content) {
            Assert.assertNotEquals(0, movieShowRepository.findAllByStartTimeBetweenAndMovie(new Timestamp(System.currentTimeMillis()), end, movieWithRating.getMovie(), PageRequest.of(0, 1)));
        }
    }
}

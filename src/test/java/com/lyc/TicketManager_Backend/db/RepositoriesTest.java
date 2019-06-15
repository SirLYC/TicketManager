package com.lyc.TicketManager_Backend.db;

import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.*;
import com.lyc.TicketManager_Backend.db.service.MovieService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RepositoriesTest {

    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
    private static ValidatorFactory validatorFactory;
    private static Validator validator;
    @Autowired
    private MovieShowRepository movieShowRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private DirectorRepository directorRepository;
    @Autowired
    private ActorRepository actorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMovieRatingRepository userMovieRatingRepository;
    @Autowired
    private MovieTypeRepository movieTypeRepository;
    @Autowired
    private MovieService movieService;

    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }

    @Before
    public void clearDb() {
        actorRepository.deleteAll();
        directorRepository.deleteAll();
        userMovieRatingRepository.deleteAll();
        movieShowRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();
        movieTypeRepository.deleteAll();
    }

    @Test
    public void testMovie() {

        long time = System.currentTimeMillis();
        Set<Movie> movies = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            movies.add(movieRepository.save(new Movie(0, "url", "test" + i, new Date(time + i * ONE_DAY), "America", Duration.ZERO)));
        }

        Assert.assertEquals(5, movieRepository.randomMovies(5).size());

        Actor actor = new Actor(0, "A");
        actor.getMovies().addAll(movies);

        actor = actorRepository.save(actor);
        actor = actorRepository.findById(actor.getId()).get();
        Assert.assertEquals(actor.getMovies().size(), 10);
        Assert.assertEquals(movieRepository.findById(movies.iterator().next().getId()).get().getActors().size(), 1);

        actor.setName("B");
        actor.getMovies().remove(actor.getMovies().iterator().next());

        actor = actorRepository.save(actor);

        Assert.assertEquals(actor.getName(), "B");
        Assert.assertEquals(actor.getMovies().size(), 9);

        Movie movie = actor.getMovies().iterator().next();
        Actor newActor = new Actor(0, "CC");
        newActor.getMovies().add(movie);
        actorRepository.save(newActor);
        Optional<Movie> optionalMovie = movieRepository.findById(movie.getId());
        Assert.assertTrue(optionalMovie.isPresent());
        Assert.assertEquals(optionalMovie.get().getActors().size(), 2);
    }

//    @Test
//    public void testMovieShow() {
//        Movie movie = movieRepository.save(new Movie(0, "url", "test", new Date(System.currentTimeMillis()), "America", Duration.ZERO));
//        Assert.assertEquals(movie.getShows().size(), 0);
//        MovieShow movieShow = new MovieShow(0, movie);
//        movieShowRepository.save(movieShow);
//        Assert.assertEquals(movieRepository.findById(movie.getId()).get().getShows().size(), 1);
//        movieShowRepository.delete(movieShow);
//        Assert.assertEquals(movieRepository.findAll().size(), 1);
//    }

    @Test
    public void testUserRating() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(userRepository.save(new User(i, String.valueOf(i), String.valueOf(i), String.valueOf(i))));
        }

        Assert.assertEquals(userRepository.findAll().size(), users.size());

        Movie movie = new Movie(0, "1", "2", new Date(System.currentTimeMillis()), "America", Duration.ZERO);

        movieRepository.save(movie);

        double[] ratings = new double[users.size()];
        Random r = new Random();
        double avg = 0;
        for (int i = 0; i < ratings.length; i++) {
            ratings[i] = r.nextDouble() * 5;
            avg += ratings[i];
        }
        avg /= ratings.length;
        List<UserMovieRating> userMovieRatingList = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            UserMovieRating userMovieRating = new UserMovieRating(0, movie, ratings[i], user, new Timestamp(System.currentTimeMillis()));
            userMovieRatingList.add(userMovieRatingRepository.save(userMovieRating));
        }

        for (User user : userRepository.findAll()) {
            Assert.assertEquals(user.getUserMovieRatings().size(), 1);
        }

        AggregateRateResult aggregateRateResult = userMovieRatingRepository.queryAggregateRateResult(movie.getId());
        Assert.assertEquals(aggregateRateResult.getTotalRatings(), users.size());
        Assert.assertEquals(aggregateRateResult.getRating(), avg, 0.1);

        UserMovieRating userMovieRating = userMovieRatingList.remove(0);
        userMovieRatingRepository.delete(userMovieRating);
        Assert.assertEquals(userMovieRatingRepository.queryAggregateRateResult(movie.getId()).getTotalRatings(), users.size() - 1);

        userMovieRating.setId(0);
        userMovieRating.setRating(-1);
        Set<ConstraintViolation<UserMovieRating>> violations = validator.validate(userMovieRating);
        Assert.assertEquals(violations.size(), 1);
        Assert.assertEquals(violations.iterator().next().getMessage(), "分值不能为负数");

        userMovieRating.setRating(5.1);
        violations = validator.validate(userMovieRating);
        Assert.assertEquals(violations.size(), 1);
        Assert.assertEquals(violations.iterator().next().getMessage(), "最大分值为5");
    }

    @Test
    public void testMovieByRatings() {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            movies.add(new Movie(0, "name" + i, "cover" + i, new Date(System.currentTimeMillis()), "America", Duration.ZERO));
        }
        movies = movieRepository.saveAll(movies);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            users.add(new User(i, "username" + i, "nickname" + i, String.valueOf(i)));
        }
        users = userRepository.saveAll(users);

        Random r = new Random();

        List<UserMovieRating> userMovieRatings = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            userMovieRatings.add(new UserMovieRating(0, movies.get(i % movies.size()),
                    r.nextDouble() * 5, users.get(i % users.size()), new Timestamp(System.currentTimeMillis())));
        }
        userMovieRatingRepository.saveAll(userMovieRatings);


        Pageable pageable = PageRequest.of(0, 15);
        long start = System.nanoTime();
        Page<MovieWithRating> page = userMovieRatingRepository.findTopRatingMovies(pageable);
        Assert.assertEquals(page.toList().size(), 15);
        long end = System.nanoTime();
        System.out.println("cost time: " + (end - start) / 1000000 + "ms");
        List<MovieWithRating> ratingList = page.toList();
        for (int i = 1; i < ratingList.size(); i++) {
            Assert.assertFalse(ratingList.get(i).getRating() > ratingList.get(i - 1).getRating());
        }

        Assert.assertEquals(userMovieRatingRepository.findTopRatingMovies(pageable.next()).toList().size(), 5);


        List<MovieWithRating> list = movieService.getTopMovies(0, 10).toList();
        for (int i = 1; i < list.size(); i++) {
            Assert.assertFalse(list.get(i).getRating() > list.get(i).getRating());
        }
        System.out.println(list);
    }

    @Test
    public void testSearchMovie() {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            movies.add(new Movie(0, "name" + i, "cover" + i, new Date(System.currentTimeMillis()), "America", Duration.ZERO));
        }
        movies = movieRepository.saveAll(movies);
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            users.add(new User(i, "username" + i, "nickname" + i, String.valueOf(i)));
        }
        users = userRepository.saveAll(users);

        Random r = new Random();

        List<UserMovieRating> userMovieRatings = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            userMovieRatings.add(new UserMovieRating(0, movies.get(i % movies.size()),
                    r.nextDouble() * 5, users.get(i % users.size()), new Timestamp(System.currentTimeMillis())));
        }
        userMovieRatingRepository.saveAll(userMovieRatings);
        List<MovieType> movieTypeList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            movieTypeList.add(new MovieType(0, "type" + i));
        }
        movieTypeRepository.saveAll(movieTypeList);

        int type1Cnt = 0;
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            int t1 = i % 10;
            int t2 = (i + 1) % 10;
            if (t1 == 1) type1Cnt++;
            else if (t2 == 1) type1Cnt++;
            movie.getTypes().add(movieTypeList.get(t1));
            movie.getTypes().add(movieTypeList.get(t2));
        }
        movieRepository.saveAll(movies);

        List<MovieWithRating> list = movieService.search("type1", null, null, null, null, null, 0, 10, false).toList();

        Assert.assertEquals(list.toString(), type1Cnt, list.size());

        long start = System.nanoTime();
        List<MovieWithRating> list1 = movieService.search(null, null, null, null, null, null, 0, 10, false).toList();
        long searchSpent = (System.nanoTime() - start) / 1000000;

        for (MovieWithRating movieWithRating : list1) {
            Assert.assertEquals(userMovieRatingRepository.countByMovie(movieWithRating.getMovie()), movieWithRating.getTotalRatings());
        }

        start = System.nanoTime();
        List<MovieWithRating> topMovieList = movieService.getTopMovies(0, 10).toList();
        long getSpent = (System.nanoTime() - start) / 1000000;
        Assert.assertArrayEquals(
                topMovieList.toArray(),
                list1.toArray()
        );

        System.out.println("search spent: " + searchSpent + "ms; get top movies spent " + getSpent + "ms.");
    }

    @Test
    public void getMovieByIdTest() {
        Movie movie = movieRepository.save(new Movie(0, "A", "B", new Date(System.currentTimeMillis()), "C", Duration.ZERO));

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = new User();
            String s = String.valueOf(i);
            user.setUsername(s);
            user.setNickname(s);
            user.setPasswordHash(s);
            users.add(user);
        }
        users = userRepository.saveAll(users);

        double rating = 0;
        Random random = new Random();
        for (User user : users) {
            rating += userMovieRatingRepository.save(new UserMovieRating(0, movie, random.nextDouble() * 10, user, new Timestamp(System.currentTimeMillis()))).getRating();
        }

        rating /= users.size();

        MovieWithRating movieWithRating = movieService.getMovieWithRatingById(movie.getId()).get();

        Assert.assertEquals(movieWithRating.getRating(), rating, 0.01);
    }
}

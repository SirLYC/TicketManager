package com.lyc.TicketManager_Backend.util;

import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FakeDataUtil {
    private static final long YEAR_MILLIS = 1000L * 365 * 24 * 60 * 60;
    private final Random r = new Random();
    private final String[] countries = new String[]{
            "中国", "美国", "日本", "英国", "西班牙", "韩国", "俄罗斯", "法国", "德国", "葡萄牙", "巴西", "印度"
    };
    private final Date[] releaseTimes = new Date[20];
    private final String[] movieNames = new String[]{
            "复仇者联盟",
            "美国队长",
            "钢铁侠",
            "银河护卫队",
            "黑豹",
            "绿巨人",
            "黑寡妇",
            "蜘蛛侠",
            "蚁人",
            "雷神",
            "奇异博士",
            "惊奇队长",
            "鹰眼",
            "冬日战士",
            "X战警",
            "金刚狼",
            "死侍",
            "毒液"
    };
    private final String[] movieTypeNames = new String[]{
            "剧情",
            "冒险",
            "爱情",
            "纪录",
            "恐怖",
            "家庭",
            "科幻",
            "魔幻",
            "伦理",
            "古装",
            "动作"
    };
    private final String[] actorNames = new String[]{
            "Robert John Downey Jr",
            "Chris Evans",
            "Scarlett Ingrid Johansson",
            "Thomas William Hiddleston",
            "Tom Holland",
            "Sebastian Stan",
            "Chris Pratt",
            "Zoe Saldana",
            "Brie Larson",
            "Paul Bettany",
            "Paul Stephen Rudd",
            "Chris Hemsworth",
            "Benedict Timothy Carlton Cumberbatch",
            "Samuel Leroy Jackson",
            "Chadwick Boseman",
            "Jennifer Shrader Lawrence",
            "Hugh Jackman",
            "Patrick Stewart",
            "Ian McKellen",
            "James Andrew McAvoy",
            "Michael Fassbender"
    };
    private final String[] directorNames = new String[]{
            "Joss Whedon",
            "Anthony Russo",
            "Joe Russo",
            "Joe Johnston",
            "Jon Favreau",
            "Jon Watts",
            "James Gunn",
            "Peyton Reed",
            "Scott Derrickson",
            "Kenneth Charles Branagh"
    };
    private final List<String> coverUrls = new ArrayList<>();
    @Resource
    private MovieShowRepository movieShowRepository;
    @Resource
    private MovieRepository movieRepository;
    @Resource
    private DirectorRepository directorRepository;
    @Resource
    private ActorRepository actorRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private UserMovieRatingRepository userMovieRatingRepository;
    @Resource
    private MovieTypeRepository movieTypeRepository;
    @Resource
    private RoomRepository roomRepository;
    @Resource
    private SeatRepository seatRepository;
    private Long[] durations = new Long[]{
            // 1h
            3600L,
            // 1h15min
            4500L,
            // 1h30min
            5400L,
            // 1h45min
            6300L,
            // 2h
            7200L,
            8100L,
            // 2h30min
            9000L,
            // 2h45min
            9900L,
            // 3h
            10800L
    };
    private Map<Integer, Integer> nameCounter = new HashMap<>();
    private Map<Integer, Integer> actorCounter = new HashMap<>();
    private Map<Integer, Integer> directorCounter = new HashMap<>();

    void runFirstTime(String urlFile) throws NoSuchAlgorithmException {
        if (movieRepository.count() != 0) {
            return;
        }
        File file;
        List<String> urls = new ArrayList<>(300);
        if (urlFile != null && (file = new File(urlFile)).exists()) {
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    String s = scanner.nextLine();
                    if (s.startsWith("http")) {
                        urls.add(s);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("cannot find url file: " + urlFile);
        }

        initRandomData(urls);
        clearData();
        fillData();
    }

    private void initRandomData(List<String> urls) {
        coverUrls.addAll(urls);
        long current = System.currentTimeMillis();
        for (int i = 0; i < releaseTimes.length; i++) {
            releaseTimes[i] = new Date(current -= YEAR_MILLIS);
        }
    }

    private void clearData() {
        actorRepository.deleteAll();
        directorRepository.deleteAll();
        userMovieRatingRepository.deleteAll();
        userRepository.deleteAll();
        seatRepository.deleteAll();
        movieShowRepository.deleteAll();
        movieRepository.deleteAll();
        movieTypeRepository.deleteAll();
        roomRepository.deleteAll();
    }

    private void fillData() throws NoSuchAlgorithmException {
        String passwordHash = userPassword();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            users.add(new User(0, "username" + i, "nickname" + i, passwordHash));
        }
        users = userRepository.saveAll(users);
        List<MovieType> movieTypes = movieTypeRepository.saveAll(
                Arrays.stream(movieTypeNames).map(s -> new MovieType(0, s)).collect(Collectors.toList())
        );


        int movieSize = 300;
        int add = movieSize - coverUrls.size();
        for (int i = 0; i < add; i++) {
            coverUrls.add("cover" + i);
        }
        Collections.shuffle(coverUrls, r);
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < movieSize; i++) {
            Movie movie = new Movie(0, getRandomMovieName(), coverUrls.get(i), getReleaseTime(), getCountry(), Duration.ofSeconds(randomDuration()));
            int typeCnt = r.nextInt(4) + 2;
            Set<MovieType> movieTypeSet = movie.getTypes();
            for (int j = 0; j < typeCnt; j++) {
                movieTypeSet.add(getRandomElement(movieTypes));
            }
            movies.add(movie);
        }
        movies = movieRepository.saveAll(movies);

        List<Actor> actors = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Actor actor = new Actor(0, getRandomActorName());
            Set<Movie> actorMovies = actor.getMovies();
            for (int k = -4; k <= 4; k++) {
                actorMovies.add(movies.get((i * 9 + k) / movieSize));
            }
            int rCnt = 9 + r.nextInt(5);
            while (actorMovies.size() < rCnt) {
                actorMovies.add(getRandomElement(movies));
            }
            actors.add(actor);
        }
        actorRepository.saveAll(actors);

        List<Director> directors = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Director director = new Director(0, getRandomDirectorName());
            Set<Movie> directorMovies = director.getMovies();
            directorMovies.add(movies.get(i * 3));
            directorMovies.add(movies.get(i * 3 + 1));
            directorMovies.add(movies.get(i * 3 + 2));
            int cnt = 4 + r.nextInt(5);
            while (directorMovies.size() < cnt) {
                directorMovies.add(getRandomElement(movies));
            }
            directors.add(director);
        }
        directorRepository.saveAll(directors);

        List<UserMovieRating> userMovieRatingList = new ArrayList<>();
        for (User user : users) {
            int cnt = r.nextInt(3) + 10;
            Set<Movie> rateMovies = new HashSet<>();
            while (rateMovies.size() < cnt) {
                rateMovies.add(getRandomElement(movies));
            }
            for (Movie rateMovie : rateMovies) {
                userMovieRatingList.add(new UserMovieRating(0, rateMovie, r.nextDouble() * 10, user, new Timestamp(System.currentTimeMillis())));
            }
        }

        userMovieRatingRepository.saveAll(userMovieRatingList);

        List<Room> roomList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            roomList.add(new Room(0, (i + 1) + "号厅", 7 + i % 4, 7 + i % 4));
        }
        roomList = roomRepository.saveAll(roomList);

        List<Movie> randomMovies = getRandomMovieList(roomList.size() * 4);
        int movieListSize = randomMovies.size();
        Calendar now = Calendar.getInstance();
        roundCalendar(now);
        Calendar nowAdd3Days = Calendar.getInstance();
        nowAdd3Days.setTimeInMillis(now.getTimeInMillis());
        nowAdd3Days.add(Calendar.DAY_OF_YEAR, 3);
        nowAdd3Days.set(Calendar.HOUR_OF_DAY, 0);
        for (int i = 0; i < roomList.size(); i++) {
            int index = i * 4;
            int maxIndex = i * 4 + 3;
            Movie movie = randomMovies.get(index % movieListSize);
            Room room = roomList.get(i);
            int c = room.getColumns();
            int r = room.getRows();
            long seconds = movie.getDuration().getSeconds();
            long millis = seconds * 1000;
            int price = (int) (seconds / 900 * 5) * 100;
            now.setTimeInMillis(System.currentTimeMillis());
            roundCalendar(now);
            while (now.before(nowAdd3Days)) {
                if (now.get(Calendar.HOUR_OF_DAY) < 10) {
                    now.set(Calendar.HOUR_OF_DAY, 10);
                    if (++index < maxIndex) {
                        movie = randomMovies.get(index % movieListSize);
                    }
                }
                createNewShow(now, movie, room, c, r, millis, price);
            }
        }
    }

    private void createNewShow(Calendar now, Movie movie, Room room, int c, int r, long millis, int price) {
        MovieShow movieShow = movieShowRepository.save(new MovieShow(0, movie, room, price,
                new Timestamp(now.getTimeInMillis()),
                new Timestamp(now.getTimeInMillis() + millis),
                new Timestamp(System.currentTimeMillis())));
        addSeatFor(movieShow, c, r);
        now.setTimeInMillis(now.getTimeInMillis() + millis);
        now.add(Calendar.MINUTE, 30);
    }

    void fillMovieAfter2Days() {
        Calendar now = Calendar.getInstance();
        roundCalendar(now);
        now.add(Calendar.DAY_OF_YEAR, 2);
        Calendar nowCopy = Calendar.getInstance();
        nowCopy.setTimeInMillis(now.getTimeInMillis());
        if (movieShowRepository.existsByStartTimeAfter(new Timestamp(now.getTimeInMillis()))) {
            return;
        }

        List<Room> roomList = roomRepository.findAll();
        int total = roomList.size();
        List<Movie> movieList = getRandomMovieList(total);
        int movieListSize = movieList.size();
        for (int i = 0; i < total; i++) {
            Room room = roomList.get(i);
            Movie movie = movieList.get(i % movieListSize);

            int c = room.getColumns();
            int r = room.getRows();
            long seconds = movie.getDuration().getSeconds();
            long millis = seconds * 1000;
            int price = (int) (seconds / 900 * 5) * 100;
            now.setTimeInMillis(nowCopy.getTimeInMillis());

            while (now.get(Calendar.DAY_OF_YEAR) == nowCopy.get(Calendar.DAY_OF_YEAR)) {
                createNewShow(now, movie, room, c, r, millis, price);
            }
        }
    }

    private List<Movie> getRandomMovieList(int total) {
        int get = 0;
        List<Movie> result = new ArrayList<>();
        while (get < total) {
            int toFetch = total - get;
            List<Long> idList = movieRepository.randomMovies(toFetch);
            int size = idList.size();
            for (long id : idList) {
                Optional<Movie> movieOptional = movieRepository.findById(id);
                if (movieOptional.isPresent()) {
                    get++;
                    result.add(movieOptional.get());
                }
            }
            if (size < toFetch) {
                break;
            }
        }
        return result;
    }

    private void addSeatFor(MovieShow movieShow, int c, int r) {
        Set<Seat> seatSet = new HashSet<>();
        for (int x = 1; x <= c; x++) {
            for (int y = 1; y <= r; y++) {
                seatSet.add(new Seat(0, x, y, movieShow, null));
            }
        }
        seatRepository.saveAll(seatSet);
    }

    private void roundCalendar(Calendar now) {
        now.set(Calendar.HOUR_OF_DAY, 10);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
    }

    private Long randomDuration() {
        return durations[r.nextInt(durations.length)];
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(r.nextInt(list.size()));
    }

    private String getCountry() {
        return countries[r.nextInt(countries.length)];
    }

    private String getRandomMovieName() {
        return getRandomData(movieNames, nameCounter);
    }

    private String getRandomData(String[] array, Map<Integer, Integer> counter) {
        int index = r.nextInt(array.length);
        Integer count = counter.get(index);
        if (count == null) count = 0;

        String result;
        if (count == 0) {
            result = array[index];
        } else {
            result = array[index] + count;
        }
        counter.put(index, count + 1);
        return result;
    }

    private String getRandomActorName() {
        return getRandomData(actorNames, actorCounter);
    }

    private String getRandomDirectorName() {
        return getRandomData(directorNames, directorCounter);
    }

    private Date getReleaseTime() {
        return releaseTimes[r.nextInt(releaseTimes.length)];
    }

    private String userPassword() throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update("123456".getBytes());
        return new BigInteger(messageDigest.digest()).toString(16).toUpperCase();
    }
}
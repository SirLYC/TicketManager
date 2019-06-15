package com.lyc.TicketManager_Backend.db.service;

import com.lyc.TicketManager_Backend.bean.PageParam;
import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.*;
import com.lyc.TicketManager_Backend.util.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImp implements MovieService {

    @PersistenceContext
    public EntityManager entityManager;
    @Resource
    private MovieRepository movieRepository;
    @Resource
    private UserMovieRatingRepository userMovieRatingRepository;
    @Resource
    private ActorRepository actorRepository;
    @Resource
    private DirectorRepository directorRepository;
    @Resource
    private MovieTypeRepository movieTypeRepository;

    @Override
    public Optional<MovieWithRating> getMovieWithRatingById(long movieId) {
        return userMovieRatingRepository.queryMovieWithRating(movieId);
    }

    @Override
    public Page<MovieWithRating> getTopMovies(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size);
        return userMovieRatingRepository.findTopRatingMovies(pageable);
    }

    @Override
    public Page<MovieWithRating> recentMovies(int page, int size) {
        Pageable pageable = PageUtil.getPageable(page, size);
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 4);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Timestamp end = new Timestamp(calendar.getTimeInMillis());
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MovieWithRating> cq = cb.createQuery(MovieWithRating.class);
        Root<UserMovieRating> userMovieRatingRoot = cq.from(UserMovieRating.class);
        Expression<Double> ratingAvgExp = cb.avg(userMovieRatingRoot.get("rating"));
        Expression<Long> ratingCountExp = cb.count(userMovieRatingRoot.get("rating"));
        CompoundSelection<MovieWithRating> movieWithRatingSelection =
                cb.construct(MovieWithRating.class, userMovieRatingRoot.get("movie"), ratingAvgExp, ratingCountExp);
        cq.select(movieWithRatingSelection);

        Subquery<Integer> showQuery = cq.subquery(Integer.class);
        Root<MovieShow> movieShowRoot = showQuery.from(MovieShow.class);
        showQuery.select(movieShowRoot.get("id"));
        showQuery.where(
                cb.equal(movieShowRoot.get("movie").get("id"), userMovieRatingRoot.get("movie").get("id")),
                cb.between(movieShowRoot.get("startTime"), start, end)
        );
        Predicate existShowPredicate = cb.exists(showQuery);

        cq.groupBy(userMovieRatingRoot.get("movie").get("id"));
        cq.orderBy(cb.desc(ratingAvgExp));
        cq.where(existShowPredicate);

        return doPageQuery(cq, pageable, UserMovieRating.class, "movie");
    }

    @Override
    public Page<MovieWithRating> search(String keyword, String name, String type,
                                        String actor, String director, String country,
                                        int page, int size, boolean blur) {
        Pageable pageable = PageUtil.getPageable(page, size);
        boolean skipMatch = keyword == null && name == null && type == null && actor == null && director == null && country == null;
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MovieWithRating> cq = cb.createQuery(MovieWithRating.class);
        Root<UserMovieRating> userMovieRatingRoot = cq.from(UserMovieRating.class);
        Expression<Double> ratingAvgExp = cb.avg(userMovieRatingRoot.get("rating"));
        Expression<Long> ratingCountExp = cb.count(userMovieRatingRoot.get("rating"));
        CompoundSelection<MovieWithRating> movieWithRatingSelection =
                cb.construct(MovieWithRating.class, userMovieRatingRoot.get("movie"), ratingAvgExp, ratingCountExp);
        cq.select(movieWithRatingSelection);

        if (!skipMatch) {
            List<Predicate> orPredicates = new ArrayList<>();
            List<Predicate> andPredicates = new ArrayList<>();

            if (name != null) {
                andPredicates.add(blur ?
                        cb.like(userMovieRatingRoot.get("movie").get("name"), "%" + name + "%") :
                        cb.equal(userMovieRatingRoot.get("movie").get("name"), name)
                );
            } else if (keyword != null) {
                orPredicates.add(blur ?
                        cb.like(userMovieRatingRoot.get("movie").get("name"), "%" + keyword + "%") :
                        cb.equal(userMovieRatingRoot.get("movie").get("name"), keyword)
                );
            }
            if (country != null) {
                andPredicates.add(blur ?
                        cb.like(userMovieRatingRoot.get("movie").get("country"), "%" + country + "%") :
                        cb.equal(userMovieRatingRoot.get("movie").get("country"), name)
                );
            } else if (keyword != null) {
                orPredicates.add(blur ?
                        cb.like(userMovieRatingRoot.get("movie").get("country"), "%" + keyword + "%") :
                        cb.equal(userMovieRatingRoot.get("movie").get("country"), keyword)
                );
            }
            if (type != null) {
                andPredicates.add(createMovieAttrSubqueryPredicate(
                        userMovieRatingRoot, cq, cb, "types", "type", type, blur));
            } else if (keyword != null) {
                orPredicates.add(createMovieAttrSubqueryPredicate(
                        userMovieRatingRoot, cq, cb, "types", "type", keyword, blur));
            }
            if (actor != null) {
                andPredicates.add(createMovieAttrSubqueryPredicate(
                        userMovieRatingRoot, cq, cb, "actors", "name", actor, blur
                ));
            } else if (keyword != null) {
                orPredicates.add(createMovieAttrSubqueryPredicate(
                        userMovieRatingRoot, cq, cb, "actors", "name", keyword, blur
                ));
            }
            if (director != null) {
                andPredicates.add(createMovieAttrSubqueryPredicate(
                        userMovieRatingRoot, cq, cb, "directors", "name", director, blur
                ));
            } else if (keyword != null) {
                orPredicates.add(createMovieAttrSubqueryPredicate(
                        userMovieRatingRoot, cq, cb, "directors", "name", keyword, blur
                ));
            }

            Predicate andPredicate = null;
            if (!andPredicates.isEmpty()) {
                Predicate[] predicates = new Predicate[andPredicates.size()];
                andPredicate = cb.and(andPredicates.toArray(predicates));
            }

            Predicate orPredicate = null;
            if (!orPredicates.isEmpty()) {
                Predicate[] predicates = new Predicate[orPredicates.size()];
                orPredicate = cb.or(orPredicates.toArray(predicates));
            }

            if (andPredicate != null && orPredicate != null) {
                cq.where(andPredicate, orPredicate);
            } else if (andPredicate != null) {
                cq.where(andPredicate);
            } else if (orPredicate != null) {
                cq.where(orPredicate);
            }
        }

        cq.groupBy(userMovieRatingRoot.get("movie").get("id"));
        cq.orderBy(cb.desc(ratingAvgExp));
        return doPageQuery(cq, pageable, UserMovieRating.class, "movie");
    }

    @Override
    public Optional<Movie> getMovie(long id) {
        return movieRepository.findById(id);
    }

    @Override
    public Page<Actor> getActors(PageParam pageParam) {
        if ("name".equals(pageParam.order)) {
            actorRepository.findAll(PageUtil.getPageable(pageParam));
        }
        return actorRepository.findAll(PageUtil.getPageable(pageParam.page, pageParam.size));
    }

    @Override
    public Page<Director> getDirectors(PageParam pageParam) {
        if ("name".equals(pageParam.order)) {
            directorRepository.findAll(PageUtil.getPageable(pageParam));
        }
        return directorRepository.findAll(PageUtil.getPageable(pageParam.page, pageParam.size));
    }

    @Override
    public Page<MovieType> getMovieTypes(PageParam pageParam) {
        if ("type".equals(pageParam.order)) {
            movieTypeRepository.findAll(PageUtil.getPageable(pageParam));
        }
        return movieTypeRepository.findAll(PageUtil.getPageable(pageParam.page, pageParam.size));
    }

    private Predicate createMovieAttrSubqueryPredicate(Root<UserMovieRating> root, CriteriaQuery<?> cq, CriteriaBuilder cb, String joinTable, String attr, String target, boolean blur) {
        Subquery<Integer> subquery = cq.subquery(Integer.class);
        Root<Movie> movieRoot = subquery.from(Movie.class);
        subquery.select(movieRoot.get("id"));
        Join<Object, Object> typeJoin = movieRoot.join(joinTable);
        subquery.where(
                cb.equal(root.get("movie").get("id"), movieRoot.get("id")),
                blur ? cb.like(typeJoin.get(attr), "%" + target + "%") : cb.equal(typeJoin.get(attr), target)
        );
        return cb.exists(subquery);
    }

    @Override
    public Page<String> getCountries(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);

        Root<Movie> movieRoot = cq.from(Movie.class);
        cq.distinct(true);
        cq.select(movieRoot.get("country"));

        return doPageQuery(cq, pageable, Movie.class, "country");
    }

    private <T> Page<T> doPageQuery(CriteriaQuery<T> cq, Pageable pageable, Class<?> countClass, String countAttr) {
        TypedQuery<T> query = entityManager.createQuery(cq);
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        q.select(cb.countDistinct(q.from(countClass).get(countAttr)));
        Predicate restriction = cq.getRestriction();
        if (restriction != null) {
            q.where(restriction);
        }
        TypedQuery<Long> countQuery = entityManager.createQuery(q);

        return PageableExecutionUtils.getPage(query.getResultList(), pageable,
                countQuery::getSingleResult);
    }
}

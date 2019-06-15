package com.lyc.TicketManager_Backend.db.service;

import com.lyc.TicketManager_Backend.db.bean.*;
import com.lyc.TicketManager_Backend.db.repo.KeywordRepository;
import com.lyc.TicketManager_Backend.util.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class KeywordServiceImp implements KeywordService {

    @Resource
    private KeywordRepository keywordRepository;

    @Override
    @Async
    public void saveKeyword(List<Movie> movies, Set<String> keywords) {
        keywords.remove(null);
        if (movies == null || movies.isEmpty()) {
            return;
        }
        Set<String> set = new HashSet<>();
        for (Movie movie : movies) {
            set.add(movie.getName());
            set.add(movie.getCountry());
            for (Actor actor : movie.getActors()) {
                set.add(actor.getName());
            }
            for (Director director : movie.getDirectors()) {
                set.add(director.getName());
            }
            for (MovieType type : movie.getTypes()) {
                set.add(type.getType());
            }
            set.addAll(keywords);
        }
        Set<String> saveSet = new HashSet<>();
        for (String keyword : keywords) {
            if (set.contains(keyword)) {
                saveSet.add(keyword);
            } else {
                for (String s : set) {
                    if (s.contains(keyword) || keyword.contains(s)) {
                        saveSet.add(keyword);
                    }
                }
            }
        }
        for (String s : saveSet) {
            saveSingleKeyword(s.trim());
        }
    }

    @Transactional
    void saveSingleKeyword(String keyword) {
        if (keyword == null) return;
        if (keyword.isEmpty()) return;
        Optional<Keyword> keywordOptional = keywordRepository.findById(keyword);
        Keyword target = keywordOptional.orElseGet(() -> new Keyword(keyword, 0));
        target.setHit(target.getHit() + 1);
        keywordRepository.save(target);
    }

    @Override
    public Page<Keyword> getKeywords(Integer page, Integer size) {
        return keywordRepository.findAll(PageUtil.getPageable(page, size, "hit", false));
    }
}

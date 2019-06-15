package com.lyc.TicketManager_Backend.db.service;

import com.lyc.TicketManager_Backend.db.bean.Keyword;
import com.lyc.TicketManager_Backend.db.bean.Movie;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface KeywordService {
    @Transactional
    @Async
    void saveKeyword(List<Movie> list, Set<String> keywords);

    Page<Keyword> getKeywords(Integer page, Integer size);
}

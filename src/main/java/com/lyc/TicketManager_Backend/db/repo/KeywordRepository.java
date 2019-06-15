package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.Keyword;
import com.lyc.TicketManager_Backend.db.bean.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface KeywordRepository extends JpaRepository<Keyword, String>, JpaSpecificationExecutor<Movie> {
}

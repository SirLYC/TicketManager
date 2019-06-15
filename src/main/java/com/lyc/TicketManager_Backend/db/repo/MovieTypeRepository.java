package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.MovieType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieTypeRepository extends JpaRepository<MovieType, Long> {

}

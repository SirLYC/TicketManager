package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.Ticket;
import com.lyc.TicketManager_Backend.db.bean.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    @Modifying
    @Transactional
    @Query("update Ticket ticket set ticket.refund = true, ticket.refundTime = :time where ticket.id = :id and ticket.refund = false and ticket.refundTime = null ")
    int refund(@Param("time") Timestamp timestamp, @Param("id") String id);

    Page<Ticket> findAllByUser(User user, Pageable pageable);

    Page<Ticket> findAllByUserAndRefund(User user, boolean refund, Pageable pageable);
}

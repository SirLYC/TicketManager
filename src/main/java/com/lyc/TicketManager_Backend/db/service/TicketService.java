package com.lyc.TicketManager_Backend.db.service;

import com.lyc.TicketManager_Backend.db.bean.Ticket;
import com.lyc.TicketManager_Backend.db.bean.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface TicketService {
    @Transactional
    Ticket purchaseTicket(User user, long movieShowId, Set<Integer> seatLocations, int payMethod);

    @Transactional
    void returnTicket(User user, String ticketId);
}

package com.lyc.TicketManager_Backend.bean;

import lombok.Data;

@Data
public class PageParam {
    public final Integer page;
    public final Integer size;
    public final String order;
    public final Boolean asc;
}

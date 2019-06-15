package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PageInfo {
    private final int page;
    private final int size;
    @JsonProperty("total_page")
    private final int totalPage;
    @JsonProperty("total_size")
    private final long totalSize;
}

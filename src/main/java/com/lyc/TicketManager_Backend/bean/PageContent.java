package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageContent<T> {
    private final List<T> list;
    @JsonProperty("page_info")
    private final PageInfo pageInfo;

    public static <E> PageContent<E> of(Page<E> page) {
        return new PageContent<>(
                page.toList(),
                new PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.getTotalElements()
                )
        );
    }
}

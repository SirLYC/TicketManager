package com.lyc.TicketManager_Backend.util;

import com.lyc.TicketManager_Backend.bean.PageParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public class PageUtil {
    public static Pageable getPageable(Integer page, Integer size) {
        return getPageable(page, size, null, null);
    }

    public static Pageable getPageable(Integer page, Integer size, String order, Boolean asc) {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (order != null) {
            if (asc == null) asc = true;
            Sort sort = Sort.by(Sort.Order.by(order));
            if (asc) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }
            return PageRequest.of(page, size, sort);
        }
        return PageRequest.of(page, size);
    }

    public static Pageable getPageable(PageParam pageParam) {
        return getPageable(pageParam.page, pageParam.size, pageParam.order, pageParam.asc);
    }
}

package com.lyc.TicketManager_Backend.bean;

import com.lyc.TicketManager_Backend.config.StatusCode;
import lombok.Data;

@Data
public class ResponseMessage<T> {
    private final int code;
    private final T content;

    public static <E> ResponseMessage<E> success(E content) {
        return new ResponseMessage<>(StatusCode.SUCCESS, content);
    }

    public static <E> ResponseMessage<E> innerError(E content) {
        return new ResponseMessage<>(StatusCode.INNER_ERROR, content);
    }

    public static <E> ResponseMessage<E> requestError(E content) {
        return new ResponseMessage<>(StatusCode.REQUEST_ERROR, content);
    }

    public static <E> ResponseMessage<E> notFountError(E content) {
        return new ResponseMessage<>(StatusCode.NOT_FOUND, content);
    }
}

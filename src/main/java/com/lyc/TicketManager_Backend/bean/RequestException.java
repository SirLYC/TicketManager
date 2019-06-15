package com.lyc.TicketManager_Backend.bean;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestException extends RuntimeException {
    private int code;
    private String message;

    public RequestException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseMessage<String> toResponseMessage() {
        return new ResponseMessage<>(code, message);
    }

}

package com.lyc.TicketManager_Backend.controller;

import com.lyc.TicketManager_Backend.bean.RequestException;
import com.lyc.TicketManager_Backend.bean.ResponseMessage;
import com.lyc.TicketManager_Backend.config.StatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(Exception.class)
    public ResponseMessage<?> handleError(HttpServletRequest request, Exception e) {
        if (e instanceof RequestException) {
            return ((RequestException) e).toResponseMessage();
        }

        e.printStackTrace();
        return new ResponseMessage<>(StatusCode.INNER_ERROR, e.getMessage());
    }
}

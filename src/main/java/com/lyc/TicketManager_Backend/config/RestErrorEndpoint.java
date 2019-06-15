package com.lyc.TicketManager_Backend.config;

import com.lyc.TicketManager_Backend.bean.ResponseMessage;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class RestErrorEndpoint implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public ResponseMessage<?> error(HttpServletRequest request, HttpServletResponse response) {
        return ResponseMessage.notFountError("not found");
    }


    @Override
    public String getErrorPath() {
        return PATH;
    }
}
package com.lyc.TicketManager_Backend.util;

import com.lyc.TicketManager_Backend.bean.RequestException;
import com.lyc.TicketManager_Backend.config.StatusCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class BindingResultHandler {
    public static void checkRequest(BindingResult bindingResult) throws RequestException {
        if (bindingResult.hasFieldErrors()) {
            throw new RequestException(StatusCode.REQUEST_ERROR, bindingResult.getFieldError().getDefaultMessage());
        }

        boolean hasError = bindingResult.hasErrors();
        if (hasError) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error.getDefaultMessage() != null) {
                    throw new RequestException(StatusCode.INNER_ERROR, error.getDefaultMessage());
                }
            }
        }

        if (hasError) {
            throw new RequestException(StatusCode.INNER_ERROR, "未知错误");
        }
    }
}

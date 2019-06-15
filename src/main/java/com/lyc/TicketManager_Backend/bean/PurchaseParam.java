package com.lyc.TicketManager_Backend.bean;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseParam {
    @JsonProperty("movie_show_id")
    @NotNull(message = "电影场次id未提供")
    private Long movieShowId;
    @NotEmpty(message = "购买座位未提供")
    @NotNull(message = "购买座位未提供")
    private Set<Integer> seats;
    @NotNull(message = "支付方式未提供")
    @JsonProperty("pay_method")
    private Integer payMethod;
}

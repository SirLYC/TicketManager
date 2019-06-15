package com.lyc.TicketManager_Backend.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketIdBody {
    @JsonProperty("ticket_id")
    @NotNull(message = "未提供订单id")
    @NotBlank(message = "未提供订单id")
    private String ticketId;
}

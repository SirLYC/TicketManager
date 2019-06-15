package com.lyc.TicketManager_Backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private long id;
    private String username;
    @NotNull(message = "昵称不能为空")
    private String nickname;
}

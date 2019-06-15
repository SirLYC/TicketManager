package com.lyc.TicketManager_Backend.bean;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UserRegister {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{1,21}$", message = "用户名由1~21位大小写字母、下划线、数字组成")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,12}$", message = "密码由6~12位大小写字母、下划线、数字组成")
    private String password;
    @NotNull(message = "昵称不能为空")
    private String nickname;
}

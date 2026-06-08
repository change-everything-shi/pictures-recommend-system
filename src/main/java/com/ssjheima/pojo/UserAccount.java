package com.ssjheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private Integer id;
    private String username;
    private String passwordHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


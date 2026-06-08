package com.ssjheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoComment {
    private Integer id;
    private Integer photoId;
    private Integer userId;
    private String username;
    private String content;
    private LocalDateTime createTime;
}


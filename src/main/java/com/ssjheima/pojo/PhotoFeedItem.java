package com.ssjheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoFeedItem {
    private Integer id;
    private Integer userId;
    private String username;
    private String title;
    private String description;
    private String tags;
    private String imageUrl;
    private LocalDateTime createTime;
}


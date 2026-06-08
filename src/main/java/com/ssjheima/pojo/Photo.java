package com.ssjheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    private Integer id;
    private Integer userId;
    private String title;
    private String description;
    private String tags;      // 逗号分隔：游戏,数码,家具...
    private String imageUrl;  // 例如：/uploads/photos/xxx.jpg
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


package com.ssjheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoStats {
    private Integer photoId;
    private long likeCount;
    private long favoriteCount;
    private boolean likedByMe;
    private boolean favoritedByMe;
    private List<PhotoComment> comments;
}


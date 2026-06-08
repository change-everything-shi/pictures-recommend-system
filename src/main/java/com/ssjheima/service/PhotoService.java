package com.ssjheima.service;

import com.ssjheima.pojo.PhotoFeedItem;
import com.ssjheima.pojo.PhotoStats;

import java.util.List;

public interface PhotoService {
    Integer publish(Integer userId, String title, String description, String tags, String imageUrl);

    List<PhotoFeedItem> feed(Integer viewerUserId);

    PhotoFeedItem detail(Integer id);

    List<PhotoFeedItem> listAll();

    List<PhotoFeedItem> listByIds(List<Integer> ids);

    List<PhotoFeedItem> listMine(Integer userId);

    void updateOwn(Integer userId, Integer photoId, String title, String description, String tags);

    void deleteOwn(Integer userId, Integer photoId);

    PhotoStats buildStats(Integer photoId, Integer currentUserId);

    void like(Integer photoId, Integer currentUserId);

    void favorite(Integer photoId, Integer currentUserId);

    void comment(Integer photoId, Integer currentUserId, String content);

    List<PhotoFeedItem> listLiked(Integer userId);

    List<PhotoFeedItem> listFavorited(Integer userId);

    List<PhotoFeedItem> listCommented(Integer userId);
}


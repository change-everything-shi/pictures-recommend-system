package com.ssjheima.mapper;

import com.ssjheima.pojo.Photo;
import com.ssjheima.pojo.PhotoFeedItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PhotoMapper {

    void insert(Photo photo);

    PhotoFeedItem findFeedItemById(Integer id);

    List<PhotoFeedItem> listFeed(Integer viewerUserId);

    List<PhotoFeedItem> listAllFeedItems();

    List<PhotoFeedItem> listByIds(List<Integer> ids);

    List<PhotoFeedItem> listByUser(Integer userId);

    void updatePhoto(Photo photo);

    void deleteOwn(Photo photo);

    List<PhotoFeedItem> listLikedByUser(Integer userId);

    List<PhotoFeedItem> listFavoritedByUser(Integer userId);

    List<PhotoFeedItem> listCommentedByUser(Integer userId);
}


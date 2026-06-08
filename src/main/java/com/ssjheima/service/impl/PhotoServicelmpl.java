package com.ssjheima.service.impl;

import com.ssjheima.mapper.PhotoInteractionMapper;
import com.ssjheima.mapper.PhotoMapper;
import com.ssjheima.pojo.Photo;
import com.ssjheima.pojo.PhotoComment;
import com.ssjheima.pojo.PhotoFeedItem;
import com.ssjheima.pojo.PhotoStats;
import com.ssjheima.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class PhotoServicelmpl implements PhotoService {

    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private PhotoInteractionMapper photoInteractionMapper;

    @Override
    public Integer publish(Integer userId, String title, String description, String tags, String imageUrl) {
        Photo p = new Photo();
        p.setUserId(userId);
        p.setTitle(title);
        p.setDescription(description);
        p.setTags(tags);
        p.setImageUrl(imageUrl);
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        photoMapper.insert(p);
        return p.getId();
    }

    @Override
    public List<PhotoFeedItem> feed(Integer viewerUserId) {
        return photoMapper.listFeed(viewerUserId);
    }

    @Override
    public PhotoFeedItem detail(Integer id) {
        return photoMapper.findFeedItemById(id);
    }

    @Override
    public List<PhotoFeedItem> listAll() {
        return photoMapper.listAllFeedItems();
    }

    @Override
    public List<PhotoFeedItem> listByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return photoMapper.listByIds(ids);
    }

    @Override
    public List<PhotoFeedItem> listMine(Integer userId) {
        return photoMapper.listByUser(userId);
    }

    @Override
    public void updateOwn(Integer userId, Integer photoId, String title, String description, String tags) {
        Photo p = new Photo();
        p.setId(photoId);
        p.setUserId(userId);
        p.setTitle(title);
        p.setDescription(description);
        p.setTags(tags);
        p.setUpdateTime(LocalDateTime.now());
        photoMapper.updatePhoto(p);
    }

    @Override
    public void deleteOwn(Integer userId, Integer photoId) {
        // 先删交互数据，再删照片，避免外键约束失败
        photoInteractionMapper.deleteLikesByPhoto(photoId);
        photoInteractionMapper.deleteFavoritesByPhoto(photoId);
        photoInteractionMapper.deleteCommentsByPhoto(photoId);

        Photo p = new Photo();
        p.setId(photoId);
        p.setUserId(userId);
        photoMapper.deleteOwn(p);
    }

    @Override
    public PhotoStats buildStats(Integer photoId, Integer currentUserId) {
        long likeCount = photoInteractionMapper.countLike(photoId);
        long favoriteCount = photoInteractionMapper.countFavorite(photoId);
        boolean likedByMe = false;
        boolean favoritedByMe = false;
        if (currentUserId != null) {
            likedByMe = photoInteractionMapper.existsLike(photoId, currentUserId) > 0;
            favoritedByMe = photoInteractionMapper.existsFavorite(photoId, currentUserId) > 0;
        }
        List<PhotoComment> comments = photoInteractionMapper.listComments(photoId);
        PhotoStats stats = new PhotoStats();
        stats.setPhotoId(photoId);
        stats.setLikeCount(likeCount);
        stats.setFavoriteCount(favoriteCount);
        stats.setLikedByMe(likedByMe);
        stats.setFavoritedByMe(favoritedByMe);
        stats.setComments(comments);
        return stats;
    }

    @Override
    public void like(Integer photoId, Integer currentUserId) {
        photoInteractionMapper.insertLike(photoId, currentUserId);
    }

    @Override
    public void favorite(Integer photoId, Integer currentUserId) {
        photoInteractionMapper.insertFavorite(photoId, currentUserId);
    }

    @Override
    public void comment(Integer photoId, Integer currentUserId, String content) {
        PhotoComment c = new PhotoComment();
        c.setPhotoId(photoId);
        c.setUserId(currentUserId);
        c.setContent(content);
        photoInteractionMapper.insertComment(c);
    }

    @Override
    public List<PhotoFeedItem> listLiked(Integer userId) {
        return photoMapper.listLikedByUser(userId);
    }

    @Override
    public List<PhotoFeedItem> listFavorited(Integer userId) {
        return photoMapper.listFavoritedByUser(userId);
    }

    @Override
    public List<PhotoFeedItem> listCommented(Integer userId) {
        return photoMapper.listCommentedByUser(userId);
    }
}


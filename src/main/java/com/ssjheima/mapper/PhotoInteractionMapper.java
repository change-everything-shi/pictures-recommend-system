package com.ssjheima.mapper;

import com.ssjheima.pojo.PhotoComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PhotoInteractionMapper {

    void insertLike(@Param("photoId") Integer photoId, @Param("userId") Integer userId);

    Long countLike(@Param("photoId") Integer photoId);

    Long existsLike(@Param("photoId") Integer photoId, @Param("userId") Integer userId);

    void insertFavorite(@Param("photoId") Integer photoId, @Param("userId") Integer userId);

    Long countFavorite(@Param("photoId") Integer photoId);

    Long existsFavorite(@Param("photoId") Integer photoId, @Param("userId") Integer userId);

    void insertComment(PhotoComment comment);

    List<PhotoComment> listComments(@Param("photoId") Integer photoId);

    void deleteLikesByPhoto(@Param("photoId") Integer photoId);

    void deleteFavoritesByPhoto(@Param("photoId") Integer photoId);

    void deleteCommentsByPhoto(@Param("photoId") Integer photoId);
}


package com.ssjheima.service;

import com.ssjheima.pojo.PhotoFeedItem;

import java.util.List;

public interface RecommendService {
    List<Integer> recommendIds(PhotoFeedItem target, List<PhotoFeedItem> candidates, Integer topK);
}


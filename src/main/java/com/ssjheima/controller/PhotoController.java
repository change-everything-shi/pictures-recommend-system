package com.ssjheima.controller;

import com.ssjheima.pojo.PhotoFeedItem;
import com.ssjheima.pojo.PhotoStats;
import com.ssjheima.pojo.Result;
import com.ssjheima.service.LocalPhotoStorageService;
import com.ssjheima.service.PhotoService;
import com.ssjheima.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@RestController
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private LocalPhotoStorageService localPhotoStorageService;

    @Autowired
    private RecommendService recommendService;

    @PostMapping("/photos")
    public Result publish(@RequestParam("file") MultipartFile file,
                          @RequestParam String title,
                          @RequestParam(required = false) String description,
                          @RequestParam String tags,
                          HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String username = (String) session.getAttribute(AuthController.SESSION_USERNAME);

        String imageUrl = localPhotoStorageService.save(file);
        if (imageUrl == null) {
            return Result.error("图片不能为空");
        }
        Integer id = photoService.publish(uid, title, description, normalizeTags(tags), imageUrl);
        log.info("发布照片，用户：{}，photoId：{}", username, id);
        return Result.success();
    }

    @GetMapping("/photos/feed")
    public Result feed(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        List<PhotoFeedItem> list = photoService.feed(uid);
        return Result.success(list);
    }

    @GetMapping("/photos/my")
    public Result my(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        List<PhotoFeedItem> list = photoService.listMine(uid);
        return Result.success(list);
    }

    @GetMapping("/photos/liked")
    public Result liked(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        return Result.success(photoService.listLiked(uid));
    }

    @GetMapping("/photos/favorited")
    public Result favorited(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        return Result.success(photoService.listFavorited(uid));
    }

    @GetMapping("/photos/commented")
    public Result commented(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        return Result.success(photoService.listCommented(uid));
    }

    @GetMapping("/photos/search")
    public Result search(@RequestParam(required = false) String q) {
        String keyword = q == null ? "" : q.trim();
        if (keyword.isEmpty()) {
            // 关键字为空时，直接返回全部（由前端决定是否重新随机）
            return Result.success(photoService.listAll());
        }
        String kwLower = keyword.toLowerCase();
        List<PhotoFeedItem> all = photoService.listAll();
        List<PhotoFeedItem> matched = new ArrayList<>();
        for (PhotoFeedItem p : all) {
            if (p == null) continue;
            String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
            String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
            String tags = p.getTags() != null ? p.getTags().toLowerCase() : "";
            if (title.contains(kwLower) || desc.contains(kwLower) || tags.contains(kwLower)) {
                matched.add(p);
            }
        }
        return Result.success(matched);
    }

    @GetMapping("/photos/{id}")
    public Result detail(@PathVariable Integer id) {
        PhotoFeedItem item = photoService.detail(id);
        if (item == null) {
            return Result.error("照片不存在");
        }
        return Result.success(item);
    }

    @GetMapping("/photos/{id}/recommend")
    public Result recommend(@PathVariable Integer id,
                            @RequestParam(defaultValue = "6") Integer topK) {
        PhotoFeedItem target = photoService.detail(id);
        if (target == null) {
            return Result.error("照片不存在");
        }
        List<PhotoFeedItem> all = photoService.listAll();
        List<PhotoFeedItem> candidates = new ArrayList<>();
        for (PhotoFeedItem p : all) {
            if (p != null && p.getId() != null && !p.getId().equals(target.getId())) {
                candidates.add(p);
            }
        }
        List<Integer> ids = recommendService.recommendIds(target, candidates, topK);
        ids = dedupKeepOrder(ids);
        List<PhotoFeedItem> rec = reorderByIds(photoService.listByIds(ids), ids);
        return Result.success(rec);
    }

    @PutMapping("/photos/{id}")
    public Result update(@PathVariable Integer id,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam String tags,
                         HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        photoService.updateOwn(uid, id, title, description, normalizeTags(tags));
        return Result.success();
    }

    @GetMapping("/photos/{id}/stats")
    public Result stats(@PathVariable Integer id, HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        PhotoStats stats = photoService.buildStats(id, uid);
        return Result.success(stats);
    }

    @PostMapping("/photos/{id}/like")
    public Result like(@PathVariable Integer id, HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        photoService.like(id, uid);
        return Result.success();
    }

    @PostMapping("/photos/{id}/favorite")
    public Result favorite(@PathVariable Integer id, HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        photoService.favorite(id, uid);
        return Result.success();
    }

    @PostMapping("/photos/{id}/comment")
    public Result comment(@PathVariable Integer id,
                          @RequestParam String content,
                          HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        photoService.comment(id, uid, content);
        return Result.success();
    }

    @DeleteMapping("/photos/{id}")
    public Result delete(@PathVariable Integer id, HttpSession session) {
        Integer uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        photoService.deleteOwn(uid, id);
        return Result.success();
    }

    // 最近 3 张分段混合推荐：ids=1,2,3
    @GetMapping("/photos/recommend/history")
    public Result recommendByHistory(@RequestParam String ids,
                                     @RequestParam(defaultValue = "12") Integer topK) {
        List<Integer> historyIds = parseIds(ids);
        if (historyIds.size() < 1) {
            return Result.error("ids不能为空");
        }

        List<PhotoFeedItem> all = photoService.listAll();
        List<PhotoFeedItem> candidates = new ArrayList<>();
        for (PhotoFeedItem p : all) {
            if (p == null || p.getId() == null) continue;
            if (historyIds.contains(p.getId())) continue;
            candidates.add(p);
        }

        // 按点击顺序取出最近 3 张的详情（可能有缺失，需判空）
        List<PhotoFeedItem> history = new ArrayList<>();
        for (Integer pid : historyIds) {
            PhotoFeedItem item = photoService.detail(pid);
            if (item != null) {
                history.add(item);
            }
        }
        if (history.isEmpty()) {
            return Result.error("浏览记录无效");
        }

        // 对每一张历史照片分别做一次推荐，再进行分段混合：
        // 如果其中某一类（如“美女”）出现多次，就会多贡献几段推荐，自然权重更高。
        int k = topK == null || topK <= 0 ? 12 : topK;
        int perSize = Math.max(4, k); // 每段多取一些，方便后面去重混合

        List<List<Integer>> perHistoryIds = new ArrayList<>();
        for (PhotoFeedItem h : history) {
            List<Integer> idsOne = recommendService.recommendIds(h, candidates, perSize);
            perHistoryIds.add(dedupKeepOrder(idsOne));
        }

        // 轮询每一段结果，按顺序从每段取一个，直到达到 topK 或都取完
        List<Integer> mixed = new ArrayList<>();
        boolean added;
        int round = 0;
        do {
            added = false;
            for (List<Integer> seg : perHistoryIds) {
                if (seg == null || seg.isEmpty()) continue;
                if (round >= seg.size()) continue;
                Integer cid = seg.get(round);
                if (cid == null) continue;
                if (historyIds.contains(cid)) continue;
                if (mixed.contains(cid)) continue;
                mixed.add(cid);
                added = true;
                if (mixed.size() >= k) break;
            }
            round++;
        } while (added && mixed.size() < k);

        mixed = dedupKeepOrder(mixed);
        List<PhotoFeedItem> rec = reorderByIds(photoService.listByIds(mixed), mixed);
        return Result.success(rec);
    }

    private String normalizeTags(String tags) {
        if (tags == null) {
            return "";
        }
        String[] arr = tags.split("[,，\\s]+");
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String t : arr) {
            if (t == null) continue;
            String s = t.trim();
            if (!s.isEmpty()) set.add(s);
        }
        return String.join(",", set);
    }

    private List<Integer> dedupKeepOrder(List<Integer> ids) {
        if (ids == null) return new ArrayList<>();
        LinkedHashSet<Integer> set = new LinkedHashSet<>(ids);
        return new ArrayList<>(set);
    }

    private List<Integer> parseIds(String ids) {
        List<Integer> out = new ArrayList<>();
        if (ids == null) return out;
        String[] arr = ids.split("[,，\\s]+");
        for (String s : arr) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isEmpty()) continue;
            try {
                out.add(Integer.parseInt(t));
            } catch (Exception ignored) {
            }
        }
        return dedupKeepOrder(out);
    }

    private String joinField(List<PhotoFeedItem> list, String field) {
        StringJoiner sj = new StringJoiner(" ");
        for (PhotoFeedItem p : list) {
            if (p == null) continue;
            String v = null;
            if ("title".equals(field)) v = p.getTitle();
            if ("description".equals(field)) v = p.getDescription();
            if ("tags".equals(field)) v = p.getTags();
            if (v != null && !v.trim().isEmpty()) sj.add(v.trim());
        }
        return sj.toString();
    }

    private List<PhotoFeedItem> reorderByIds(List<PhotoFeedItem> list, List<Integer> ids) {
        if (list == null || ids == null) {
            return list;
        }
        Map<Integer, PhotoFeedItem> map = new HashMap<>();
        for (PhotoFeedItem p : list) {
            if (p != null && p.getId() != null) {
                map.put(p.getId(), p);
            }
        }
        List<PhotoFeedItem> out = new ArrayList<>();
        for (Integer id : ids) {
            PhotoFeedItem p = map.get(id);
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }
}


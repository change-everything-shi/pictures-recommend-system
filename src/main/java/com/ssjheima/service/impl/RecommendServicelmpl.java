package com.ssjheima.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssjheima.pojo.PhotoFeedItem;
import com.ssjheima.service.RecommendService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Service
public class RecommendServicelmpl implements RecommendService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Integer> recommendIds(PhotoFeedItem target, List<PhotoFeedItem> candidates, Integer topK) {
        if (target == null) {
            return Collections.emptyList();
        }
        if (candidates == null) {
            candidates = Collections.emptyList();
        }
        int k = (topK == null || topK <= 0) ? 6 : topK;

        String url = System.getenv("PY_RECOMMENDER_URL");
        if (url == null || url.trim().isEmpty()) {
            url = "http://127.0.0.1:5000/recommend";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("topK", k);
        payload.put("target", toSimplePhoto(target));
        payload.put("candidates", toSimpleCandidates(candidates));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactoryWithTimeouts());

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return Collections.emptyList();
        }

        Map<String, Object> map;
        try {
            map = objectMapper.readValue(resp.getBody(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
        Object idsObj = map.get("ids");
        if (!(idsObj instanceof List)) {
            return Collections.emptyList();
        }
        List<?> raw = (List<?>) idsObj;
        List<Integer> ids = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Number) {
                ids.add(((Number) o).intValue());
            } else if (o instanceof String) {
                try {
                    ids.add(Integer.parseInt((String) o));
                } catch (Exception ignored) {
                }
            }
        }
        return ids;
    }

    private Object toSimplePhoto(PhotoFeedItem p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("tags", p.getTags());
        return m;
    }

    private List<Object> toSimpleCandidates(List<PhotoFeedItem> candidates) {
        List<Object> out = new ArrayList<>(candidates.size());
        for (PhotoFeedItem p : candidates) {
            out.add(toSimplePhoto(p));
        }
        return out;
    }

    private org.springframework.http.client.ClientHttpRequestFactory clientHttpRequestFactoryWithTimeouts() {
        org.springframework.http.client.SimpleClientHttpRequestFactory f = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        int timeoutMs = (int) Duration.ofSeconds(3).toMillis();
        f.setConnectTimeout(timeoutMs);
        f.setReadTimeout(timeoutMs);
        return f;
    }
}


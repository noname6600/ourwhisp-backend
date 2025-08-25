package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.dto.MessageSearchFilterDto;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MessageSearchCacheService extends BaseRedisService<Object> {

    private static final String SEARCH_PREFIX = "messages:search:";

    public void saveSearchResult(String sessionUUID, MessageSearchFilterDto filter, List<Message> messages, long totalCount) {
        String cacheKey = getSearchKey(sessionUUID, filter);
        MessageSearchResult result = new MessageSearchResult(
                messages,
                filter.getPage(),
                filter.getSize(),
                (int) Math.ceil((double) totalCount / filter.getSize()),
                totalCount
        );
        saveToHash(SEARCH_PREFIX + cacheKey, "result", result);
        expireKey(SEARCH_PREFIX + cacheKey, 5, TimeUnit.MINUTES);
    }

    public MessageSearchResult getSearchResult(String sessionUUID, MessageSearchFilterDto filter) {
        String cacheKey = getSearchKey(sessionUUID, filter);
        return (MessageSearchResult) getFromHash(SEARCH_PREFIX + cacheKey, "result");
    }

    private String getSearchKey(String sessionUUID, MessageSearchFilterDto filter) {
        return String.format(
                "%s:keyword:%s:length:%s:minViews:%s:page:%d:size:%d",
                sessionUUID,
                filter.getKeyword() == null ? "" : filter.getKeyword().toLowerCase(),
                filter.getLength() == null ? "" : filter.getLength().toLowerCase(),
                filter.getMinViews() == null ? "" : filter.getMinViews(),
                filter.getPage(),
                filter.getSize()
        );
    }
}

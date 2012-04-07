package org.mutabilitydetector.jsr353.jsonreaders.model;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

public final class OrgJsonExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryService;

    public OrgJsonExtractor(TopicCategoryService topicCategoryService) {
        this.topicCategoryService = topicCategoryService;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) {
        try {
            JSONObject json = new JSONObject(new JSONTokener(new InputStreamReader(jsonInput)));
            return extractTrendsFrom(json);
        } catch (JSONException e) {
           throw new IllegalArgumentException("Invalid JSON supplied", e);
        }
    }

    private Iterable<TrendingTopic> extractTrendsFrom(JSONObject rootObject) {
        List<TrendingTopic> trendingTopics = new ArrayList<TrendingTopic>();
        
        JSONObject trends = rootObject.optJSONObject("trends");
        
        @SuppressWarnings("unchecked")
        Iterator<String> trendDateHourKeys = trends.keys();
        
        while (trendDateHourKeys.hasNext()) {
            String trendDateHourKey = trendDateHourKeys.next();
            DateTime trendHour = parseDateTime(trendDateHourKey);
            
            JSONArray topTrendsInHour = trends.optJSONArray(trendDateHourKey.toString());
            for (int i = 0; i < topTrendsInHour.length(); i++) {
                JSONObject topTrend = topTrendsInHour.optJSONObject(i);
                
                int position = i + 1;
                String topic = stripHash(topTrend.optString("name"));
                Category category = topicCategoryService.categoryFor(topic);
                
                trendingTopics.add(new TrendingTopic(trendHour, position, topic, category));
            }
            
        }
        
        
        return Collections.unmodifiableList(trendingTopics);
    }


}

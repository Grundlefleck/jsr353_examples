package org.mutabilitydetector.jsr353.jsonreaders;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public final class GoogleGsonExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryservice;

    public GoogleGsonExtractor(TopicCategoryService topicCategoryservice) {
        this.topicCategoryservice = topicCategoryservice;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException {
        JsonStreamParser gsonStreamParser = new JsonStreamParser(new InputStreamReader(jsonInput));
        
        return extractTrendsFrom(gsonStreamParser);
    }
    
    
    private Iterable<TrendingTopic> extractTrendsFrom(JsonStreamParser gsonStreamParser) {
        ArrayList<TrendingTopic> trendingTopics = new ArrayList<TrendingTopic>();
        JsonElement rootElement = gsonStreamParser.next();
        
        if (rootElement.isJsonObject()) {
            JsonObject rootObject = rootElement.getAsJsonObject();
            
            JsonObject trends = rootObject.get("trends").getAsJsonObject();
            
            for (Entry<String, JsonElement> hourOfTrends : trends.entrySet()) {
                DateTime hour = parseDateTime(hourOfTrends.getKey());
                int position = 1;
                
                JsonArray trendsInHour = hourOfTrends.getValue().getAsJsonArray();
                
                for (JsonElement jsonElement : trendsInHour) {
                    String topicName = stripHash(jsonElement.getAsJsonObject().get("name").getAsString());
                    
                    Category category = topicCategoryservice.categoryFor(topicName);
                    
                    trendingTopics.add(new TrendingTopic(hour, position, topicName, category));
                    position++;
                }
            }
        }
        
        return Collections.unmodifiableList(trendingTopics);
    }


    public static class Trends {
        
    }

}

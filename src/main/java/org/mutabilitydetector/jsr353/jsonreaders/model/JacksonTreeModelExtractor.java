package org.mutabilitydetector.jsr353.jsonreaders.model;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

public final class JacksonTreeModelExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryService;

    public JacksonTreeModelExtractor(TopicCategoryService topicCategoryService) {
        this.topicCategoryService = topicCategoryService;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException {
        JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
        try {
            JsonParser parser = jsonFactory.createJsonParser(jsonInput);
            return extractTrendsFrom(parser);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON supplied", e);
        } 
    }

    private Iterable<TrendingTopic> extractTrendsFrom(JsonParser parser) throws IOException {
        List<TrendingTopic> trendingTopics = new ArrayList<TrendingTopic>();
        
        JsonNode trends = parser.readValueAsTree().path("trends");
        Iterator<Entry<String, JsonNode>> trendsByHour = trends.getFields();
        
        while (trendsByHour.hasNext()) {
            Entry<String, JsonNode> trendsInHour = trendsByHour.next();
            
            DateTime hour = parseDateTime(trendsInHour.getKey());
            Iterator<JsonNode> topics = trendsInHour.getValue().getElements();
            
            int position = 1;
            while (topics.hasNext()) {
                JsonNode trendEntry = topics.next();
                
                String name = stripHash(trendEntry.get("name").asText());
                Category category = topicCategoryService.categoryFor(name);
                
                trendingTopics.add(new TrendingTopic(hour, position, name, category));
                position++;
            }
        }
        
        return Collections.unmodifiableList(trendingTopics);
    }

}

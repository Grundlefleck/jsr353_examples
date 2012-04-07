package org.mutabilitydetector.jsr353.jsonreaders.streaming;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.joda.time.DateTime;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

public final class JacksonStreamingExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryService;

    public JacksonStreamingExtractor(TopicCategoryService topicCategoryService) {
        this.topicCategoryService = topicCategoryService;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        try {
            JsonParser parser = jsonFactory.createJsonParser(jsonInput);
            return extractTrendsFrom(parser);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON supplied", e);
        } 
    }

    private Iterable<TrendingTopic> extractTrendsFrom(JsonParser parser) throws JsonParseException, IOException {
        List<TrendingTopic> trendingTopics = new ArrayList<TrendingTopic>();
        
        parser.nextToken();
        
        while (parser.nextToken() != null) {
            if ("trends".equals(parser.getCurrentName())) {
                
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    while (parser.nextToken() == JsonToken.FIELD_NAME) {
                        DateTime trendDateHour = parseDateTime(parser.getCurrentName());
                        int position = 1;
                        
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_OBJECT) {

                                if ("name".equals(parser.getCurrentName())) {
                                    String trendTopicName = stripHash(parser.nextTextValue());
                                    Category category = topicCategoryService.categoryFor(trendTopicName);
                                    
                                    TrendingTopic trendingTopic = new TrendingTopic(trendDateHour, 
                                                                                    position, 
                                                                                    trendTopicName, 
                                                                                    category);
                                    trendingTopics.add(trendingTopic);
                                }
                            }
                            position++;
                        }
                    }
                }
            }
        }
        
        return Collections.unmodifiableList(trendingTopics);
    }

}

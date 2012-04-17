package org.mutabilitydetector.jsr353.jsonreaders.streaming;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

public class JsonSimpleStreamingExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryService;

    public JsonSimpleStreamingExtractor(TopicCategoryService topicCategoryService) {
        this.topicCategoryService = topicCategoryService;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException {
        JSONParser jsonParser = new JSONParser();
        try {
            return extractTrendsFrom(jsonInput, jsonParser);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid JSON supplied", e);
        }
    }

    private Iterable<TrendingTopic> extractTrendsFrom(InputStream jsonInput, JSONParser jsonParser) throws IOException, ParseException {
        TrendsContentHandler trendsContentHandler = new TrendsContentHandler(topicCategoryService);
        jsonParser.parse(new InputStreamReader(jsonInput), trendsContentHandler);
        return Collections.unmodifiableList(trendsContentHandler.topics());
    }
    
    private static class TrendsContentHandler implements ContentHandler {

        private final TopicCategoryService topicCategoryService;
        private final List<TrendingTopic> trendingTopics = new ArrayList<>(); 
        
        
        private boolean continueParsing = true;
        private boolean withinTrendsObject;
        private boolean withinHoursArray;
        private boolean withinHourlyTrendObject;
        private boolean expectingTopicName;
        
        private DateTime hour;
        private int position;

        public TrendsContentHandler(TopicCategoryService topicCategoryService) {
            this.topicCategoryService = topicCategoryService;
        }

        public List<TrendingTopic> topics() {
            return trendingTopics;
        }

        @Override
        public void startJSON() throws ParseException, IOException {
            withinTrendsObject = false;
            withinHoursArray = false;
            withinHourlyTrendObject = false;
            expectingTopicName = false;
        }

        @Override
        public void endJSON() throws ParseException, IOException {
            
        }

        @Override
        public boolean startObject() throws ParseException, IOException {
            if (withinTrendsObject && withinHoursArray) {
                withinHourlyTrendObject = true;
                position++;
            }
            return continueParsing;
        }

        @Override
        public boolean endObject() throws ParseException, IOException {
            if (withinTrendsObject && withinHoursArray && withinHourlyTrendObject) {
                withinHourlyTrendObject = false;
            } else if (withinTrendsObject && withinHoursArray) {
                withinHoursArray = false;
            } else if (withinTrendsObject) {
                withinTrendsObject = false;
                continueParsing = false;
            }
            
            return continueParsing;
        }

        @Override
        public boolean startObjectEntry(String key) throws ParseException, IOException {
            if ("trends".equals(key)) {
                withinTrendsObject = true;
            } else if (withinTrendsObject && !(withinHourlyTrendObject))  {
                hour = parseDateTime(key);
                position = 0;
            } else if (withinHourlyTrendObject && "name".equals(key)) {
                expectingTopicName = true;
            }
            
            return continueParsing;
        }

        @Override
        public boolean endObjectEntry() throws ParseException, IOException {
            return continueParsing;
        }

        @Override
        public boolean startArray() throws ParseException, IOException {
            if (withinTrendsObject) {
                withinHoursArray = true;
            }
            
            return continueParsing;
        }

        @Override
        public boolean endArray() throws ParseException, IOException {
            if (withinHoursArray) {
                withinHoursArray = false;
            }
            
            return continueParsing;
        }

        @Override
        public boolean primitive(Object value) throws ParseException, IOException {
            if (expectingTopicName) {
                String topicName = stripHash((String)value);
                Category category = topicCategoryService.categoryFor(topicName);
                
                trendingTopics.add(new TrendingTopic(hour, position, topicName, category));
                
                expectingTopicName = false;
            }
            return continueParsing;
        }

    }
}

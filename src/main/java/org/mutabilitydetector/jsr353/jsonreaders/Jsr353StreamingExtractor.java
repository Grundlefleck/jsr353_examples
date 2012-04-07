package org.mutabilitydetector.jsr353.jsonreaders;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.json.stream.JsonPullReader;
import javax.json.stream.JsonPullReader.Event;

import org.joda.time.DateTime;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

public final class Jsr353StreamingExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryService;

    public Jsr353StreamingExtractor(TopicCategoryService topicCategoryService) {
        this.topicCategoryService = topicCategoryService;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException {
        JsonPullReader pullReader = JsonPullReader.create(new InputStreamReader(jsonInput));

        return extractTrendsFrom(pullReader);
    }

    private Iterable<TrendingTopic> extractTrendsFrom(JsonPullReader pullReader) {
        List<TrendingTopic> trendingTopics = new ArrayList<TrendingTopic>();
        Iterator<Event> iterator = pullReader.iterator();
        
        while (iterator.next() != Event.END_OBJECT) {

            if (iterator.next() == Event.KEY_NAME) {
                if ("trends".equals(pullReader.getString())) {
                    while (iterator.next() != Event.END_OBJECT) {
                        if (iterator.next() == Event.KEY_NAME) {
                            DateTime trendDateHour = parseDateTime(pullReader.getString());
                            int position = 1;

                            while (iterator.next() != Event.END_ARRAY) {
                                while (iterator.next() != Event.END_OBJECT) {
                                    if (iterator.next() == Event.KEY_NAME) {
                                        if ("name".equals(pullReader.getString())) {
                                            if (iterator.next() == Event.VALUE_STRING) {
                                                String trendTopicName = stripHash(pullReader.getString());
                                                Category category = topicCategoryService.categoryFor(trendTopicName);
    
                                                TrendingTopic trendingTopic = new TrendingTopic(trendDateHour,
                                                                                                position,
                                                                                                trendTopicName,
                                                                                                category);
                                                trendingTopics.add(trendingTopic);
                                            }
                                        }
                                    }
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

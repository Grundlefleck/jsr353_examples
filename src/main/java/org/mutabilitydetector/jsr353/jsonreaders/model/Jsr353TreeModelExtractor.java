package org.mutabilitydetector.jsr353.jsonreaders.model;

import static org.mutabilitydetector.jsr353.HashStripper.stripHash;
import static org.mutabilitydetector.jsr353.IsoDateHourMinuteFormatter.parseDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.joda.time.DateTime;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;

public class Jsr353TreeModelExtractor implements TrendingTopicsJsonExtractor {

    private final TopicCategoryService topicCategoryService;

    public Jsr353TreeModelExtractor(TopicCategoryService topicCategoryService) {
        this.topicCategoryService = topicCategoryService;
    }

    @Override
    public Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(jsonInput));
        return extractTrendingTopicsFrom(jsonReader);

    }

    private List<TrendingTopic> extractTrendingTopicsFrom(JsonReader jsonReader) {
        JsonObject rootObject = (JsonObject) jsonReader.readObject();
        Map<String, JsonValue> trendsValues = rootObject.getValue("trends", JsonObject.class).getValues();

        List<TrendingTopic> trendingTopics = new ArrayList<>();

        for (Entry<String, JsonValue> hourOfTrends : trendsValues.entrySet()) {
            DateTime hour = parseDateTime(hourOfTrends.getKey());
            int position = 1;

            for (JsonValue trendValue : ((JsonArray) hourOfTrends.getValue())) {
                JsonObject trend = (JsonObject) trendValue;
                
                String topicName = stripHash(trend.getValue("name", JsonString.class).getValue());
                Category category = topicCategoryService.categoryFor(topicName);
                
                trendingTopics.add(new TrendingTopic(hour, position, topicName, category));
                
                position++;
            }
        }
        
        return Collections.unmodifiableList(trendingTopics);
    }

}

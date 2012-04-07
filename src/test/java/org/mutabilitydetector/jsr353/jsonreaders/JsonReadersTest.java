package org.mutabilitydetector.jsr353.jsonreaders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mutabilitydetector.jsr353.Category;
import org.mutabilitydetector.jsr353.TopicCategoryService;
import org.mutabilitydetector.jsr353.TrendingTopic;
import org.mutabilitydetector.jsr353.TrendingTopicsJsonExtractor;
import org.mutabilitydetector.jsr353.jsonreaders.model.GoogleGsonExtractor;
import org.mutabilitydetector.jsr353.jsonreaders.model.JacksonTreeModelExtractor;
import org.mutabilitydetector.jsr353.jsonreaders.model.OrgJsonExtractor;
import org.mutabilitydetector.jsr353.jsonreaders.streaming.JacksonStreamingExtractor;

@RunWith(Theories.class)
public class JsonReadersTest {

    
    private final DateTimeFormatter isoDateHourMinuteFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static final TopicCategoryService topicCategoryService = mock(TopicCategoryService.class);
    
    @DataPoints public static final TrendingTopicsJsonExtractor[] extractors = new TrendingTopicsJsonExtractor[] { 
        new OrgJsonExtractor(topicCategoryService),
        new JacksonStreamingExtractor(topicCategoryService),
        new JacksonTreeModelExtractor(topicCategoryService),
        new GoogleGsonExtractor(topicCategoryService)
        //new Jsr353Extractor(topicCategoryService) -- no implementation available
        
    };

    @Theory
    public void testJsonExtractor(TrendingTopicsJsonExtractor extractor) throws Exception {
        when(topicCategoryService.categoryFor("lazyfilms")).thenReturn(new Category("Film"));
        when(topicCategoryService.categoryFor("ThingsNotTodoAfterAbreakup")).thenReturn(new Category("Relationships"));
        when(topicCategoryService.categoryFor("Becauseofschool")).thenReturn(new Category("Education"));
        when(topicCategoryService.categoryFor("Miley Is Magnificent")).thenReturn(new Category("TV"));
        when(topicCategoryService.categoryFor("The Hangover")).thenReturn(new Category("Film"));
        
        InputStream jsonInput = getClass().getResourceAsStream("/trending-topics.json");
        
        Iterable<TrendingTopic> trendingTopics = extractor.trendingTopicsFrom(jsonInput);
        
        assertThat(trendingTopics, 
                   containsInAnyOrder(
                       new TrendingTopic(dateTime("2012-03-11 01:00"), 1, "lazyfilms", new Category("Film")),
                       new TrendingTopic(dateTime("2012-03-11 01:00"), 2, "ThingsNotTodoAfterAbreakup", new Category("Relationships")),
                       new TrendingTopic(dateTime("2012-03-11 14:00"), 1, "Becauseofschool", new Category("Education")),
                       new TrendingTopic(dateTime("2012-03-11 14:00"), 2, "Miley Is Magnificent", new Category("TV")),
                       new TrendingTopic(dateTime("2012-03-11 02:00"), 1, "lazyfilms", new Category("Film")),
                       new TrendingTopic(dateTime("2012-03-11 02:00"), 2, "ThingsNotTodoAfterAbreakup", new Category("Relationships")),
                       new TrendingTopic(dateTime("2012-03-11 02:00"), 3, "The Hangover", new Category("Film"))));
        
    }

    private DateTime dateTime(String trendTime) {
        return isoDateHourMinuteFormatter.parseDateTime(trendTime);
    }
    
}

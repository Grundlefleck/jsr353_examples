package org.mutabilitydetector.jsr353;

import java.io.IOException;
import java.io.InputStream;

public interface TrendingTopicsJsonExtractor {

    /**
     * Process the given input stream into {@link TrendingTopic}s.
     * 
     * Assumes the underlying JSON contained in the given input stream is both valid JSON, and matches the expected
     * structure. No extra error handling for the JSON format is required.
     */
    Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException;
}

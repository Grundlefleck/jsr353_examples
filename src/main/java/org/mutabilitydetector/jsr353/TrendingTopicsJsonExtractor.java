package org.mutabilitydetector.jsr353;

import java.io.IOException;
import java.io.InputStream;

public interface TrendingTopicsJsonExtractor {
    Iterable<TrendingTopic> trendingTopicsFrom(InputStream jsonInput) throws IOException;
}

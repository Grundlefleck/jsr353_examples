package org.mutabilitydetector.jsr353;

import org.joda.time.DateTime;

public class TrendingTopic {
    public final DateTime trendingDate;
    public final int position;
    public final String topic;
    public final Category category;
    
    public TrendingTopic(DateTime trendingDate, int position, String topic, Category category) {
        this.trendingDate = trendingDate;
        this.position = position;
        this.topic = topic;
        this.category = category;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + position;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        result = prime * result + ((trendingDate == null) ? 0 : trendingDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TrendingTopic other = (TrendingTopic) obj;
        if (category == null) {
            if (other.category != null) return false;
        } else if (!category.equals(other.category)) return false;
        if (position != other.position) return false;
        if (topic == null) {
            if (other.topic != null) return false;
        } else if (!topic.equals(other.topic)) return false;
        if (trendingDate == null) {
            if (other.trendingDate != null) return false;
        } else if (!trendingDate.equals(other.trendingDate)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "TrendingTopic [trendingDate=" + trendingDate
                + ", position="
                + position
                + ", topic="
                + topic
                + ", category="
                + category
                + "]";
    }
    
    
}

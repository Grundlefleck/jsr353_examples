package org.mutabilitydetector.jsr353;

public class HashStripper {
    
    private HashStripper() { }
    
    public static String stripHash(String tag) {
        return tag.startsWith("#") ? tag.substring(1) : tag;
    }
}

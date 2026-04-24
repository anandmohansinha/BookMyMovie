package com.example.movieticketbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.integration")
public class IntegrationProperties {

    private boolean redisLockCacheEnabled = true;
    private boolean kafkaBookingEventsEnabled = true;
    private String redisKeyPrefix = "bookmymovie";
    private String bookingCreatedTopic = "booking-created-events";

    public boolean isRedisLockCacheEnabled() {
        return redisLockCacheEnabled;
    }

    public void setRedisLockCacheEnabled(boolean redisLockCacheEnabled) {
        this.redisLockCacheEnabled = redisLockCacheEnabled;
    }

    public boolean isKafkaBookingEventsEnabled() {
        return kafkaBookingEventsEnabled;
    }

    public void setKafkaBookingEventsEnabled(boolean kafkaBookingEventsEnabled) {
        this.kafkaBookingEventsEnabled = kafkaBookingEventsEnabled;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public String getBookingCreatedTopic() {
        return bookingCreatedTopic;
    }

    public void setBookingCreatedTopic(String bookingCreatedTopic) {
        this.bookingCreatedTopic = bookingCreatedTopic;
    }
}

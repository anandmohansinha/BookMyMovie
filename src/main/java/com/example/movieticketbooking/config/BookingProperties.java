package com.example.movieticketbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {

    private long lockDurationMinutes = 5;
    private BigDecimal convenienceFeePerSeat = BigDecimal.valueOf(30);
    private String lockCleanupCron = "0 */1 * * * *";

    public long getLockDurationMinutes() {
        return lockDurationMinutes;
    }

    public void setLockDurationMinutes(long lockDurationMinutes) {
        this.lockDurationMinutes = lockDurationMinutes;
    }

    public BigDecimal getConvenienceFeePerSeat() {
        return convenienceFeePerSeat;
    }

    public void setConvenienceFeePerSeat(BigDecimal convenienceFeePerSeat) {
        this.convenienceFeePerSeat = convenienceFeePerSeat;
    }

    public String getLockCleanupCron() {
        return lockCleanupCron;
    }

    public void setLockCleanupCron(String lockCleanupCron) {
        this.lockCleanupCron = lockCleanupCron;
    }
}

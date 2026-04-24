package com.example.movieticketbooking.scheduler;

import com.example.movieticketbooking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LockExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(LockExpiryScheduler.class);

    private final BookingService bookingService;

    public LockExpiryScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(cron = "${app.booking.lock-cleanup-cron:0 */1 * * * *}")
    public void releaseExpiredLocks() {
        int released = bookingService.expireStaleLocks();
        if (released > 0) {
            log.info("Lock cleanup scheduler released {} expired seat locks", released);
        }
    }
}

package com.example.movieticketbooking.entity;

import com.example.movieticketbooking.enums.ShowSeatStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "show_seat_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_show_seat_inventory_show_seat", columnNames = {"show_id", "seat_id"}),
        indexes = {
                @Index(name = "idx_show_seat_inventory_show_status", columnList = "show_id, status"),
                @Index(name = "idx_show_seat_inventory_lock_reference", columnList = "lock_reference")
        }
)
public class ShowSeatInventory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id")
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShowSeatStatus status;

    @Column(length = 150)
    private String lockedBy;

    @Column(name = "lock_reference", length = 50)
    private String lockReference;

    private LocalDateTime lockExpiryTime;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getLockReference() {
        return lockReference;
    }

    public void setLockReference(String lockReference) {
        this.lockReference = lockReference;
    }

    public LocalDateTime getLockExpiryTime() {
        return lockExpiryTime;
    }

    public void setLockExpiryTime(LocalDateTime lockExpiryTime) {
        this.lockExpiryTime = lockExpiryTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

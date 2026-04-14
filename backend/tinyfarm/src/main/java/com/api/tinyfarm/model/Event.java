package com.api.tinyfarm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eid", nullable = false)
    private Long id;

    @Column(name = "uid", nullable = false)
    private Long userId;

    @Column(name = "text")
    private String text;

    @Column(name = "eventDate")
    private LocalDateTime eventDate;

    @PrePersist
    public void prePersist() {
        if (this.eventDate == null) {
            this.eventDate = LocalDateTime.now();
        }
    }
}

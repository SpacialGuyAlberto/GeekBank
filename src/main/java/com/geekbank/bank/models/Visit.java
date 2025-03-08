// Visit.java
package com.geekbank.bank.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visits") // Opcional: especifica el nombre de la tabla
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    private LocalDateTime timestamp;

    public Visit() {}

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audio_data")
public class AudioData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long durationMillis;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}


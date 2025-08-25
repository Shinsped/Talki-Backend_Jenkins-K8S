package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Entity
@Table(name = "utterance")
public class Utterance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "audio_id", nullable = false)
    private AudioData audioData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Speaker speaker;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private Long startMillis;

    @Column(nullable = false)
    private Long endMillis;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Utterance() {
        this.createdAt = LocalDateTime.now();
    }

    public Utterance(AudioData audioData, Speaker speaker, String text, Long startMillis, Long endMillis) {
        this.audioData = audioData;
        this.speaker = speaker;
        this.text = text;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.createdAt = LocalDateTime.now();
    }
}


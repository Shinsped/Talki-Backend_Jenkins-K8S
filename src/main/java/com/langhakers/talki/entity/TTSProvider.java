package com.langhakers.talki.entity;

public enum TTSProvider {
    GOOGLE_CLOUD,       // Google Cloud Text-to-Speech
    AWS_POLLY,          // Amazon Polly
    AZURE_COGNITIVE,    // Microsoft Azure Cognitive Services
    OPENAI,             // OpenAI TTS
    ELEVENLABS,         // ElevenLabs
    CUSTOM,             // Custom TTS provider
    LOCAL               // Local TTS engine
}

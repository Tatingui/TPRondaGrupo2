package com.example.tprondagrupo2.model;

/**
 * Body para hacer una pregunta o responderla: { "text": "..." }.
 */
public class TextoRequest {

    private final String text;

    public TextoRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

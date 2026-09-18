package com.ronda.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de una pregunta o de una respuesta. */
public class TextRequest {

    @NotBlank(message = "El texto no puede estar vacío")
    @Size(max = 500, message = "El texto no puede superar los 500 caracteres")
    private String text;

    public TextRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

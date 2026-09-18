package com.example.tprondagrupo2.model;

/**
 * Pregunta pública que un interesado le hace al vendedor.
 * Respuesta de GET /publications/{id}/questions.
 */
public class Pregunta {

    private Long id;
    private String text;
    private String answer;      // null si el vendedor todavía no respondió
    private String askerName;
    private String createdAt;

    public Pregunta() {
        // Constructor vacio requerido por Gson
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAskerName() {
        return askerName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean estaRespondida() {
        return answer != null && !answer.isEmpty();
    }
}

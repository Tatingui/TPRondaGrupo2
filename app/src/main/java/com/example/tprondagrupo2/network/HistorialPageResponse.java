package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.OperacionHistorial;
import java.util.List;

/**
 * Respuesta paginada del historial de operaciones (Page<TransactionDTO> de Spring).
 */
public class HistorialPageResponse {

    private List<OperacionHistorial> content;
    private int totalPages;
    private long totalElements;
    private boolean last;
    private int number;

    public List<OperacionHistorial> getContent() { return content; }
    public void setContent(List<OperacionHistorial> content) { this.content = content; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}

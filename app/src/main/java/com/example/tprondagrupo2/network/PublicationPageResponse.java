package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Publicacion;
import java.util.List;

public class PublicationPageResponse {
    private List<Publicacion> content;
    private int totalPages;
    private long totalElements;
    private boolean last;
    private int number;

    public List<Publicacion> getContent() { return content; }
    public void setContent(List<Publicacion> content) { this.content = content; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}

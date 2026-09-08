package com.example.tprondagrupo2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SavedSearch implements Serializable {
    private Long id;
    private String query;
    private Long categoryId;
    private String categoryName;
    private String condition;
    private String conditionName;
    private String location;
    private String locationName;
    private Double minPrice;
    private Double maxPrice;
    private String sort;
    private String sortName;
    private String createdAt;

    public SavedSearch() {
    }

    public SavedSearch(Long id, String query, Long categoryId, String categoryName,
                       String condition, String conditionName, String location, String locationName,
                       Double minPrice, Double maxPrice, String sort, String sortName, String createdAt) {
        this.id = id;
        this.query = query;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.condition = condition;
        this.conditionName = conditionName;
        this.location = location;
        this.locationName = locationName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sort = sort;
        this.sortName = sortName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getConditionName() { return conditionName; }
    public void setConditionName(String conditionName) { this.conditionName = conditionName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getSortName() { return sortName; }
    public void setSortName(String sortName) { this.sortName = sortName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getDisplayTitle() {
        if (query != null && !query.trim().isEmpty()) {
            return query.trim();
        }
        if (categoryName != null && !categoryName.equalsIgnoreCase("Categoría") && !categoryName.equalsIgnoreCase("Todas")) {
            return categoryName;
        }
        return "Búsqueda guardada";
    }

    public String getSummaryFilters() {
        List<String> filters = new ArrayList<>();

        if (query != null && !query.trim().isEmpty() && categoryName != null && !categoryName.equalsIgnoreCase("Categoría") && !categoryName.equalsIgnoreCase("Todas")) {
            filters.add("Categoría: " + categoryName);
        }
        if (conditionName != null && !conditionName.equalsIgnoreCase("Estado") && !conditionName.equalsIgnoreCase("Cualquiera")) {
            filters.add("Estado: " + conditionName);
        }
        if (locationName != null && !locationName.equalsIgnoreCase("Zona") && !locationName.equalsIgnoreCase("Todas")) {
            filters.add("Zona: " + locationName);
        }
        if (minPrice != null || maxPrice != null) {
            if (minPrice != null && maxPrice != null) {
                filters.add("$" + minPrice.longValue() + " - $" + maxPrice.longValue());
            } else if (minPrice != null) {
                filters.add("Desde $" + minPrice.longValue());
            } else {
                filters.add("Hasta $" + maxPrice.longValue());
            }
        }
        if (sortName != null && !sortName.equalsIgnoreCase("Ordenar por") && !sortName.equalsIgnoreCase("Recientes")) {
            filters.add("Orden: " + sortName);
        }

        if (filters.isEmpty()) {
            return "Sin filtros adicionales";
        }

        return String.join(" • ", filters);
    }

    public boolean hasAnyFilterOrQuery() {
        boolean hasQuery = query != null && !query.trim().isEmpty();
        boolean hasCategory = categoryId != null;
        boolean hasCondition = condition != null;
        boolean hasLocation = location != null;
        boolean hasPrice = minPrice != null || maxPrice != null;
        boolean hasSort = sort != null && !sort.equals("createdAt,desc");
        return hasQuery || hasCategory || hasCondition || hasLocation || hasPrice || hasSort;
    }
}

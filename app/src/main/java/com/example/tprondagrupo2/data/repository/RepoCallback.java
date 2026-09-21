package com.example.tprondagrupo2.data.repository;

public interface RepoCallback<T> {
    void onSuccess(T result);
    void onError(String message);
    default void onNetworkError() {
        onError("Error de red");
    }
}

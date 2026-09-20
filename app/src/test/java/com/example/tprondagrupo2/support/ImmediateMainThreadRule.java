package com.example.tprondagrupo2.support;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import org.junit.rules.ExternalResource;

/** Ejecuta LiveData de forma determinista en tests JVM, sin simular métodos de Android. */
public final class ImmediateMainThreadRule extends ExternalResource {
    @Override
    protected void before() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override public void executeOnDiskIO(Runnable runnable) { runnable.run(); }
            @Override public void postToMainThread(Runnable runnable) { runnable.run(); }
            @Override public boolean isMainThread() { return true; }
        });
    }

    @Override
    protected void after() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }
}

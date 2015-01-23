package com.boothj5.hedgehog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class Command<T> {
    private static final Map<String, ExecutorService> executors = new HashMap<>();

    private final Settings settings;

    public Command(Settings settings) {
        this.settings = settings;

        if (!executors.containsKey(this.settings.name)) {
            ExecutorService executor;

            if (this.settings.poolSize == -1) {
                log("Single thread execution");
                executor = Executors.newSingleThreadExecutor();
            } else {
                log("Creating thread pool, size: " + this.settings.poolSize);
                executor = Executors.newFixedThreadPool(this.settings.poolSize);
            }

            executors.put(this.settings.name, executor);
        }
    }

    public Command() {
        this.settings = new Settings(this.getClass().getSimpleName(), true, 1000L, -1);
    }

    protected abstract T run() throws Exception;

    protected T fallback() {
        throw new UnsupportedOperationException("Fallback not implemented");
    }

    public final T execute() {
        log("execute()");
        ExecutorService executor = executors.get(settings.name);
        if (settings.poolSize != -1) {
            log("Pool size: " + ((ThreadPoolExecutor) executor).getPoolSize() + ", active: " + ((ThreadPoolExecutor) executor).getPoolSize());
        }

        Future<T> future = executor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                log("run()");
                return run();
            }
        });

        try {
            return future.get(settings.timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log("execution failed, exception: " + e);
            if (settings.fallbackEnabled) {
                log("fallback()");
                return fallback();
            } else {
                log("fallback disabled");
                throw new HegdehogRuntimeException(e);
            }
        }
    }

    public int getPoolSize() {
        if (settings.poolSize != -1) {
            ThreadPoolExecutor executorService = (ThreadPoolExecutor) executors.get(settings.name);
            return executorService.getPoolSize();
        } else {
            return 0;
        }
    }

    public int getActiveThreads() {
        if (settings.poolSize != -1) {
            ThreadPoolExecutor executorService = (ThreadPoolExecutor) executors.get(settings.name);
            return executorService.getActiveCount();
        } else {
            return 0;
        }
    }

    private void log(String msg)
    {
        System.out.println("[" + Thread.currentThread().getName() + "] - " + settings.name + ": " + msg);
    }
}

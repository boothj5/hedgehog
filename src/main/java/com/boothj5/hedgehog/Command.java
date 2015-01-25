package com.boothj5.hedgehog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class Command<T> {

    private static final Map<String, ThreadPoolExecutor> executors =
            Collections.synchronizedMap(new HashMap<String, ThreadPoolExecutor>());

    private final Settings settings;

    public Command(Settings settings) {
        this.settings = settings;
        addExecutor();
    }

    public Command() {
        this.settings = new Settings(this.getClass().getName(), true, 1000L, -1, -1);
        addExecutor();
    }

    public final T execute() {
        ThreadPoolExecutor executor = executors.get(settings.name);
        log("execute()");
        log("Pool size: " + executor.getPoolSize() + ", active: " + executor.getPoolSize());
        log("Queue size: " + executor.getQueue().size() + ", remaining: " + executor.getQueue().remainingCapacity());

        try {

            Future<T> future = executor.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    log("run()");
                    return run();
                }
            });
            return future.get(settings.timeoutMillis, TimeUnit.MILLISECONDS);

        } catch (InterruptedException | ExecutionException | TimeoutException | RejectedExecutionException e) {
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
        ThreadPoolExecutor executorService = executors.get(settings.name);
        return executorService.getPoolSize();
    }

    public int getActiveThreads() {
        ThreadPoolExecutor executorService = executors.get(settings.name);
        return executorService.getActiveCount();
    }

    public int getQueueSize() {
        ThreadPoolExecutor executorService = executors.get(settings.name);
        return executorService.getQueue().size();
    }

    public int getQueueRemaining() {
        ThreadPoolExecutor executorService = executors.get(settings.name);
        return executorService.getQueue().remainingCapacity();
    }

    protected abstract T run() throws Exception;

    protected T fallback() {
        throw new UnsupportedOperationException("Fallback not implemented");
    }

    private void addExecutor() {
        if (!executors.containsKey(this.settings.name)) {

            LinkedBlockingQueue<Runnable> queue;
            if (this.settings.queueSize == -1) {
                queue = new LinkedBlockingQueue<>();
            } else {
                queue = new LinkedBlockingQueue<>(this.settings.queueSize);
            }

            ThreadPoolExecutor executor;
            if (this.settings.poolSize == -1) {
                log("Single thread execution");
                executor = new ThreadPoolExecutor(
                        1,
                        1,
                        0L, TimeUnit.MILLISECONDS,
                        queue);
            } else {
                log("Creating thread pool, size: " + this.settings.poolSize);
                executor = new ThreadPoolExecutor(
                        this.settings.poolSize,
                        this.settings.poolSize,
                        0L, TimeUnit.MILLISECONDS,
                        queue);
            }

            executors.put(this.settings.name, executor);
        } else {
            throw new HegdehogRuntimeException("Command with key " + settings.name + " already exists");
        }
    }

    private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] - " + settings.name + ": " + msg);
    }
}

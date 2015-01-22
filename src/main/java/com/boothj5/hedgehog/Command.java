package com.boothj5.hedgehog;

import java.util.concurrent.*;

public abstract class Command<T> {
    private final Settings settings;

    public Command(Settings settings) {
        this.settings = settings;
    }

    public Command() {
        this.settings = new Settings(this.getClass().getSimpleName(), true, 1000L);
    }

    protected abstract T run() throws Exception;

    protected T fallback() {
        throw new UnsupportedOperationException("Fallback not implemented");
    }

    public final T execute() {
        log("execute()");
        ExecutorService executor = Executors.newSingleThreadExecutor();

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

    private void log(String msg)
    {
        System.out.println("[" + Thread.currentThread().getName() + "] - " + settings.name + ": " + msg);
    }
}

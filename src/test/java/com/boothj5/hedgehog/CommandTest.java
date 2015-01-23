package com.boothj5.hedgehog;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

public class CommandTest {

    @Test
    public void executeReturnsRunResult() {
        Settings settings = new SettingsBuilder()
                .withName("SUCCESS")
                .build();

        Command<String> command = new Command<String>(settings) {
            @Override
            public String run() {
                return "hello";
            }
        };

        String result = command.execute();
        assertEquals("hello", result);
    }

    @Test
    public void executeReturnsFallbackResultOnException() {
        Settings settings = new SettingsBuilder()
                .withName("FALLBACK-ENABLED")
                .build();

        Command<String> command = new Command<String>(settings) {
            @Override
            public String run() {
                throw new RuntimeException();
            }
            @Override
            public String fallback() {
                return "fail";
            }
        };

        String result = command.execute();
        assertEquals("fail", result);
    }

    @Test
    public void fallbackDisabled() {
        Settings settings = new SettingsBuilder()
                .withName("FALLBACK-DISABLED")
                .disableFallback()
                .build();

        Command<String> command = new Command<String>(settings) {
            @Override
            public String run() {
                throw new RuntimeException();
            }
            @Override
            public String fallback() {
                return "fail";
            }
        };

        try {
            command.execute();
            fail("Expected HedgehogRuntimeException");
        } catch (Exception e) {
            assertTrue(e instanceof HegdehogRuntimeException);
        }
    }

    @Test
    public void timeout() {
        Settings settings = new SettingsBuilder()
                .withName("TIMEOUT")
                .withTimeoutMillis(10)
                .build();

        Command<String> command = new Command<String>(settings) {
            @Override
            public String run() throws InterruptedException {
                Thread.sleep(100);
                return "done";
            }
            @Override
            public String fallback() {
                return "fail";
            }
        };

        String result = command.execute();
        assertEquals("fail", result);
    }

    @Test
    public void poolDefault() throws InterruptedException {
        Settings settings = new SettingsBuilder()
                .withName("POOLDEFAULT")
                .build();

        final Command<String> command = new Command<String>(settings) {
            @Override
            protected String run() throws Exception {
                Thread.sleep(200);
                return "done";
            }
        };

        int poolSize = command.getPoolSize();
        int activeThreads = command.getActiveThreads();
        assertEquals(0, poolSize);
        assertEquals(0, activeThreads);

        new Thread(new Runnable() {
            @Override
            public void run() {
                command.execute();
            }
        }).start();

        Thread.sleep(50);

        poolSize = command.getPoolSize();
        activeThreads = command.getActiveThreads();
        assertEquals(0, poolSize);
        assertEquals(0, activeThreads);

        Thread.sleep(200);

        poolSize = command.getPoolSize();
        activeThreads = command.getActiveThreads();
        assertEquals(0, poolSize);
        assertEquals(0, activeThreads);
    }

    @Test
    public void poolDebugMax() throws InterruptedException {
        Settings settings = new SettingsBuilder()
                .withName("SLEEPER")
                .withThreadPool(5)
                .build();

        final Command<String> command = new Command<String>(settings) {
            @Override
            protected String run() throws Exception {
                Thread.sleep(1000);
                return "done";
            }
        };

        int poolSize = command.getPoolSize();
        int activeThreads = command.getActiveThreads();
        assertEquals(0, poolSize);
        assertEquals(0, activeThreads);

        for (int runs = 0; runs < 10; runs++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    command.execute();
                }
            }).start();
            Thread.sleep(20);
        }

        Thread.sleep(50);

        poolSize = command.getPoolSize();
        activeThreads = command.getActiveThreads();
        assertEquals(5, poolSize);
        assertEquals(5, activeThreads);
    }
}
package com.boothj5.hedgehog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;

public class CommandTest {

    private AtomicInteger executionCounter = new AtomicInteger(0);

    @Before
    public void setup() {
        executionCounter.set(0);
    }

    @After
    public void tearDown() {
        executionCounter.set(0);
    }

    @Test
    public void executeReturnsRunResult() {
        Command<String> command = new Command<String>() {
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
        Command<String> command = new Command<String>() {
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

    @Test(expected = HegdehogRuntimeException.class)
    public void duplicateKeyThrowsException() {
        Settings settings = new SettingsBuilder()
                .withName("DUPLICATE")
                .build();

        new Command<String>(settings) {
            @Override
            public String run() {
                return "pass";
            }
            @Override
            public String fallback() {
                return "fail";
            }
        };

        new Command<String>(settings) {
            @Override
            public String run() {
                return "pass";
            }
            @Override
            public String fallback() {
                return "fail";
            }
        };
    }

    @Test(expected = HegdehogRuntimeException.class)
    public void fallbackDisabled() {
        Settings settings = new SettingsBuilder()
                .withName("FALLBACK-DISABLED")
                .withFallbackDisabled()
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

        command.execute();
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
    public void poolDefaultIsOne() throws InterruptedException {
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

        assertEquals(0, command.getPoolSize());
        assertEquals(0, command.getActiveThreads());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                command.execute();
            }
        });
        t.start();

        Thread.sleep(50);

        assertEquals(1, command.getPoolSize());
        assertEquals(1, command.getActiveThreads());

        t.join();

        assertEquals(1, command.getPoolSize());
        assertEquals(0, command.getActiveThreads());
    }

    @Test
    public void poolDefaultMakesSecondThreadWait() throws InterruptedException {
        executionCounter.set(0);

        final Command<String> command = new Command<String>(new SettingsBuilder()
                .withName("POOLDEFAULTWAIT")
                .build()) {
            @Override
            protected String run() throws Exception {
                executionCounter.incrementAndGet();
                Thread.sleep(200);
                return "done";
            }
        };

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                command.execute();
            }
        });
        t1.start();

        Thread.sleep(50);

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                command.execute();
            }
        });
        t2.start();

        Thread.sleep(50);

        assertEquals(1, executionCounter.get());

        t1.join();
        t2.join();

        assertEquals(2, executionCounter.get());
    }

    @Test
    public void poolDebugMax() throws InterruptedException {

        final Command<String> command = new Command<String>(new SettingsBuilder()
                .withName("SLEEPER")
                .withThreadPool(5)
                .withTimeoutMillis(10000)
                .build()) {
            @Override
            protected String run() throws Exception {
                Thread.sleep(1000);
                return "done";
            }
        };

        assertEquals(0, command.getPoolSize());
        assertEquals(0, command.getActiveThreads());

        List<Thread> threads = new ArrayList<>();

        for (int runs = 0; runs < 10; runs++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    command.execute();
                }
            }));
            threads.get(runs).start();
            Thread.sleep(20);
        }

        Thread.sleep(50);

        assertEquals(5, command.getPoolSize());
        assertEquals(5, command.getActiveThreads());

        for (int runs = 0; runs < 10; runs++) {
            threads.get(runs).join();
        }

        assertEquals(5, command.getPoolSize());
        assertEquals(0, command.getActiveThreads());
    }

    @Test(expected = HegdehogRuntimeException.class)
    public void throwsRuntimeExceptionWhenQueueFull() throws InterruptedException {
        final Command<String> command = new Command<String>(new SettingsBuilder()
                .withName("QUEUEREJECTION")
                .withQueue(1)
                .withFallbackDisabled()
                .build()) {
            @Override
            protected String run() throws Exception {
                Thread.sleep(100);
                return "done";
            }
        };

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                command.execute();
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                command.execute();
            }
        });

        t1.start();
        Thread.sleep(20);
        t2.start();
        Thread.sleep(20);

        command.execute();
    }
}
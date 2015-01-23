package com.boothj5.hedgehog;

class Settings {
    final String name;
    final boolean fallbackEnabled;
    final long timeoutMillis;
    final int poolSize;

    Settings(String name, boolean fallbackEnabled, long timeoutMillis, int poolSize) {
        this.name = name;
        this.fallbackEnabled = fallbackEnabled;
        this.timeoutMillis = timeoutMillis;
        this.poolSize = poolSize;
    }
}

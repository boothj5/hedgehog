package com.boothj5.hedgehog;

class Settings {
    final String name;
    final boolean fallbackEnabled;
    public long timeoutMillis;

    Settings(String name, boolean fallbackEnabled, long timeoutMillis) {
        this.name = name;
        this.fallbackEnabled = fallbackEnabled;
        this.timeoutMillis = timeoutMillis;
    }
}

package com.boothj5.hedgehog;

public class SettingsBuilder {
    private boolean fallbackEnabled = true;
    private Long timeoutMillis = null;
    private String name;
    private Integer poolSize;

    public SettingsBuilder disableFallback() { this.fallbackEnabled = false; return this; }
    public SettingsBuilder withTimeoutMillis(long timeoutMillis) { this.timeoutMillis = timeoutMillis; return this; }
    public SettingsBuilder withName(String name) { this.name = name; return this; }
    public SettingsBuilder withThreadPool(int poolSize) { this.poolSize = poolSize; return this; }

    public Settings build() {
        if (this.timeoutMillis == null) {
            this.timeoutMillis = 1000L;
        }
        if (poolSize == null) {
            poolSize = -1;
        }

        return new Settings(name, fallbackEnabled, timeoutMillis, poolSize);
    }
}

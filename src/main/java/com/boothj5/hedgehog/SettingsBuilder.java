package com.boothj5.hedgehog;

public class SettingsBuilder {
    protected boolean fallbackEnabled = true;
    private Long timeoutMillis = null;
    private String name;

    public SettingsBuilder disableFallback() { this.fallbackEnabled = false; return this; }
    public SettingsBuilder withTimeoutMillis(long timeoutMillis) { this.timeoutMillis = timeoutMillis; return this; }
    public SettingsBuilder withName(String name) { this.name = name; return this;}

    public Settings build() {
        if (this.timeoutMillis == null) {
            this.timeoutMillis = 1000L;
        }

        return new Settings(name, fallbackEnabled, timeoutMillis);
    }
}

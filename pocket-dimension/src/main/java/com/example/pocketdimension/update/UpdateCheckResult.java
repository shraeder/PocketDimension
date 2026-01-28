package com.example.pocketdimension.update;

public class UpdateCheckResult {
    private final String latestVersion;
    private final String downloadUrl;

    public UpdateCheckResult(String latestVersion, String downloadUrl) {
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}

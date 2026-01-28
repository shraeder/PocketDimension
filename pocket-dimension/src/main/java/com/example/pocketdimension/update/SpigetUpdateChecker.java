package com.example.pocketdimension.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigetUpdateChecker {
    private static final Pattern NAME_FIELD = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    private final int resourceId;
    private final String downloadUrl;

    public SpigetUpdateChecker(int resourceId, String downloadUrl) {
        this.resourceId = resourceId;
        this.downloadUrl = downloadUrl;
    }

    public UpdateCheckResult fetchLatest() throws Exception {
        URL url = new URL("https://api.spiget.org/v2/resources/" + resourceId + "/versions/latest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "PocketDimension-UpdateChecker");

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("Spiget HTTP " + code);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String body = sb.toString();
        Matcher m = NAME_FIELD.matcher(body);
        if (!m.find()) {
            throw new IllegalStateException("Could not parse latest version from Spiget response");
        }

        String latest = m.group(1).trim();
        return new UpdateCheckResult(latest, downloadUrl);
    }
}

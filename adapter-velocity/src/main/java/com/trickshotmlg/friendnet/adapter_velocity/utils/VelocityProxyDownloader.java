package com.trickshotmlg.friendnet.adapter_velocity.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VelocityProxyDownloader {

    private static final String BUILDS_ENDPOINT = "https://fill.papermc.io/v3/projects/velocity/versions/%s/builds";
    private static final Pattern STABLE_PROXY_DOWNLOAD_PATTERN = Pattern.compile(
            "\"channel\"\\s*:\\s*\"STABLE\".*?\"server:default\"\\s*:\\s*\\{.*?\"url\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.DOTALL
    );

    private VelocityProxyDownloader() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: VelocityProxyDownloader <velocityVersion> <proxyJar> <force> <userAgent>");
        }

        String velocityVersion = args[0];
        Path proxyJar = Path.of(args[1]);
        boolean force = Boolean.parseBoolean(args[2]);
        String userAgent = args[3];

        String downloadUrl = findLatestStableDownloadUrl(velocityVersion, userAgent);
        Path markerFile = proxyJar.resolveSibling(proxyJar.getFileName() + ".url");

        if (!force && Files.exists(proxyJar) && Files.exists(markerFile)) {
            String previousUrl = Files.readString(markerFile).trim();
            if (downloadUrl.equals(previousUrl)) {
                System.out.println("Velocity proxy is already up to date: " + proxyJar);
                return;
            }
        }

        Files.createDirectories(proxyJar.getParent());
        System.out.println("Downloading Velocity " + velocityVersion + " from " + downloadUrl);
        download(downloadUrl, proxyJar, userAgent);
        Files.writeString(markerFile, downloadUrl);
        System.out.println("Downloaded Velocity proxy to " + proxyJar);
    }

    private static String findLatestStableDownloadUrl(String velocityVersion, String userAgent) throws IOException {
        String buildsJson = readString(String.format(BUILDS_ENDPOINT, velocityVersion), userAgent);
        Matcher matcher = STABLE_PROXY_DOWNLOAD_PATTERN.matcher(buildsJson);

        if (!matcher.find()) {
            throw new IOException("No stable Velocity proxy build found for version " + velocityVersion);
        }

        return unescapeJson(matcher.group(1));
    }

    private static String readString(String url, String userAgent) throws IOException {
        HttpURLConnection connection = openConnection(url, userAgent);

        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    private static void download(String url, Path destination, String userAgent) throws IOException {
        HttpURLConnection connection = openConnection(url, userAgent);

        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection openConnection(String url, String userAgent) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("HTTP " + responseCode + " while requesting " + url);
        }

        return connection;
    }

    private static String unescapeJson(String value) {
        return value.replace("\\/", "/").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}

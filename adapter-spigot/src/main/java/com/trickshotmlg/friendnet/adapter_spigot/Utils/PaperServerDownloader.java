package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PaperServerDownloader {

    private static final String BUILDS_ENDPOINT = "https://fill.papermc.io/v3/projects/paper/versions/%s/builds";
    private static final Pattern STABLE_SERVER_DOWNLOAD_PATTERN = Pattern.compile(
            "\"channel\"\\s*:\\s*\"STABLE\".*?\"server:default\"\\s*:\\s*\\{.*?\"url\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.DOTALL
    );

    private PaperServerDownloader() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: PaperServerDownloader <minecraftVersion> <serverJar> <force> <userAgent>");
        }

        String minecraftVersion = args[0];
        Path serverJar = Path.of(args[1]);
        boolean force = Boolean.parseBoolean(args[2]);
        String userAgent = args[3];

        String downloadUrl = findLatestStableDownloadUrl(minecraftVersion, userAgent);
        Path markerFile = serverJar.resolveSibling(serverJar.getFileName() + ".url");

        if (!force && Files.exists(serverJar) && Files.exists(markerFile)) {
            String previousUrl = Files.readString(markerFile).trim();
            if (downloadUrl.equals(previousUrl)) {
                System.out.println("Paper server is already up to date: " + serverJar);
                return;
            }
        }

        Files.createDirectories(serverJar.getParent());
        System.out.println("Downloading Paper " + minecraftVersion + " from " + downloadUrl);
        download(downloadUrl, serverJar, userAgent);
        Files.writeString(markerFile, downloadUrl);
        System.out.println("Downloaded Paper server to " + serverJar);
    }

    private static String findLatestStableDownloadUrl(String minecraftVersion, String userAgent) throws IOException {
        String buildsJson = readString(String.format(BUILDS_ENDPOINT, minecraftVersion), userAgent);
        Matcher matcher = STABLE_SERVER_DOWNLOAD_PATTERN.matcher(buildsJson);

        if (!matcher.find()) {
            throw new IOException("No stable Paper server build found for Minecraft " + minecraftVersion);
        }

        return unescapeJson(matcher.group(1));
    }

    private static String readString(String url, String userAgent) throws IOException {
        HttpURLConnection connection = openConnection(url, userAgent);

        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes());
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

package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModrinthPluginDownloader {

    private static final String PROJECT_VERSION_ENDPOINT =
            "https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s";
    private static final Pattern PRIMARY_FILE_URL_PATTERN = Pattern.compile(
            "\"files\"\\s*:\\s*\\[\\s*\\{.*?\"url\"\\s*:\\s*\"([^\"]+)\".*?\"primary\"\\s*:\\s*true",
            Pattern.DOTALL
    );

    private ModrinthPluginDownloader() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 6) {
            throw new IllegalArgumentException("Usage: ModrinthPluginDownloader <projectSlug> <loader> <gameVersion> <destinationJar> <force> <userAgent>");
        }

        String projectSlug = args[0];
        String loader = args[1];
        String gameVersion = args[2];
        Path destinationJar = Path.of(args[3]);
        boolean force = Boolean.parseBoolean(args[4]);
        String userAgent = args[5];

        String downloadUrl = findLatestPrimaryFileUrl(projectSlug, loader, gameVersion, userAgent);
        Path markerFile = destinationJar.resolveSibling(destinationJar.getFileName() + ".url");

        if (!force && Files.exists(destinationJar) && Files.exists(markerFile)) {
            String previousUrl = Files.readString(markerFile).trim();
            if (downloadUrl.equals(previousUrl)) {
                System.out.println(projectSlug + " is already up to date: " + destinationJar);
                return;
            }
        }

        Files.createDirectories(destinationJar.getParent());
        System.out.println("Downloading " + projectSlug + " from " + downloadUrl);
        download(downloadUrl, destinationJar, userAgent);
        Files.writeString(markerFile, downloadUrl);
        System.out.println("Downloaded " + projectSlug + " to " + destinationJar);
    }

    private static String findLatestPrimaryFileUrl(String projectSlug, String loader, String gameVersion, String userAgent) throws IOException {
        String encodedProject = URLEncoder.encode(projectSlug, StandardCharsets.UTF_8);
        String encodedLoader = URLEncoder.encode("[\"" + loader + "\"]", StandardCharsets.UTF_8);
        String encodedGameVersion = URLEncoder.encode("[\"" + gameVersion + "\"]", StandardCharsets.UTF_8);
        String url = String.format(PROJECT_VERSION_ENDPOINT, encodedProject, encodedLoader, encodedGameVersion);
        String versionsJson = readString(url, userAgent);
        Matcher matcher = PRIMARY_FILE_URL_PATTERN.matcher(versionsJson);

        if (!matcher.find()) {
            throw new IOException("No primary Modrinth file found for " + projectSlug + " " + loader + " " + gameVersion);
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

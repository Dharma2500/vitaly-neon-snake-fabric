package ru.vitaly.neonsnake;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Leaderboard {
    private static final int LIMIT = 10;

    private final Path file = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("neonsnake")
            .resolve("leaderboard.tsv");

    private final List<ScoreEntry> entries = new ArrayList<>();

    public Leaderboard() {
        load();
    }

    public synchronized void updateRun(String runId, int score) {
        if (score <= 0) {
            return;
        }

        entries.removeIf(entry -> entry.runId().equals(runId));
        entries.add(new ScoreEntry(runId, score, System.currentTimeMillis()));
        entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());

        if (entries.size() > LIMIT) {
            entries.subList(LIMIT, entries.size()).clear();
        }

        save();
    }

    public synchronized List<ScoreEntry> entries() {
        return List.copyOf(entries);
    }

    public synchronized int bestScore() {
        return entries.isEmpty() ? 0 : entries.getFirst().score();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\t");
                if (parts.length != 3) {
                    continue;
                }

                try {
                    entries.add(new ScoreEntry(
                            parts[0],
                            Integer.parseInt(parts[1]),
                            Long.parseLong(parts[2])
                    ));
                } catch (NumberFormatException ignored) {
                    // Ignore a damaged row, not the whole leaderboard.
                }
            }

            entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());
            if (entries.size() > LIMIT) {
                entries.subList(LIMIT, entries.size()).clear();
            }
        } catch (IOException exception) {
            System.err.println("[NEON SNAKE] Could not read leaderboard: " + exception.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = entries.stream()
                    .map(entry -> entry.runId() + "\t" + entry.score() + "\t" + entry.timestamp())
                    .toList();
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("[NEON SNAKE] Could not save leaderboard: " + exception.getMessage());
        }
    }

    public record ScoreEntry(String runId, int score, long timestamp) {
    }
}

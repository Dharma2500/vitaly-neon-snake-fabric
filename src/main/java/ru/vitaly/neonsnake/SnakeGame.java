package ru.vitaly.neonsnake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class SnakeGame {
    public static final int GRID_WIDTH = 30;
    public static final int GRID_HEIGHT = 16;

    private final Leaderboard leaderboard;
    private final Random random = new Random();

    private final List<Cell> snake = new ArrayList<>();
    private List<Cell> previousSnake = new ArrayList<>();

    private Cell food;
    private Cell bonus;

    private Direction direction = Direction.RIGHT;
    private Direction queuedDirection = Direction.RIGHT;

    private int score;
    private boolean started;
    private boolean running;
    private boolean boardComplete;

    private long lastStepAt;
    private long bonusSpawnAt;
    private long bonusExpiresAt;

    private String runId = UUID.randomUUID().toString();

    public SnakeGame(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        reset(false);
    }

    public void startOrResume() {
        if (!started) {
            reset(false);
        }

        started = true;
        running = !boardComplete;
        lastStepAt = System.currentTimeMillis();
    }

    public void pause() {
        running = false;
    }

    public void newGame() {
        reset(true);
    }

    public void setDirection(Direction requested) {
        if (requested == direction.opposite() || requested == queuedDirection.opposite()) {
            return;
        }
        queuedDirection = requested;
    }

    public void tick(long now) {
        if (!running) {
            return;
        }

        if (bonus != null && now >= bonusExpiresAt) {
            bonus = null;
            bonusSpawnAt = now + 8_000L + random.nextInt(8_001);
        }

        if (bonus == null && now >= bonusSpawnAt) {
            spawnBonus(now);
        }

        int safety = 0;
        long interval = speedMillis();
        while (running && now - lastStepAt >= interval && safety++ < 5) {
            lastStepAt += interval;
            advance(now);
            interval = speedMillis();
        }
    }

    private void reset(boolean autoStart) {
        snake.clear();
        int centerX = GRID_WIDTH / 2;
        int centerY = GRID_HEIGHT / 2;

        snake.add(new Cell(centerX, centerY));
        snake.add(new Cell(centerX - 1, centerY));
        snake.add(new Cell(centerX - 2, centerY));

        previousSnake = new ArrayList<>(snake);
        direction = Direction.RIGHT;
        queuedDirection = Direction.RIGHT;
        score = 0;
        started = autoStart;
        running = autoStart;
        boardComplete = false;
        runId = UUID.randomUUID().toString();

        long now = System.currentTimeMillis();
        lastStepAt = now;
        bonus = null;
        food = null;
        bonusSpawnAt = now + 12_000L + random.nextInt(9_001);
        bonusExpiresAt = 0L;
        spawnFood();
    }

    private void advance(long now) {
        direction = queuedDirection;

        Cell head = snake.getFirst();
        Cell nextHead = new Cell(
                Math.floorMod(head.x() + direction.dx, GRID_WIDTH),
                Math.floorMod(head.y() + direction.dy, GRID_HEIGHT)
        );

        previousSnake = new ArrayList<>(snake);
        int collisionIndex = snake.indexOf(nextHead);
        snake.addFirst(nextHead);

        boolean ateFood = nextHead.equals(food);
        boolean ateBonus = bonus != null && nextHead.equals(bonus);

        if (collisionIndex >= 0) {
            // The part of the snake behind the impact point is shed.
            // Touching the tail is harmless: it moves into the old tail cell.
            int keepLength = collisionIndex + 1;
            while (snake.size() > keepLength) {
                snake.removeLast();
            }
        } else if (!ateFood && !ateBonus) {
            snake.removeLast();
        }

        if (ateFood) {
            score += 10;
            spawnFood();
            updateRecord();
        }

        if (ateBonus) {
            score += 30;
            bonus = null;
            bonusSpawnAt = now + 13_000L + random.nextInt(10_001);
            updateRecord();
        }
    }

    private void spawnFood() {
        food = randomFreeCell();
        if (food == null) {
            boardComplete = true;
            running = false;
        }
    }

    private void spawnBonus(long now) {
        bonus = randomFreeCell();
        if (bonus == null) {
            bonusSpawnAt = now + 5_000L;
            return;
        }
        bonusExpiresAt = now + 7_000L;
        bonusSpawnAt = Long.MAX_VALUE;
    }

    private Cell randomFreeCell() {
        List<Cell> free = new ArrayList<>();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                Cell candidate = new Cell(x, y);
                if (!snake.contains(candidate) && !candidate.equals(food) && !candidate.equals(bonus)) {
                    free.add(candidate);
                }
            }
        }

        return free.isEmpty() ? null : free.get(random.nextInt(free.size()));
    }

    private void updateRecord() {
        leaderboard.updateRun(runId, score);
    }

    public long speedMillis() {
        return Math.max(65L, 180L - (score / 50L) * 10L);
    }

    public int level() {
        return 1 + score / 50;
    }

    public float interpolation(long now) {
        if (!running) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (float) (now - lastStepAt) / speedMillis()));
    }

    public List<Cell> snake() {
        return List.copyOf(snake);
    }

    public List<Cell> previousSnake() {
        return List.copyOf(previousSnake);
    }

    public Cell food() {
        return food;
    }

    public Cell bonus() {
        return bonus;
    }

    public int score() {
        return score;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isBoardComplete() {
        return boardComplete;
    }

    public int bestScore() {
        return leaderboard.bestScore();
    }

    public List<Leaderboard.ScoreEntry> leaderboard() {
        return leaderboard.entries();
    }

    public enum Direction {
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        LEFT(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public Direction opposite() {
            return switch (this) {
                case UP -> DOWN;
                case RIGHT -> LEFT;
                case DOWN -> UP;
                case LEFT -> RIGHT;
            };
        }
    }

    public record Cell(int x, int y) {
    }
}

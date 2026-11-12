package ru.vitaly.neonsnake;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class SnakeScreen extends Screen {
    private static final int CYAN = 0xFF42E8F5;
    private static final int CYAN_DIM = 0xFF1A8295;
    private static final int PINK = 0xFFFF5CCB;
    private static final int PURPLE = 0xFFB38CFF;
    private static final int TEXT = 0xFFE8F7FF;
    private static final int MUTED = 0xFF7895A8;

    private static final ItemStack HEAD_ITEM = new ItemStack(Items.SEA_LANTERN);
    private static final ItemStack BODY_ITEM = new ItemStack(Items.PRISMARINE_BRICKS);
    private static final ItemStack FOOD_ITEM = new ItemStack(Items.REDSTONE_LAMP);
    private static final ItemStack BONUS_ITEM = new ItemStack(Items.AMETHYST_BLOCK);

    private final SnakeGame game = NeonSnakeClient.GAME;

    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;

    private int boardX;
    private int boardY;
    private int boardWidth;
    private int boardHeight;
    private int sideX;
    private int sideWidth;
    private int footerY;

    private int buttonY;
    private int startButtonWidth;
    private int newButtonX;
    private int newButtonWidth;

    private ButtonWidget startButton;
    private ButtonWidget newGameButton;

    public SnakeScreen() {
        super(Text.literal("NEON SNAKE"));
    }

    @Override
    protected void init() {
        windowWidth = Math.min(width - 20, Math.max(360, (int) (width * 0.56f)));
        windowHeight = Math.min(height - 20, Math.max(280, (int) (height * 0.48f)));

        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;

        int padding = 18;
        int sideGap = 10;
        sideWidth = Math.min(170, Math.max(110, windowWidth / 4));
        boardX = windowX + padding;
        boardY = windowY + 91;
        footerY = windowY + windowHeight - 50;

        sideX = windowX + windowWidth - padding - sideWidth;
        boardWidth = sideX - sideGap - boardX;
        boardHeight = Math.max(90, footerY - boardY - 8);

        int usableButtonWidth = windowWidth - padding * 2;
        int gap = 10;
        startButtonWidth = Math.max(120, (usableButtonWidth - gap) * 57 / 100);
        newButtonX = windowX + padding + startButtonWidth + gap;
        newButtonWidth = usableButtonWidth - startButtonWidth - gap;
        buttonY = windowY + windowHeight - 43;

        startButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
                    if (game.isRunning()) {
                        game.pause();
                    } else {
                        game.startOrResume();
                    }
                })
                .dimensions(windowX + padding, buttonY, startButtonWidth, 27)
                .build());

        newGameButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> game.newGame())
                .dimensions(newButtonX, buttonY, newButtonWidth, 27)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
        game.tick(System.currentTimeMillis());
    }

    @Override
    public void removed() {
        game.pause();
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            game.pause();
            close();
            return true;
        }

        if (key == GLFW.GLFW_KEY_SPACE) {
            if (game.isRunning()) {
                game.pause();
            } else {
                game.startOrResume();
            }
            return true;
        }

        SnakeGame.Direction direction = switch (key) {
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> SnakeGame.Direction.UP;
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> SnakeGame.Direction.RIGHT;
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> SnakeGame.Direction.DOWN;
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> SnakeGame.Direction.LEFT;
            default -> null;
        };

        if (direction != null) {
            game.setDirection(direction);
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        context.fill(0, 0, width, height, 0xA9000710);
        drawWindow(context);
        drawHeader(context);
        drawStats(context);
        drawBoard(context);
        drawLeaderboard(context);
        drawButtons(context);
        drawFooter(context);
    }

    private void drawWindow(DrawContext context) {
        int right = windowX + windowWidth;
        int bottom = windowY + windowHeight;

        context.fill(windowX - 5, windowY - 5, right + 5, bottom + 5, 0x4034DDF5);
        context.fill(windowX, windowY, right, bottom, 0xF008111D);

        fillBorder(context, windowX, windowY, windowWidth, windowHeight, CYAN_DIM);
        context.fill(windowX + 2, windowY + 2, right - 2, windowY + 3, CYAN);
        context.fill(windowX + 2, bottom - 3, right - 2, bottom - 2, 0xFF382C62);

        // Small corner cuts give the panel a compact sci-fi console silhouette.
        context.fill(windowX, windowY, windowX + 14, windowY + 3, CYAN);
        context.fill(right - 14, bottom - 3, right, bottom, PINK);
    }

    private void drawHeader(DrawContext context) {
        int x = windowX + 18;
        int y = windowY + 12;

        context.drawText(textRenderer, Text.literal("NEON SNAKE"), x, y, CYAN, true);
        context.drawText(textRenderer, Text.literal("ARCADE SYSTEM  //  001"), x + 93, y + 1, MUTED, false);

        String status = game.isBoardComplete()
                ? "GRID COMPLETE"
                : game.isRunning() ? "RUNNING" : game.isStarted() ? "PAUSED" : "READY";
        int statusColor = game.isRunning() ? CYAN : game.isBoardComplete() ? PINK : PURPLE;
        int statusWidth = textRenderer.getWidth(status) + 16;
        int statusX = windowX + windowWidth - 18 - statusWidth;

        context.fill(statusX, y - 3, statusX + statusWidth, y + 13, 0xFF102331);
        fillBorder(context, statusX, y - 3, statusWidth, 16, statusColor);
        context.drawCenteredTextWithShadow(textRenderer, status, statusX + statusWidth / 2, y, statusColor);
    }

    private void drawStats(DrawContext context) {
        int gap = 8;
        int x = windowX + 18;
        int y = windowY + 39;
        int totalWidth = windowWidth - 36;
        int cardWidth = (totalWidth - gap * 2) / 3;

        drawStat(context, x, y, cardWidth, "SCORE", String.format("%04d", game.score()), CYAN);
        drawStat(context, x + cardWidth + gap, y, cardWidth, "BEST", String.format("%04d", game.bestScore()), PINK);
        drawStat(context, x + (cardWidth + gap) * 2, y, cardWidth, "LEVEL", String.format("%02d", game.level()), PURPLE);
    }

    private void drawStat(DrawContext context, int x, int y, int w, String label, String value, int accent) {
        context.fill(x, y, x + w, y + 35, 0xD40D1C29);
        fillBorder(context, x, y, w, 35, 0xFF203B4A);
        context.fill(x, y, x + 2, y + 35, accent);
        context.drawText(textRenderer, Text.literal(label), x + 9, y + 5, MUTED, false);
        context.drawText(textRenderer, Text.literal(value), x + 9, y + 17, accent, true);
    }

    private void drawBoard(DrawContext context) {
        context.fill(boardX, boardY, boardX + boardWidth, boardY + boardHeight, 0xE9071520);
        fillBorder(context, boardX, boardY, boardWidth, boardHeight, 0xFF285365);

        int cell = Math.max(4, (int) Math.floor(Math.min(
                (boardWidth - 12.0) / SnakeGame.GRID_WIDTH,
                (boardHeight - 12.0) / SnakeGame.GRID_HEIGHT
        )));
        int gridWidth = cell * SnakeGame.GRID_WIDTH;
        int gridHeight = cell * SnakeGame.GRID_HEIGHT;
        int gridX = boardX + (boardWidth - gridWidth) / 2;
        int gridY = boardY + (boardHeight - gridHeight) / 2;

        context.fill(gridX - 3, gridY - 3, gridX + gridWidth + 3, gridY + gridHeight + 3, 0xFF061019);

        for (int x = 0; x <= SnakeGame.GRID_WIDTH; x++) {
            int lineX = gridX + x * cell;
            context.fill(lineX, gridY, lineX + 1, gridY + gridHeight, 0x402B6070);
        }
        for (int y = 0; y <= SnakeGame.GRID_HEIGHT; y++) {
            int lineY = gridY + y * cell;
            context.fill(gridX, lineY, gridX + gridWidth, lineY + 1, 0x402B6070);
        }

        long now = System.currentTimeMillis();
        float progress = game.interpolation(now);
        List<SnakeGame.Cell> snake = game.snake();
        List<SnakeGame.Cell> previous = game.previousSnake();

        context.enableScissor(gridX, gridY, gridX + gridWidth, gridY + gridHeight);

        SnakeGame.Cell food = game.food();
        if (food != null) {
            drawTile(context, FOOD_ITEM, gridX + food.x() * cell, gridY + food.y() * cell, cell, 0xFFFF5C8C);
        }

        SnakeGame.Cell bonus = game.bonus();
        if (bonus != null) {
            drawTile(context, BONUS_ITEM, gridX + bonus.x() * cell, gridY + bonus.y() * cell, cell, 0xFFB38CFF);
        }

        for (int i = snake.size() - 1; i >= 0; i--) {
            SnakeGame.Cell current = snake.get(i);
            SnakeGame.Cell before = previous.isEmpty()
                    ? current
                    : previous.get(Math.min(i, previous.size() - 1));

            float dx = current.x() - before.x();
            float dy = current.y() - before.y();
            if (dx > SnakeGame.GRID_WIDTH / 2f) dx -= SnakeGame.GRID_WIDTH;
            if (dx < -SnakeGame.GRID_WIDTH / 2f) dx += SnakeGame.GRID_WIDTH;
            if (dy > SnakeGame.GRID_HEIGHT / 2f) dy -= SnakeGame.GRID_HEIGHT;
            if (dy < -SnakeGame.GRID_HEIGHT / 2f) dy += SnakeGame.GRID_HEIGHT;

            float tileX = before.x() + dx * progress;
            float tileY = before.y() + dy * progress;
            ItemStack stack = i == 0 ? HEAD_ITEM : BODY_ITEM;
            int tint = i == 0 ? CYAN : 0xFF24B7C8;

            drawAnimatedTile(context, stack, gridX, gridY, tileX, tileY, cell, tint);

            if (tileX < 0) {
                drawAnimatedTile(context, stack, gridX, gridY, tileX + SnakeGame.GRID_WIDTH, tileY, cell, tint);
            } else if (tileX >= SnakeGame.GRID_WIDTH - 1) {
                drawAnimatedTile(context, stack, gridX, gridY, tileX - SnakeGame.GRID_WIDTH, tileY, cell, tint);
            }

            if (tileY < 0) {
                drawAnimatedTile(context, stack, gridX, gridY, tileX, tileY + SnakeGame.GRID_HEIGHT, cell, tint);
            } else if (tileY >= SnakeGame.GRID_HEIGHT - 1) {
                drawAnimatedTile(context, stack, gridX, gridY, tileX, tileY - SnakeGame.GRID_HEIGHT, cell, tint);
            }
        }

        context.disableScissor();

        if (!game.isStarted()) {
            drawCenterMessage(context, gridX, gridY, gridWidth, gridHeight, "READY TO CONNECT", "PRESS START TO BEGIN");
        } else if (!game.isRunning() && !game.isBoardComplete()) {
            drawCenterMessage(context, gridX, gridY, gridWidth, gridHeight, "SYSTEM PAUSED", "RESUME WHEN READY");
        } else if (game.isBoardComplete()) {
            drawCenterMessage(context, gridX, gridY, gridWidth, gridHeight, "GRID COMPLETE", "MAXIMUM SCORE REACHED");
        }
    }

    private void drawAnimatedTile(DrawContext context, ItemStack stack, int gridX, int gridY,
                                  float tileX, float tileY, int cell, int tint) {
        int x = gridX + Math.round(tileX * cell);
        int y = gridY + Math.round(tileY * cell);
        drawTile(context, stack, x, y, cell, tint);
    }

    private void drawTile(DrawContext context, ItemStack stack, int x, int y, int cell, int tint) {
        int inset = Math.max(1, cell / 12);
        context.fill(x + inset, y + inset, x + cell - inset, y + cell - inset, tint & 0x55FFFFFF);

        float scale = cell / 16.0f;
        float offset = (cell - 16.0f * scale) / 2.0f;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x + offset, y + offset);
        context.getMatrices().scale(scale, scale);
        context.drawItem(stack, 0, 0);
        context.getMatrices().popMatrix();
    }

    private void drawCenterMessage(DrawContext context, int x, int y, int w, int h, String title, String subtitle) {
        int boxWidth = Math.min(w - 20, 220);
        int boxX = x + (w - boxWidth) / 2;
        int boxY = y + h / 2 - 19;

        context.fill(boxX, boxY, boxX + boxWidth, boxY + 39, 0xD9091721);
        fillBorder(context, boxX, boxY, boxWidth, 39, 0xFF26758B);
        context.drawCenteredTextWithShadow(textRenderer, title, x + w / 2, boxY + 7, CYAN);
        context.drawCenteredTextWithShadow(textRenderer, subtitle, x + w / 2, boxY + 22, MUTED);
    }

    private void drawLeaderboard(DrawContext context) {
        int y = boardY;
        int h = boardHeight;
        context.fill(sideX, y, sideX + sideWidth, y + h, 0xE90D1925);
        fillBorder(context, sideX, y, sideWidth, h, 0xFF483C6F);

        context.drawText(textRenderer, Text.literal("TOP RUNS"), sideX + 9, y + 8, PURPLE, true);
        context.drawText(textRenderer, Text.literal("LOCAL RECORDS"), sideX + 9, y + 20, MUTED, false);
        context.fill(sideX + 8, y + 34, sideX + sideWidth - 8, y + 35, 0xFF51416D);

        List<Leaderboard.ScoreEntry> entries = game.leaderboard();
        int rowHeight = Math.max(9, Math.min(15, (h - 43) / 10));
        for (int i = 0; i < 10; i++) {
            int rowY = y + 40 + i * rowHeight;
            if (rowY + rowHeight > y + h - 4) {
                break;
            }

            if (i < entries.size()) {
                Leaderboard.ScoreEntry entry = entries.get(i);
                int color = i == 0 ? PINK : i < 3 ? PURPLE : MUTED;
                context.drawText(textRenderer, Text.literal(String.format("%02d", i + 1)), sideX + 9, rowY, color, false);
                String score = String.valueOf(entry.score());
                int scoreWidth = textRenderer.getWidth(score);
                context.drawText(textRenderer, Text.literal(score), sideX + sideWidth - scoreWidth - 9, rowY, TEXT, true);
            } else {
                context.drawText(textRenderer, Text.literal(String.format("%02d", i + 1)), sideX + 9, rowY, 0xFF3A5261, false);
                context.drawText(textRenderer, Text.literal("--"), sideX + sideWidth - 22, rowY, 0xFF3A5261, false);
            }
        }
    }

    private void drawButtons(DrawContext context) {
        boolean startHovered = startButton != null && startButton.isHovered();
        boolean newHovered = newGameButton != null && newGameButton.isHovered();

        String startLabel = game.isRunning() ? "ПАУЗА" : game.isStarted() ? "ПРОДОЛЖИТЬ" : "СТАРТ";
        drawNeonButton(context, windowX + 18, buttonY, startButtonWidth, 27,
                startLabel, startHovered, CYAN);
        drawNeonButton(context, newButtonX, buttonY, newButtonWidth, 27,
                "НОВАЯ ИГРА", newHovered, PINK);
    }

    private void drawNeonButton(DrawContext context, int x, int y, int w, int h,
                                String label, boolean hovered, int accent) {
        context.fill(x, y, x + w, y + h, hovered ? 0xFF173544 : 0xFF0C202C);
        fillBorder(context, x, y, w, h, hovered ? accent : 0xFF315363);
        context.fill(x + 1, y + 1, x + w - 1, y + 2, accent);
        context.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + 9, hovered ? TEXT : accent);
    }

    private void drawFooter(DrawContext context) {
        String help = "WASD / ARROWS  •  SPACE: PAUSE  •  ESC: CLOSE";
        context.drawCenteredTextWithShadow(textRenderer, help,
                windowX + windowWidth / 2, windowY + windowHeight - 14, MUTED);
    }

    private void fillBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }
}

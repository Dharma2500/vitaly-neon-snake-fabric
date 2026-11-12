package ru.vitaly.neonsnake;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class NeonSnakeClient implements ClientModInitializer {
    public static final Leaderboard LEADERBOARD = new Leaderboard();
    public static final SnakeGame GAME = new SnakeGame(LEADERBOARD);

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("snake")
                        .executes(context -> {
                            // Fabric's client-command guide recommends scheduling GUI changes
                            // through MinecraftClient.execute() instead of changing screens
                            // directly from a Brigadier command callback.
                            MinecraftClient client = context.getSource().getClient();
                            client.execute(() -> {
                                if (!(client.currentScreen instanceof SnakeScreen)) {
                                    client.setScreen(new SnakeScreen());
                                }
                            });
                            context.getSource().sendFeedback(Text.literal("NEON SNAKE: opening arcade..."));
                            return 1;
                        })));
    }
}

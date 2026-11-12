package ru.vitaly.neonsnake;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public final class NeonSnakeClient implements ClientModInitializer {
    public static final Leaderboard LEADERBOARD = new Leaderboard();
    public static final SnakeGame GAME = new SnakeGame(LEADERBOARD);

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("snake")
                        .executes(context -> {
                            MinecraftClient.getInstance().setScreen(new SnakeScreen());
                            return 1;
                        })));
    }
}

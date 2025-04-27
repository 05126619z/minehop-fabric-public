package net.nerdorg.minehop.networking;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.config.ConfigWrapper;
import net.nerdorg.minehop.config.MinehopConfig;
import net.nerdorg.minehop.networking.payloads.HandshakeIDPayload;

import java.util.HashMap;

public class HandshakeHandler {
    private static HashMap<String, Integer> waitingForShake = new HashMap<>();
    private static boolean registered = false;
    public static void register() {
        if (registered){return;}
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            MinehopConfig config = ConfigWrapper.config;
            if (config != null) {
                if (config.client_validation) {
                    for (String playerName : waitingForShake.keySet()) {
                        if (server.getTicks() > waitingForShake.get(playerName) + 60) {
                            ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(playerName);
                            if (serverPlayerEntity != null) {
                                serverPlayerEntity.networkHandler.disconnect(Text.of("Please install/update to at least version " + Minehop.MOD_VERSION_STRING + " of the Minehop mod before joining this server."));
                            }
                        }
                    }
                }
            }
        }));

        ServerPlayConnectionEvents.JOIN.register(((networkHandler, sender, server) -> {
            waitingForShake.put(networkHandler.player.getNameForScoreboard(), server.getTicks());
        }));

        registerReceivers();
    }

    private static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(HandshakeIDPayload.ID, (payload, ctx) -> {
            int mod_version = payload.mod_version();
            ServerPlayerEntity player = ctx.player();
            if (mod_version == Minehop.MOD_VERSION) {
                System.out.println("Validated " + player.getNameForScoreboard());
                waitingForShake.remove(player.getNameForScoreboard());
            }
        });
    }
}

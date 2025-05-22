package net.nerdorg.minehop.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.entity.ModEntities;
import net.nerdorg.minehop.entity.custom.EndEntity;
import net.nerdorg.minehop.entity.custom.ResetEntity;
import net.nerdorg.minehop.entity.custom.StartEntity;
import net.nerdorg.minehop.item.custom.BoundsStickItem;
import net.nerdorg.minehop.networking.PacketHandler;
import net.nerdorg.minehop.util.Logger;
import net.nerdorg.minehop.util.ZoneUtil;

import java.util.List;

public class SpawnCommands {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            LiteralArgumentBuilder.<ServerCommandSource>literal("spawn")
                .executes(context -> {
                    handleSpawn(context);
                    return Command.SINGLE_SUCCESS;
                })
            ));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                LiteralArgumentBuilder.<ServerCommandSource>literal("delspawn").requires(source -> source.hasPermissionLevel(4))
                        .executes(context -> {
                            removeSpawn(context);
                            return Command.SINGLE_SUCCESS;
                        })
        ));
    }

    public static void handleSpawn(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity serverPlayerEntity = context.getSource().getPlayer();
        String name = "spawn";
        DataManager.MapData pairedMap = DataManager.getMap(name);
        if (pairedMap != null) {
            if (!serverPlayerEntity.isSpectator()) {
                if (!serverPlayerEntity.isCreative()) {
                    serverPlayerEntity.getInventory().clear();
                }
                ServerWorld foundWorld = null;
                for (ServerWorld svrWorld : context.getSource().getServer().getWorlds()) {
                    if (svrWorld.getRegistryKey().toString().equals(pairedMap.worldKey)) {
                        foundWorld = svrWorld;
                        break;
                    }
                }
                if (foundWorld != null) {
                    serverPlayerEntity.teleportTo(ZoneUtil.makeTeleportTarget(
                            foundWorld,
                            new Vec3d(pairedMap.x, pairedMap.y, pairedMap.z),
                            (float) pairedMap.yrot,
                            (float) pairedMap.xrot
                    ));
                    Minehop.timerManager.remove(serverPlayerEntity.getNameForScoreboard());
                    Logger.logSuccess(serverPlayerEntity, "Teleporting to spawn.");
                    if (SpectateCommands.spectatorList.containsKey(serverPlayerEntity.getNameForScoreboard())) {
                        List<String> spectators = SpectateCommands.spectatorList.get(serverPlayerEntity.getNameForScoreboard());
                        for (String spectator : spectators) {
                            ServerPlayerEntity spectatorPlayer = context.getSource().getServer().getPlayerManager().getPlayer(spectator);
                            if (!spectatorPlayer.isCreative()) {
                                spectatorPlayer.getInventory().clear();
                            }
                            spectatorPlayer.teleportTo(ZoneUtil.makeTeleportTarget(serverPlayerEntity.getServerWorld(), new Vec3d(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ()), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch()));
                            spectatorPlayer.setCameraEntity(serverPlayerEntity);
                        }
                    }
                }
            }
        }
        else {
            Logger.logFailure(serverPlayerEntity, "Spawn hasn't been set.");
        }
    }

    private static void removeSpawn(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity serverPlayerEntity = context.getSource().getPlayer();
        String name = "spawn";
        DataManager.MapData pairedMap = DataManager.getMap(name);
        if (pairedMap != null) {
            Minehop.mapList.remove(pairedMap);
            Logger.logSuccess(serverPlayerEntity, "Deleting spawn.");
        }
        else {
            Logger.logFailure(serverPlayerEntity, "Spawn hasn't been set.");
        }
    }
}

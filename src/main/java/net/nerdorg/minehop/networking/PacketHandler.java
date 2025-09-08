package net.nerdorg.minehop.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.commands.SpectateCommands;
import net.nerdorg.minehop.config.MinehopConfig;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.discord.DiscordIntegration;
import net.nerdorg.minehop.networking.payloads.*;
import net.nerdorg.minehop.replays.ReplayEvents;
import net.nerdorg.minehop.replays.ReplayManager;
import net.nerdorg.minehop.util.Logger;
import net.nerdorg.minehop.util.ZoneUtil;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;

public class PacketHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static boolean registered = false;

    public static void register() {
        registerC2S();
        registerS2C();
    }

    private static void registerS2C() {
        // server to client
        PayloadTypeRegistry.playS2C().register(AntiCheatPayload.ID, AntiCheatPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CSpecEfficiencyPayload.ID, CSpecEfficiencyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakeIDPayload.ID, HandshakeIDPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MapFinishPayload.ID, MapFinishPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenMapScreenPayload.ID, OpenMapScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OtherVTogglePayload.ID, OtherVTogglePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ReplayVTogglePayload.ID, ReplayVTogglePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SelfVTogglePayload.ID, SelfVTogglePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendEfficiencyPayload.ID, SendEfficiencyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendMapPayload.ID, SendMapPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendPersonalRecordPayload.ID, SendPersonalRecordPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendRecordPayload.ID, SendRecordPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendSpectatorsPayload.ID, SendSpectatorsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendTimePayload.ID, SendTimePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SetCheaterPayload.ID, SetCheaterPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SSpecEfficiencyPayload.ID, SSpecEfficiencyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdatePowerPayload.ID, UpdatePowerPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ZoneSyncIDPayload.ID, ZoneSyncIDPayload.CODEC);
    }

    private static void registerC2S() {
        // client to server
        PayloadTypeRegistry.playC2S().register(AntiCheatPayload.ID, AntiCheatPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CSpecEfficiencyPayload.ID, CSpecEfficiencyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakeIDPayload.ID, HandshakeIDPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MapFinishPayload.ID, MapFinishPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenMapScreenPayload.ID, OpenMapScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OtherVTogglePayload.ID, OtherVTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReplayVTogglePayload.ID, ReplayVTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SelfVTogglePayload.ID, SelfVTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendEfficiencyPayload.ID, SendEfficiencyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendMapPayload.ID, SendMapPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendPersonalRecordPayload.ID, SendPersonalRecordPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendRecordPayload.ID, SendRecordPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendSpectatorsPayload.ID, SendSpectatorsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendTimePayload.ID, SendTimePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetCheaterPayload.ID, SetCheaterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SSpecEfficiencyPayload.ID, SSpecEfficiencyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdatePowerPayload.ID, UpdatePowerPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ZoneSyncIDPayload.ID, ZoneSyncIDPayload.CODEC);
    }

    public static void sendConfigToClient(ServerPlayerEntity player, MinehopConfig config) {
        DataManager.MapData currentMap = ZoneUtil.getCurrentMap(player);
        ServerPlayNetworking.send(player,  new ConfigSyncPayload(
            config.movement.sv_friction,
            config.movement.sv_accelerate,
            config.movement.sv_airaccelerate,
            config.movement.sv_maxairspeed,
            config.movement.speed_mul,
            config.movement.sv_gravity,
            config.movement.speed_coefficient,
            Minehop.speedCapMap.containsKey(player.getNameForScoreboard()) ? Minehop.speedCapMap.get(player.getNameForScoreboard()) : 1000000,
        currentMap != null && currentMap.hns,
            config.enabled,
            config.fall_damage
        ));
    }
    public static void updateZone(ServerPlayerEntity player, int entityId, BlockPos pos1, BlockPos pos2, String name, int check_index) {
        ServerPlayNetworking.send(player,  new ZoneSyncIDPayload(
                entityId,
                new Vector3f(pos1.getX(), pos1.getY(), pos1.getZ()),
                new Vector3f(pos2.getX(), pos2.getY(), pos2.getZ()),
                name,
                check_index
        ));
    }

    public static void sendSelfVToggle(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SelfVTogglePayload(true));
    }

    public static void sendOtherVToggle(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player,  new OtherVTogglePayload(true));
    }

    public static void sendReplayVToggle(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player,  new ReplayVTogglePayload(true));
    }

    public static void sendEfficiency(ServerPlayerEntity player, double efficiency) {
        ServerPlayNetworking.send(player,  new SendEfficiencyPayload(efficiency));
    }

    public static void sendSpectators(ServerPlayerEntity player) {
        if (SpectateCommands.spectatorList.containsKey(player.getNameForScoreboard())) {
            List<String> spectators = SpectateCommands.spectatorList.get(player.getNameForScoreboard());
            if (spectators.size() > 1) {
                String buff = "";
                buff += (spectators.size() - 1);
                for (String spectator : spectators) {
                    if (!spectator.equals(player.getNameForScoreboard())) {
                        buff += ("~" + spectator);
                    }
                }

                ServerPlayNetworking.send(player,  new SendSpectatorsPayload(buff));
            }
        }
    }

    private static void handleMapCompletion(ServerPlayerEntity player, MinecraftServer server, String mapName, float time) {
        float ping_limit = 300; // ping limit in ms
        if (!player.isCreative() && !player.isSpectator() && !Minehop.currentCheaters.contains(player)) {
            if (Minehop.timerManager.containsKey(player.getNameForScoreboard())) {
                String map_name = mapName;

                HashMap<String, Long> timerMap = Minehop.timerManager.get(player.getNameForScoreboard());
                List<String> keyList = timerMap.keySet().stream().toList();
                double rawTime = (double) (System.nanoTime() - timerMap.get(keyList.get(0))) / 1000000000;
                if (time < rawTime + (ping_limit / 1000f) && time > rawTime - (ping_limit / 1000f)) {
                    String formattedNumber = String.format("%.5f", time);
                    DataManager.RecordData mapRecord = DataManager.getRecord(map_name);
                    if (mapRecord != null) {
                        if (time < mapRecord.time) {
                            String recordMessage = player.getNameForScoreboard() + " just beat " + mapRecord.name + "'s time (" + String.format("%.5f", mapRecord.time) + ") on " + mapRecord.map_name + " and now hold the world record with a time of " + formattedNumber + "!";
                            Logger.logGlobal(server, recordMessage);
                            Minehop.recordList.remove(mapRecord);
                            if (DataManager.getAnyRecordFromName(mapRecord.name) == null) {
                                server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse("lp user " + mapRecord.name + " parent remove record_holder", server.getCommandSource()), "lp user " + mapRecord.name + " parent remove record_holder");
                            }
                            server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse("lp user " + player.getNameForScoreboard() + " parent add record_holder", server.getCommandSource()), "lp user " + player.getNameForScoreboard() + " parent add record_holder");
                            Minehop.recordList.add(new DataManager.RecordData(player.getNameForScoreboard(), map_name, time));
                            DataManager.saveData(player.getServerWorld(), DataManager.recordsListLocation, Minehop.recordList);
                            ReplayManager.Replay replay = new ReplayManager.Replay(map_name, player.getNameForScoreboard(), time, ReplayEvents.replayEntryMap.get(player.getNameForScoreboard()));
                            ReplayManager.saveRecordReplay(player.getServerWorld(), replay);
                            DiscordIntegration.sendRecordToDiscord(recordMessage);
                        }
                    } else {
                        String recordMessage = player.getNameForScoreboard() + " just claimed the world record on " + map_name + " with a time of " + formattedNumber + "!";
                        Logger.logGlobal(server, recordMessage);
                        server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse("lp user " + player.getNameForScoreboard() + " parent add record_holder", server.getCommandSource()), "lp user " + player.getNameForScoreboard() + " parent add record_holder");
                        Minehop.recordList.add(new DataManager.RecordData(player.getNameForScoreboard(), map_name, time));
                        DataManager.saveData(player.getServerWorld(), DataManager.recordsListLocation, Minehop.recordList);
                        ReplayManager.Replay replay = new ReplayManager.Replay(map_name, player.getNameForScoreboard(), time, ReplayEvents.replayEntryMap.get(player.getNameForScoreboard()));
                        ReplayManager.saveRecordReplay(player.getServerWorld(), replay);
                        DiscordIntegration.sendRecordToDiscord(recordMessage);
                    }
                    DataManager.RecordData mapPersonalRecord = DataManager.getPersonalRecord(player.getNameForScoreboard(), map_name);
                    if (mapPersonalRecord != null) {
                        if (time < mapPersonalRecord.time) {
                            Logger.logSuccess(player, "You just beat your time (" + String.format("%.5f", mapPersonalRecord.time) + ") on " + mapPersonalRecord.map_name + ", your new record is " + formattedNumber + "!");
                            Minehop.personalRecordList.remove(mapPersonalRecord);
                            Minehop.personalRecordList.add(new DataManager.RecordData(player.getNameForScoreboard(), map_name, time));
                            DataManager.saveData(player.getServerWorld(), DataManager.pbListLocation, Minehop.personalRecordList);
                        }
                    } else {
                        Logger.logSuccess(player, "You just claimed a personal record of " + formattedNumber + "!");
                        Minehop.personalRecordList.add(new DataManager.RecordData(player.getNameForScoreboard(), map_name, time));
                        DataManager.saveData(player.getServerWorld(), DataManager.pbListLocation, Minehop.personalRecordList);
                    }
                    Logger.logSuccess(player, "Completed " + map_name + " in " + formattedNumber + " seconds.");
                } else {
                    Logger.logServer(server, "Invalid time for " + player.getNameForScoreboard() + ".");
                }
                Minehop.timerManager.remove(player.getNameForScoreboard());
            }
        }
    }

    public static void sendSpecEfficiency(ServerPlayerEntity player, double last_jump_speed, int jump_count, double last_efficiency) {
        ServerPlayNetworking.send(player,  new CSpecEfficiencyPayload(last_jump_speed, jump_count, last_efficiency));
    }

    public static void sendOpenMapScreen(ServerPlayerEntity player, String title) {
        ServerPlayNetworking.send(player,  new OpenMapScreenPayload(title));
    }

    public static void sendMaps(ServerPlayerEntity player) {
        String buff = "";

        buff += Minehop.mapList.size();

        for (DataManager.MapData mapData : Minehop.mapList) {
            buff += "^";
            buff += mapData.name;buff += "~";
            buff += mapData.x;buff += "~";
            buff += mapData.y;buff += "~";
            buff += mapData.z;buff += "~";
            buff += mapData.xrot;buff += "~";
            buff += mapData.yrot;buff += "~";
            buff += mapData.worldKey;buff += "~";
            buff += mapData.arena;buff += "~";
            buff += mapData.hns;buff += "~";
            buff += mapData.difficulty;buff += "~";
            buff += mapData.player_count;
        }

        ServerPlayNetworking.send(player,  new SendMapPayload(buff));
    }

    public static void sendRecords(ServerPlayerEntity player) {
        String buff = "";

        buff += Minehop.mapList.size();

        for (DataManager.MapData mapData : Minehop.mapList) {
            DataManager.RecordData recordData = DataManager.getRecord(mapData.name);
            buff += "^";
            if (recordData != null) {
                buff += recordData.map_name;buff += "~";
                buff += recordData.name;buff += "~";
                buff += recordData.time;
            }
            else {
                buff += mapData.name;buff += "~";
                buff += "None";buff += "~";
                buff += 1000000;
            }
        }

        ServerPlayNetworking.send(player,  new SendRecordPayload(buff));
    }

    public static void sendPersonalRecords(ServerPlayerEntity player) {
        PropertyMap map = new PropertyMap();
        int index = 0;
        for (DataManager.RecordData recordData : Minehop.personalRecordList) {
            Property property = new Property(
                    recordData.map_name,
                    recordData.name,
                    ""+recordData.time
            );
            map.put(""+index, property);
            index++;
        }

        ServerPlayNetworking.send(player,  new SendPersonalRecordPayload(map));
    }

    public static void sendPower(ServerPlayerEntity player, double x_power, double y_power, double z_power, BlockPos boosterPos) {
        ServerPlayNetworking.send(player,  new UpdatePowerPayload(x_power, y_power, z_power, boosterPos.getX(), boosterPos.getY(), boosterPos.getZ()));
    }


    public static void registerReceivers() {
        if (registered) {return;}
        registered = true;

        ServerPlayNetworking.registerGlobalReceiver(SendTimePayload.ID, (payload, ctx) -> {
            ServerPlayerEntity player = ctx.player();
            MinecraftServer server = ctx.server();
            if (!player.isSpectator()) {
                float time = payload.time();
                if (player != null && Minehop.timerManager.containsKey(player.getNameForScoreboard())) {
                    HashMap<String, Long> timerMap = Minehop.timerManager.get(player.getNameForScoreboard());
                    List<String> keyList = timerMap.keySet().stream().toList();
                    String mapName = keyList.get(0);
                    DataManager.RecordData personalRecordData = DataManager.getPersonalRecord(player.getNameForScoreboard(), mapName);
                    double personalRecord = 0;
                    if (personalRecordData != null) {
                        personalRecord = personalRecordData.time;
                    }
                    String formattedNumber = String.format("%.2f", time);
                    if (SpectateCommands.spectatorList.containsKey(player.getNameForScoreboard())) {
                        List<String> spectators = SpectateCommands.spectatorList.get(player.getNameForScoreboard());
                        for (String spectatorName : spectators) {
                            if (!spectatorName.equals(player.getNameForScoreboard())) {
                                ServerPlayerEntity spectatorPlayer = server.getPlayerManager().getPlayer(spectatorName);
                                if (!spectatorPlayer.isCreative()) {
                                    spectatorPlayer.getInventory().clear();
                                }
                                spectatorPlayer.teleportTo(ZoneUtil.makeTeleportTarget(player.getServerWorld(), new Vec3d(player.getX(), player.getY(), player.getZ()), player.getYaw(), player.getPitch()));
                                spectatorPlayer.setCameraEntity(player);
                                Logger.logActionBar(spectatorPlayer, "Time: " + formattedNumber + " PB: " + (personalRecord != 0 ? String.format("%.5f", personalRecord) : "No PB"));
                            }
                        }
                    }
                    Logger.logActionBar(player, "Time: " + formattedNumber + " PB: " + (personalRecord != 0 ? String.format("%.5f", personalRecord) : "No PB"));
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(MapFinishPayload.ID, (payload, ctx) -> {
            ServerPlayerEntity player = ctx.player();
            MinecraftServer server = ctx.server();
            String mapName = payload.map_name();
            float time = payload.time();
            handleMapCompletion(player, server, mapName, time);
        });
        ServerPlayNetworking.registerGlobalReceiver(SSpecEfficiencyPayload.ID, (payload, ctx) -> {
            ServerPlayerEntity player = ctx.player();
            MinecraftServer server = ctx.server();
            double last_jump_speed =  payload.last_jump_speed();
            int jump_count = (int) payload.jump_count();
            double last_efficiency = payload.last_efficiency();

            Minehop.lastEfficiencyMap.put(player.getNameForScoreboard(), new ReplayManager.SSJEntry(jump_count, last_jump_speed, last_efficiency));

            if (SpectateCommands.spectatorList.containsKey(player.getNameForScoreboard())) {
                List<String> spectators = SpectateCommands.spectatorList.get(player.getNameForScoreboard());
                for (String spectator : spectators) {
                    ServerPlayerEntity spectatorPlayer = server.getPlayerManager().getPlayer(spectator);
                    if (spectatorPlayer != null) {
                        if (!spectatorPlayer.getNameForScoreboard().equals(player.getNameForScoreboard())) {
                            sendSpecEfficiency(spectatorPlayer, last_jump_speed, jump_count, last_efficiency);
                        }
                    }
                }
            }
        });
    }

}

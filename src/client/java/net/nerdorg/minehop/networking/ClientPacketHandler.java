package net.nerdorg.minehop.networking;

import com.mojang.datafixers.util.Pair;
import com.mojang.util.UUIDTypeAdapter;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.MinehopClient;
import net.nerdorg.minehop.anticheat.ProcessChecker;
import net.nerdorg.minehop.block.entity.BoostBlockEntity;
import net.nerdorg.minehop.config.ConfigWrapper;
import net.nerdorg.minehop.config.MinehopConfig;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.entity.client.CustomPlayerEntityRenderer;
import net.nerdorg.minehop.entity.custom.EndEntity;
import net.nerdorg.minehop.entity.custom.ResetEntity;
import net.nerdorg.minehop.entity.custom.StartEntity;
import net.nerdorg.minehop.networking.payloads.*;
import net.nerdorg.minehop.screen.SelectMapScreen;
import org.joml.Vector3i;

import java.util.*;

public class ClientPacketHandler {
    public static void sortMapList(List<DataManager.MapData> mapList) {
        Collections.sort(mapList, new Comparator<DataManager.MapData>() {
            @Override
            public int compare(DataManager.MapData m1, DataManager.MapData m2) {
                return m1.name.compareToIgnoreCase(m2.name);
            }
        });
    }

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
                // Assign the read values to your variables or fields here
                Minehop.o_sv_friction = payload.sv_friction();
                Minehop.o_sv_accelerate = payload.sv_accelerate();
                Minehop.o_sv_airaccelerate = payload.sv_airaccelerate();
                Minehop.o_sv_maxairspeed = payload.sv_maxairspeed();
                Minehop.o_speed_mul = payload.speed_mul();
                Minehop.o_sv_gravity = payload.sv_gravity();
                Minehop.o_speed_coefficient = payload.speedCoef();
                Minehop.o_speed_cap = payload.speedCap();
                Minehop.o_hns = payload.isHNS();
                Minehop.o_enabled = payload.isEnabled();
                Minehop.o_fall_damage = payload.fallDamage();

                Minehop.receivedConfig = true;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZoneSyncIDPayload.ID, (payload, ctx) -> {
            BlockPos pos1 = new BlockPos((int) payload.pos1().x, (int) payload.pos1().y, (int) payload.pos1().z);
            BlockPos pos2 = new BlockPos((int) payload.pos2().x, (int) payload.pos2().y, (int) payload.pos2().z);

            MinecraftClient client = ctx.client();
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            client.execute(() -> {
                // Assign the read values to your variables or fields here
                Entity entity = client.world.getEntityById(payload.entityId());
                if (entity instanceof ResetEntity resetEntity) {
                    resetEntity.setCorner1(pos1);
                    resetEntity.setCorner2(pos2);
                    resetEntity.setPairedMap(payload.name());
                    resetEntity.setCheckIndex(payload.check_index());
                }
                else if (entity instanceof StartEntity startEntity) {
                    startEntity.setCorner1(pos1);
                    startEntity.setCorner2(pos2);
                    startEntity.setPairedMap(payload.name());
                }
                else if (entity instanceof EndEntity endEntity) {
                    endEntity.setCorner1(pos1);
                    endEntity.setCorner2(pos2);
                    endEntity.setPairedMap(payload.name());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SelfVTogglePayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
              //  MinehopClient.hideSelf = !MinehopClient.hideSelf;
                ConfigWrapper.config.hideSelf = !ConfigWrapper.config.hideSelf;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OtherVTogglePayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
             //   MinehopClient.hideOthers = !MinehopClient.hideOthers;
                ConfigWrapper.config.hideOthers = !ConfigWrapper.config.hideOthers;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OtherVTogglePayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
                //MinehopClient.hideReplay = !MinehopClient.hideReplay;
                ConfigWrapper.config.hideReplay = !ConfigWrapper.config.hideReplay;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendSpectatorsPayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            String buff = payload.spectatorBuff();
            ctx.client().execute(() -> {
                List<String> newSpectatorList = new ArrayList<>();
                String splitBuff[] = buff.split("~");
                int stringCount = Integer.parseInt(splitBuff[0]);


                for (int i = 1; i < stringCount; i++) {
                    String spectatorName = splitBuff[i]; // This reads a string from the buffer
                    newSpectatorList.add(spectatorName);
                }

                MinehopClient.spectatorList = newSpectatorList;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendEfficiencyPayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            double efficiency = payload.efficiency();

            MinecraftClient client = ctx.client();
            client.execute(() -> {
                if (efficiency != 0) {
                    MinehopClient.last_efficiency = efficiency;
                }
                else {
                    if (Minehop.efficiencyListMap.containsKey(client.player.getNameForScoreboard())) {
                        List<Double> efficiencyList = Minehop.efficiencyListMap.get(client.player.getNameForScoreboard());
                        if (efficiencyList != null && efficiencyList.size() > 1) {
                            double averageEfficiency = efficiencyList.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
                            MinehopClient.last_efficiency = averageEfficiency;
                            Minehop.efficiencyListMap.put(client.player.getNameForScoreboard(), new ArrayList<>());
                        }
                    }
                }
                sendSpecEfficiency();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CSpecEfficiencyPayload.ID, (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            double last_jump_speed = payload.last_jump_speed();
            int jump_count = payload.jump_count();
            double last_efficiency = payload.last_efficiency();

            ctx.client().execute(() -> {
                MinehopClient.last_jump_speed = last_jump_speed;
                MinehopClient.jump_count = jump_count;
                MinehopClient.last_efficiency = last_efficiency;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenMapScreenPayload.ID, (payload, ctx) -> {
            String title = payload.title();
            MinecraftClient client = ctx.client();
            client.execute(() -> {
                client.setScreen(new SelectMapScreen(Text.literal(title)));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendRecordPayload.ID, (payload, ctx) -> {
            List<DataManager.RecordData> newRecordList = new ArrayList<>();
            String splitBuff[] = payload.buff().split("\\^");
            int recordCount = Integer.parseInt(splitBuff[0]);

            for (int i = 1; i < recordCount+1; i++) {
                String buff[] = splitBuff[i].split("~");
                String map_name = buff[0];
                String name = buff[1];
                double time = Double.parseDouble(buff[2]);
                if (time > 0) {
                    newRecordList.add(new DataManager.RecordData(name, map_name, time));
                }
            }

            ctx.client().execute(() -> {
                Minehop.recordList = newRecordList;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendMapPayload.ID, (payload, ctx) -> {
            String splitBuff[] = payload.buff().split("\\^");
            List<DataManager.MapData> newMapList = new ArrayList<>();
            int recordCount = Integer.parseInt(splitBuff[0]);

            for (int i = 1; i < recordCount+1; i++) {
                String buff[] = splitBuff[i].split("~");
                String name = buff[0];
                double x = Double.parseDouble(buff[1]);
                double y = Double.parseDouble(buff[2]);
                double z = Double.parseDouble(buff[3]);
                double xrot = Double.parseDouble(buff[4]);
                double yrot = Double.parseDouble(buff[5]);
                String worldKey = buff[6];
                boolean arena = Boolean.parseBoolean(buff[7]);
                boolean hns = Boolean.parseBoolean(buff[8]);
                int difficulty = Integer.parseInt(buff[9]);
                int player_count = Integer.parseInt(buff[10]);
                newMapList.add(new DataManager.MapData(name, x, y, z, xrot, yrot, worldKey, arena, hns, difficulty, player_count));
            }

            ctx.client().execute(() -> {
                Minehop.mapList = newMapList;
                sortMapList(Minehop.mapList);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendPersonalRecordPayload.ID, (payload, ctx) -> {
            List<DataManager.RecordData> newRecordList = new ArrayList<>();
            String splitBuff[] = payload.buff().split("\\^");
            int recordCount = Integer.parseInt(splitBuff[0]);

            for (int i = 1; i < recordCount+1; i++) {
                String buff[] = splitBuff[i].split("~");
                String map_name = buff[0];
                String name = buff[1];
                double time = Double.parseDouble(buff[2]);
                if (time > 0) {
                    newRecordList.add(new DataManager.RecordData(name, map_name, time));
                }
            }

            ctx.client().execute(() -> {
                Minehop.personalRecordList = newRecordList;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdatePowerPayload.ID, (payload, ctx) -> {
            double power_x = payload.x_power();
            double power_y = payload.y_power();
            double power_z = payload.z_power();

            BlockPos boosterPos = new BlockPos(payload.posX(), payload.posY(), payload.posZ());
            MinecraftClient client = ctx.client();
            // Ensure you are on the main thread when modifying the game or accessing client side only classes
            client.execute(() -> {
                // Assign the read values to your variables or fields here
                new Thread(() -> {
                    BlockEntity blockEntity = client.player.getWorld().getBlockEntity(boosterPos);
                    if (blockEntity instanceof BoostBlockEntity boostBlockEntity) {
                        boostBlockEntity.setXPower(power_x);
                        boostBlockEntity.setYPower(power_y);
                        boostBlockEntity.setZPower(power_z);
                    }
                }).start();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(AntiCheatPayload.ID, (payload, ctx) -> {
            System.out.println("Anti Cheat Check");
            MinecraftClient client = ctx.client();
            client.execute(() -> {
                new Thread(() -> {

                    String[] stringNames = payload.buff().split("~");
                    stringNames = Arrays.copyOfRange(stringNames, 1, stringNames.length);
                    String checkResults = ProcessChecker.scanProcessesForKeywords(List.of(stringNames));

                    sendAntiCheatCheck(checkResults);
                }).start();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SetCheaterPayload.ID, (payload, ctx) -> {
            MinecraftClient client = ctx.client();
            ClientWorld world = ctx.client().world;
            client.execute(() -> {
                new Thread(() -> {

                    String UUID = payload.uuid();
                    boolean isCheater = payload.isCheater();

                    PlayerEntity cheater = world.getPlayerByUuid(java.util.UUID.fromString(UUID));

                    if (isCheater) {
                        if (client.player.getUuidAsString().equals(UUID)) {
                                client.getNetworkHandler().sendCommand("map restart");
                        }
                        CustomPlayerEntityRenderer.setPlayerModel(CustomPlayerEntityRenderer.PlayerModel.Cheater, UUID);
                        Minehop.currentCheaters.add(world.getPlayerByUuid(java.util.UUID.fromString(UUID)));
                    }
                    else {
                        CustomPlayerEntityRenderer.setPlayerModel(CustomPlayerEntityRenderer.PlayerModel.Player, UUID);
                        while (Minehop.currentCheaters.contains(world.getPlayerByUuid(java.util.UUID.fromString(UUID)))) {
                            Minehop.currentCheaters.remove(world.getPlayerByUuid(java.util.UUID.fromString(UUID)));
                        }
                    }

                }).start();
            });
        });
    }

    public static void sendHandshake() {
        ClientPlayNetworking.send(new HandshakeIDPayload(Minehop.MOD_VERSION));
    }

    public static void sendSpecEfficiency() {
        ClientPlayNetworking.send(new SSpecEfficiencyPayload(MinehopClient.last_jump_speed, MinehopClient.jump_count, MinehopClient.last_efficiency));
    }

    public static void sendAntiCheatCheck(String checkResults) {
        if (checkResults == null) {checkResults="";}

        ClientPlayNetworking.send(new AntiCheatPayload(checkResults));
    }

    public static void sendEndMapEvent(String map_name, float time) {
        ClientPlayNetworking.send(new MapFinishPayload(map_name, time));
    }

    public static void sendCurrentTime(float time) {
        if (time > MinehopClient.lastSendTime + 0.01) {
            ClientPlayNetworking.send(new SendTimePayload(time));
            MinehopClient.lastSendTime = time;
        }
    }
}

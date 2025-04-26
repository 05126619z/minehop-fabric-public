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
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.entity.client.CustomPlayerEntityRenderer;
import net.nerdorg.minehop.entity.custom.EndEntity;
import net.nerdorg.minehop.entity.custom.ResetEntity;
import net.nerdorg.minehop.entity.custom.StartEntity;
import net.nerdorg.minehop.screen.SelectMapScreen;

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
        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.CONFIG_SYNC_ID), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            double o_sv_friction = buf.readDouble();
            double o_sv_accelerate = buf.readDouble();
            double o_sv_airaccelerate = buf.readDouble();
            double o_sv_maxairspeed = buf.readDouble();
            double o_speed_mul = buf.readDouble();
            double o_sv_gravity = buf.readDouble();
            double o_speed_cap = buf.readDouble();
            boolean o_hns = buf.readBoolean();

            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
                // Assign the read values to your variables or fields here
                Minehop.o_sv_friction = o_sv_friction;
                Minehop.o_sv_accelerate = o_sv_accelerate;
                Minehop.o_sv_airaccelerate = o_sv_airaccelerate;
                Minehop.o_sv_maxairspeed = o_sv_maxairspeed;
                Minehop.o_speed_mul = o_speed_mul;
                Minehop.o_sv_gravity = o_sv_gravity;
                Minehop.o_speed_cap = o_speed_cap;
                Minehop.o_hns = o_hns;

                Minehop.receivedConfig = true;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.ZONE_SYNC_ID), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            int entity_id = buf.readInt();
            BlockPos pos1 = buf.readBlockPos();
            BlockPos pos2 = buf.readBlockPos();
            String name = buf.readString();
            int check_index = buf.readInt();
            
            MinecraftClient client = ctx.client();
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            client.execute(() -> {
                // Assign the read values to your variables or fields here
                Entity entity = client.world.getEntityById(entity_id);
                if (entity instanceof ResetEntity resetEntity) {
                    resetEntity.setCorner1(pos1);
                    resetEntity.setCorner2(pos2);
                    resetEntity.setPairedMap(name);
                    resetEntity.setCheckIndex(check_index);
                }
                else if (entity instanceof StartEntity startEntity) {
                    startEntity.setCorner1(pos1);
                    startEntity.setCorner2(pos2);
                    startEntity.setPairedMap(name);
                }
                else if (entity instanceof EndEntity endEntity) {
                    endEntity.setCorner1(pos1);
                    endEntity.setCorner2(pos2);
                    endEntity.setPairedMap(name);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SELF_V_TOGGLE), (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
                MinehopClient.hideSelf = !MinehopClient.hideSelf;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.OTHER_V_TOGGLE), (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
                MinehopClient.hideOthers = !MinehopClient.hideOthers;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.REPLAY_V_TOGGLE), (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            ctx.client().execute(() -> {
                MinehopClient.hideReplay = !MinehopClient.hideReplay;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SEND_SPECTATORS), (payload, ctx) -> {
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            ctx.client().execute(() -> {
                List<String> newSpectatorList = new ArrayList<>();
                int stringCount = buf.readInt();

                for (int i = 0; i < stringCount; i++) {
                    String spectatorName = buf.readString(); // This reads a string from the buffer
                    newSpectatorList.add(spectatorName);
                }

                MinehopClient.spectatorList = newSpectatorList;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SEND_EFFICIENCY), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            double efficiency = buf.readDouble();

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

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.CLIENT_SPEC_EFFICIENCY), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            // Ensure you are on the main thread when modifying the game or accessing client-side only classes
            double last_jump_speed = buf.readDouble();
            int jump_count = buf.readInt();
            double last_efficiency = buf.readDouble();

            ctx.client().execute(() -> {
                MinehopClient.last_jump_speed = last_jump_speed;
                MinehopClient.jump_count = jump_count;
                MinehopClient.last_efficiency = last_efficiency;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.OPEN_MAP_SCREEN), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            String title = buf.readString();
            MinecraftClient client = ctx.client();
            client.execute(() -> {
                client.setScreen(new SelectMapScreen(Text.literal(title)));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SEND_RECORDS), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            List<DataManager.RecordData> newRecordList = new ArrayList<>();
            int recordCount = buf.readInt();

            for (int i = 0; i < recordCount; i++) {
                String map_name = buf.readString();
                String name = buf.readString();
                double time = buf.readDouble();
                if (time > 0) {
                    newRecordList.add(new DataManager.RecordData(name, map_name, time));
                }
            }

            ctx.client().execute(() -> {
                Minehop.recordList = newRecordList;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SEND_MAPS), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            List<DataManager.MapData> newMapList = new ArrayList<>();
            int recordCount = buf.readInt();

            for (int i = 0; i < recordCount; i++) {
                String name = buf.readString();
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                double xrot = buf.readDouble();
                double yrot = buf.readDouble();
                String worldKey = buf.readString();
                boolean arena = buf.readBoolean();
                boolean hns = buf.readBoolean();
                int difficulty = buf.readInt();
                int player_count = buf.readInt();
                newMapList.add(new DataManager.MapData(name, x, y, z, xrot, yrot, worldKey, arena, hns, difficulty, player_count));
            }

            ctx.client().execute(() -> {
                Minehop.mapList = newMapList;
                sortMapList(Minehop.mapList);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SEND_PERSONAL_RECORDS), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            List<DataManager.RecordData> newRecordList = new ArrayList<>();
            int recordCount = buf.readInt();

            for (int i = 0; i < recordCount; i++) {
                String map_name = buf.readString();
                String name = buf.readString();
                double time = buf.readDouble();
                if (time > 0) {
                    newRecordList.add(new DataManager.RecordData(name, map_name, time));
                }
            }

            ctx.client().execute(() -> {
                Minehop.personalRecordList = newRecordList;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.UPDATE_POWER), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            double power_x = buf.readDouble();
            double power_y = buf.readDouble();
            double power_z = buf.readDouble();

            BlockPos boosterPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
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

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.ANTI_CHEAT_CHECK), (payload, ctx) -> {
            System.out.println("Anti Cheat Check");
            MinecraftClient client = ctx.client();
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            client.execute(() -> {
                new Thread(() -> {

                    String[] stringNames = buf.readString().split("~");
                    stringNames = Arrays.copyOfRange(stringNames, 1, stringNames.length);
                    String checkResults = ProcessChecker.scanProcessesForKeywords(List.of(stringNames));

                    sendAntiCheatCheck(checkResults);
                }).start();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new CustomPayload.Id<MyCustomPayload>(ModMessages.SET_PLAYER_CHEATER), (payload, ctx) -> {
            PacketByteBuf buf = ((MyCustomPayload) payload).buff();
            MinecraftClient client = ctx.client();
            ClientWorld world = ctx.client().world;
            client.execute(() -> {
                new Thread(() -> {

                    String UUID = buf.readString();
                    boolean isCheater = buf.readBoolean();

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
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(Minehop.MOD_VERSION);

        ClientPlayNetworking.send(new MyCustomPayload(ModMessages.HANDSHAKE_ID, buf));
    }

    public static void sendSpecEfficiency() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeDouble(MinehopClient.last_jump_speed);
        buf.writeInt(MinehopClient.jump_count);
        buf.writeDouble(MinehopClient.last_efficiency);

        ClientPlayNetworking.send(new MyCustomPayload(ModMessages.SERVER_SPEC_EFFICIENCY, buf));
    }

    public static void sendAntiCheatCheck(String checkResults) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        if (checkResults == null) {checkResults="";}
        buf.writeString(checkResults);

        ClientPlayNetworking.send(new MyCustomPayload(ModMessages.ANTI_CHEAT_CHECK, buf));
    }

    public static void sendEndMapEvent(float time) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeFloat(time);

        ClientPlayNetworking.send(new MyCustomPayload(ModMessages.MAP_FINISH, buf));
    }

    public static void sendCurrentTime(float time) {
        if (time > MinehopClient.lastSendTime + 0.01) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

            buf.writeFloat(time);

            ClientPlayNetworking.send(new MyCustomPayload(ModMessages.SEND_TIME, buf));
            MinehopClient.lastSendTime = time;
        }
    }
}

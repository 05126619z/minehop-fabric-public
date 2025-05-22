package net.nerdorg.minehop.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.entity.custom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ZoneUtil {
    public static String getCurrentMapName(Entity target_entity) {
        if (Minehop.playerMapLocation.containsKey(target_entity.getUuidAsString())) {
            return Minehop.playerMapLocation.get(target_entity.getUuidAsString()).getPairedMap();
        }
        return null;
    }

    public static GamemodeEntity getGamemodeEntity(String map_name, ServerWorld serverWorld) {
        for (Entity entity : serverWorld.iterateEntities()) {
            if (entity instanceof GamemodeEntity gamemodeEntity) {
                if (Objects.equals(gamemodeEntity.getPairedMap(), map_name)) {
                    return gamemodeEntity;
                }
            }
        }

        return null;
    }

    public static DataManager.MapData getCurrentMap(Entity target_entity) {
        return DataManager.getMap(getCurrentMapName(target_entity));
    }
}

package net.nerdorg.minehop.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.entity.custom.EndEntity;
import net.nerdorg.minehop.entity.custom.ResetEntity;
import net.nerdorg.minehop.entity.custom.StartEntity;
import net.nerdorg.minehop.entity.custom.Zone;

import java.util.ArrayList;
import java.util.List;

public class ZoneUtil {
    public static String getCurrentMapName(Entity target_entity) {
        if (Minehop.playerMapLocation.containsKey(target_entity.getUuidAsString())) {
            return Minehop.playerMapLocation.get(target_entity.getUuidAsString()).getPairedMap();
        }

        return null;
    }

    public static DataManager.MapData getCurrentMap(Entity target_entity) {
        return DataManager.getMap(getCurrentMapName(target_entity));
    }

    public static TeleportTarget makeTeleportTarget(ServerWorld serverWorld, Vec3d targetLocation, float yaw, float pitch) {
        return new TeleportTarget(serverWorld, new Vec3d(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ()), Vec3d.ZERO, yaw, pitch, (playerEntity) -> {

        });
    }
}

package net.nerdorg.minehop.networking.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.nerdorg.minehop.Minehop;

public record ConfigSyncPayload(double sv_friction, double sv_accelerate,
                                double sv_airaccelerate, double sv_maxairspeed,
                                double speed_mul, double sv_gravity, double speedCap, boolean isHNS) implements CustomPayload {
    public static final Identifier HANDSHAKE_ID = Identifier.of(Minehop.MOD_ID, "config");
    public static final Id<ConfigSyncPayload> ID = new Id<>(HANDSHAKE_ID);
    public static final PacketCodec<PacketByteBuf, ConfigSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, ConfigSyncPayload::sv_friction,
            PacketCodecs.DOUBLE, ConfigSyncPayload::sv_accelerate,
            PacketCodecs.DOUBLE, ConfigSyncPayload::sv_airaccelerate,
            PacketCodecs.DOUBLE, ConfigSyncPayload::sv_maxairspeed,
            PacketCodecs.DOUBLE, ConfigSyncPayload::speed_mul,
            PacketCodecs.DOUBLE, ConfigSyncPayload::sv_gravity,
            PacketCodecs.DOUBLE, ConfigSyncPayload::speedCap,
            PacketCodecs.BOOLEAN, ConfigSyncPayload::isHNS,
            ConfigSyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

package net.nerdorg.minehop.networking.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.nerdorg.minehop.Minehop;

public record MapFinishPayload(float time) implements CustomPayload {
    public static final Identifier HANDSHAKE_ID = Identifier.of(Minehop.MOD_ID, "map_finish");
    public static final Id<MapFinishPayload> ID = new Id<>(HANDSHAKE_ID);
    public static final PacketCodec<PacketByteBuf, MapFinishPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, MapFinishPayload::time,
            MapFinishPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

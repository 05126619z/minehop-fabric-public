package net.nerdorg.minehop.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MyCustomPayload(Identifier id, PacketByteBuf buff) implements CustomPayload {
    public static final Id<MyCustomPayload> ID = null;

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

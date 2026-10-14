package dev.chickenthedev.stratagems.network;

import dev.chickenthedev.stratagems.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Client -> server: "I entered the code for this stratagem, arm it for my next throw."
public record ArmStratagemPayload(String stratagemId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ArmStratagemPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "arm_stratagem"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmStratagemPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> ByteBufCodecs.STRING_UTF8.encode(buf, payload.stratagemId()),
                    buf -> new ArmStratagemPayload(ByteBufCodecs.STRING_UTF8.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

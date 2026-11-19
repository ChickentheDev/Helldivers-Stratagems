package dev.chickenthedev.stratagems.network;

import dev.chickenthedev.stratagems.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Client -> server: "throw the beacon for my currently armed stratagem."
// Sent by the throw keybind, so the player doesn't have to hold the uplink.
// The server validates the armed state and the stratagem's own cooldown.
public record ThrowStratagemPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ThrowStratagemPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "throw_stratagem"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowStratagemPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {},
                    buf -> new ThrowStratagemPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

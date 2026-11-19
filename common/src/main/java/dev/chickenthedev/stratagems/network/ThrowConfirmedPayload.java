package dev.chickenthedev.stratagems.network;

import dev.chickenthedev.stratagems.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Server -> client: "your throw went through; this stratagem is now on
// cooldown for this many ticks." Lets the HUD show a per-stratagem timer
// that exactly mirrors the server, now that the uplink item no longer
// carries a shared cooldown the client could watch.
public record ThrowConfirmedPayload(String stratagemId, int cooldownTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ThrowConfirmedPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "throw_confirmed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowConfirmedPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.STRING_UTF8.encode(buf, payload.stratagemId());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.cooldownTicks());
                    },
                    buf -> new ThrowConfirmedPayload(
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

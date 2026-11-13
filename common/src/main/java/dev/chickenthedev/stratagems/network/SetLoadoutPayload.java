package dev.chickenthedev.stratagems.network;

import dev.chickenthedev.stratagems.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

// Client -> server: "equip these stratagems on my uplink." The server validates
// the ids, writes them into the uplink's CUSTOM_DATA, and syncs the stack back.
public record SetLoadoutPayload(List<String> stratagemIds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetLoadoutPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_loadout"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<String>> LIST_CODEC =
            StreamCodec.of(
                    (buf, list) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, list.size());
                        for (String s : list) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                    },
                    buf -> {
                        int size = ByteBufCodecs.VAR_INT.decode(buf);
                        java.util.ArrayList<String> list = new java.util.ArrayList<>(size);
                        for (int i = 0; i < size; i++) list.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                        return list;
                    });

    public static final StreamCodec<RegistryFriendlyByteBuf, SetLoadoutPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> LIST_CODEC.encode(buf, payload.stratagemIds()),
                    buf -> new SetLoadoutPayload(LIST_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

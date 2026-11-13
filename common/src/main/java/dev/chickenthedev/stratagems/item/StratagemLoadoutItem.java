package dev.chickenthedev.stratagems.item;

import dev.chickenthedev.stratagems.StratagemLoadout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

// Opens the loadout picker: choose which 4 stratagems the uplink has equipped.
// The picker itself is client-only, so the screen is opened through a hook the
// client entrypoint sets (this common class must never reference client classes,
// or the dedicated server fails to load the item at all).
public class StratagemLoadoutItem extends Item {

    /** Set by the client entrypoint: opens the picker for the given loadout. */
    public static Consumer<List<String>> screenOpener;

    public StratagemLoadoutItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) return InteractionResult.SUCCESS_SERVER;
        ItemStack uplink = StratagemLoadout.findUplink(player);
        if (uplink == null) {
            player.sendOverlayMessage(Component.translatable("message.stratagems.need_uplink"));
            return InteractionResult.SUCCESS;
        }
        if (screenOpener != null) screenOpener.accept(StratagemLoadout.get(uplink));
        return InteractionResult.SUCCESS;
    }
}

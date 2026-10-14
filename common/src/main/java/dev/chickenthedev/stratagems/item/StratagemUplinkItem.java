package dev.chickenthedev.stratagems.item;

import dev.chickenthedev.stratagems.StratagemManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

// The handheld device used to call stratagems. Enter a code with the arrow keys
// (stratagem keybind), then right-click to throw the beacon for the armed stratagem.
public class StratagemUplinkItem extends Item {

    public StratagemUplinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            return StratagemManager.tryThrow(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}

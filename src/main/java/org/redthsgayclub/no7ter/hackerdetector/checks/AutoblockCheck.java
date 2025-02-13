package org.redthsgayclub.no7ter.hackerdetector.checks;

import org.redthsgayclub.no7ter.config.ConfigHandler;
import org.redthsgayclub.no7ter.hackerdetector.data.PlayerDataSamples;
import org.redthsgayclub.no7ter.hackerdetector.utils.ViolationLevelTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class AutoblockCheck extends Check {

    @Override
    public String getCheatName() {
        return "Autoblock";
    }

    @Override
    public String getCheatDescription() {
        return "The player can attack while their sword is blocked";
    }

    @Override
    public boolean canSendReport() {
        return true;
    }

    @Override
    public void performCheck(EntityPlayer player, PlayerDataSamples data) {
        super.checkViolationLevel(player, this.check(player, data), data.autoblockAVL);
    }

    @Override
    public boolean check(EntityPlayer player, PlayerDataSamples data) {
        if (data.hasSwung) {
            final ItemStack itemStack = player.getHeldItem();
            if (itemStack != null && itemStack.getItem() instanceof ItemSword) {
                if (data.useItemTime > 5) {
                    data.autoblockAVL.add(2);
                    if (ConfigHandler.debugLogging) {
                        this.log(player, data, data.autoblockAVL, " | useItemTime " + data.useItemTime);
                    }
                    return true;
                } else if (data.useItemTime == 0) {
                    data.autoblockAVL.substract(2);
                }
            }
        }
        return false;
    }

    public static ViolationLevelTracker newVL() {
        return new ViolationLevelTracker(3);
    }

}


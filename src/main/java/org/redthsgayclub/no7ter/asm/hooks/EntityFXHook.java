package org.redthsgayclub.no7ter.asm.hooks;

import org.redthsgayclub.no7ter.config.ConfigHandler;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;

@SuppressWarnings("unused")
public class EntityFXHook {

    public static boolean shouldHideParticle(EntityFX entityFXIn, Entity entityIn) {
        return ConfigHandler.clearVision && entityFXIn.getDistanceSq(entityIn.posX, entityIn.posY + entityIn.getEyeHeight(), entityIn.posZ) < 0.5625d;
    }

}

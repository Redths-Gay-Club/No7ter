package org.redthsgayclub.no7ter.asm.hooks;

import net.minecraft.entity.EntityLivingBase;

@SuppressWarnings("unused")
public class RendererLivingEntityHook_HitColor {

    private static int hitColor;

    public static float getRed(float r, EntityLivingBase entity) {
        // Return default value
        return r;
    }

    public static float getGreen(float g) {
        // Return default value
        return g;
    }

    public static float getBlue(float b) {
        // Return default value
        return b;
    }

    public static float getAlpha(float a) {
        // Return default value
        return a;
    }
}

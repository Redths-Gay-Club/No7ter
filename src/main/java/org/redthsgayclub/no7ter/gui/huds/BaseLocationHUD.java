package org.redthsgayclub.no7ter.gui.huds;

import org.redthsgayclub.no7ter.chat.ChatListener;
import org.redthsgayclub.no7ter.config.ConfigHandler;
import org.redthsgayclub.no7ter.enums.MegaWallsMap;
import org.redthsgayclub.no7ter.events.MegaWallsGameEvent;
import org.redthsgayclub.no7ter.scoreboard.ScoreboardTracker;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BaseLocationHUD extends AbstractRenderer {

    public static BaseLocationHUD instance;
    private MegaWallsMap currentMap;

    public BaseLocationHUD() {
        super(ConfigHandler.baseLocationHUDPosition);
        instance = this;
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @Override
    public void render(ScaledResolution resolution) {
        final String text = currentMap.getPlayerBaseLocation(mc.thePlayer);
        this.guiPosition.updateAdjustedAbsolutePosition(resolution, mc.fontRendererObj.getStringWidth(text), mc.fontRendererObj.FONT_HEIGHT);
        drawCenteredString(mc.fontRendererObj, text, this.guiPosition.getAbsoluteRenderX(), this.guiPosition.getAbsoluteRenderY(), 0xFFFFFF);
    }

    @Override
    public void renderDummy() {
        drawCenteredString(mc.fontRendererObj, EnumChatFormatting.GREEN + "GREEN", this.guiPosition.getAbsoluteRenderX(), this.guiPosition.getAbsoluteRenderY(), 0xFFFFFF);
    }

    @Override
    public boolean isEnabled(long currentTimeMillis) {
        return ConfigHandler.showBaseLocationHUD && (ScoreboardTracker.isInMwGame || ScoreboardTracker.isMWReplay) && this.currentMap != null;
    }

    @SubscribeEvent
    public void onMWEvent(MegaWallsGameEvent event) {
        if (!ConfigHandler.showBaseLocationHUD) return;
        if (event.getType() == MegaWallsGameEvent.EventType.CONNECT || event.getType() == MegaWallsGameEvent.EventType.GAME_START) {
            if (mc.thePlayer != null) {
                ChatListener.setMegaWallsMap();
            }
        }
    }

    public void setCurrentMap(String mapName) {
        if (mapName == null) {
            currentMap = null;
        } else {
            currentMap = MegaWallsMap.fromName(mapName);
        }
    }

}

package org.redthsgayclub.no7ter.asm.hooks;

import org.redthsgayclub.no7ter.config.ConfigHandler;
import org.redthsgayclub.no7ter.features.FinalKillCounter;
import org.redthsgayclub.no7ter.gui.huds.FKCounterHUD;
import org.redthsgayclub.no7ter.gui.huds.LastWitherHPHUD;
import org.redthsgayclub.no7ter.scoreboard.ScoreboardTracker;
import net.minecraft.client.gui.FontRenderer;

@SuppressWarnings("unused")
public class GuiIngameHook {

    public static String cancelHungerTitle(String subtitle) {
        if (ConfigHandler.hideHungerTitleInMW && subtitle.contains("Get to the middle to stop the hunger!")) {
            return "";
        }
        return subtitle;
    }

    public static String getSidebarTextLine(String textIn, int lineNumber) {
        if (ConfigHandler.witherHUDinSidebar && lineNumber == 12 && ConfigHandler.showLastWitherHUD && ScoreboardTracker.isInMwGame && ScoreboardTracker.getParser().isOnlyOneWitherAlive()) {
            return LastWitherHPHUD.instance.displayText;
        }
        if (ConfigHandler.fkcounterHUDinSidebar && lineNumber == 11 && ConfigHandler.showfkcounterHUD && ScoreboardTracker.isInMwGame && FinalKillCounter.getGameId() != null) {
            return FKCounterHUD.instance.displayText;
        }
        return textIn;
    }

    public static int getSidebarTextLineWidth(int width, FontRenderer fontRenderer, boolean redNumbers) {
        if (ConfigHandler.witherHUDinSidebar && ConfigHandler.showLastWitherHUD && ScoreboardTracker.isInMwGame && ScoreboardTracker.getParser().isOnlyOneWitherAlive()) {
            width = Math.max(width, fontRenderer.getStringWidth(LastWitherHPHUD.instance.displayText + (redNumbers ? ": 12" : "")));
        }
        if (ConfigHandler.fkcounterHUDinSidebar && ConfigHandler.showfkcounterHUD && ScoreboardTracker.isInMwGame && FinalKillCounter.getGameId() != null) {
            width = Math.max(width, fontRenderer.getStringWidth(FKCounterHUD.instance.displayText + (redNumbers ? ": 11" : "")));
        }
        return width;
    }

}

package org.redthsgayclub.no7ter.features;

import org.redthsgayclub.no7ter.api.apikey.HypixelApiKeyUtil;
import org.redthsgayclub.no7ter.api.exceptions.ApiException;
import org.redthsgayclub.no7ter.api.hypixelplayerdataparser.LoginData;
import org.redthsgayclub.no7ter.api.hypixelplayerdataparser.MegaWallsClassSkinData;
import org.redthsgayclub.no7ter.api.hypixelplayerdataparser.MegaWallsClassStats;
import org.redthsgayclub.no7ter.api.requests.HypixelPlayerData;
import org.redthsgayclub.no7ter.asm.hooks.GuiScreenHook;
import org.redthsgayclub.no7ter.chat.ChatUtil;
import org.redthsgayclub.no7ter.enums.MWClass;
import org.redthsgayclub.no7ter.events.MegaWallsGameEvent;
import org.redthsgayclub.no7ter.utils.DateUtil;
import org.redthsgayclub.no7ter.utils.DelayedTask;
import org.redthsgayclub.no7ter.utils.MultithreadingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MegaWallsEndGameStats {

    private static final Pattern RANDOM_CLASS_PATTERN = Pattern.compile("^Random class: (\\w+)");
    private static MWClass selectedClass;
    private static boolean isRandom = false;
    /*Data downloaded at the start of the game*/
    private static MegaWallsClassStats mwClassStartGameStats;
    private static long timestampGameStart;
    private static IChatComponent endGameStatsMessage;

    @SubscribeEvent
    public void onMwGame(MegaWallsGameEvent event) {
        if (event.getType() == MegaWallsGameEvent.EventType.GAME_START) {
            onGameStart();
        } else if (event.getType() == MegaWallsGameEvent.EventType.GAME_END) {
            new DelayedTask(MegaWallsEndGameStats::onGameEnd, 300);
        }
    }

    private static void onGameStart() {
        if (HypixelApiKeyUtil.apiKeyIsNotSetup()) {
            return;
        }
        timestampGameStart = (new Date()).getTime();
        final EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) {
            return;
        }
        final String uuid = thePlayer.getUniqueID().toString();
        MultithreadingUtil.addTaskToQueue(() -> {
            try {
                final HypixelPlayerData playerdata = new HypixelPlayerData(uuid);
                if (!isRandom) {
                    selectedClass = MWClass.fromTagOrName(new MegaWallsClassSkinData(playerdata.getPlayerData()).getCurrentmwclass());
                }
                if (selectedClass == null) {
                    return null;
                }
                mwClassStartGameStats = new MegaWallsClassStats(playerdata.getPlayerData(), selectedClass.className);
            } catch (ApiException ignored) {}
            isRandom = false;
            return null;
        });
    }

    private static void onGameEnd() {
        if (HypixelApiKeyUtil.apiKeyIsNotSetup() || mwClassStartGameStats == null || selectedClass == null) {
            return;
        }
        final EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) {
            return;
        }
        final String uuid = thePlayer.getUniqueID().toString();
        MultithreadingUtil.addTaskToQueue(() -> {
            try {
                final HypixelPlayerData playerdata = new HypixelPlayerData(uuid);
                final MegaWallsClassStats endGameStats = new MegaWallsClassStats(playerdata.getPlayerData(), selectedClass.className);
                endGameStats.minus(mwClassStartGameStats);
                final String formattedName = new LoginData(playerdata.getPlayerData()).getFormattedName();
                final String gameDuration = DateUtil.timeSince(timestampGameStart);
                endGameStatsMessage = endGameStats.getGameStatMessage(formattedName, gameDuration);
                mwClassStartGameStats = null;
                ChatUtil.addChatMessage(new ChatComponentText(ChatUtil.getTagMW() + EnumChatFormatting.YELLOW + "Click to view the stats of your " + EnumChatFormatting.AQUA + "Mega Walls " + EnumChatFormatting.YELLOW + "game!")
                        .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, GuiScreenHook.MW_GAME_END_STATS))));
            } catch (Exception ignored) {}
            return null;
        });
    }

    public static void printGameStatsMessage() {
        if (endGameStatsMessage != null) {
            ChatUtil.addChatMessage(endGameStatsMessage);
        } else {
            ChatUtil.addChatMessage(ChatUtil.getTagMW() + EnumChatFormatting.RED + "No game stats available");
        }
    }

    public static boolean processMessage(String msg) {
        final Matcher matcher = RANDOM_CLASS_PATTERN.matcher(msg);
        if (matcher.find()) {
            selectedClass = MWClass.fromTagOrName(matcher.group(1));
            isRandom = true;
            return true;
        }
        return false;
    }

}

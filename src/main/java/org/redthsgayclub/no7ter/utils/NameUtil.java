package org.redthsgayclub.no7ter.utils;

import com.mojang.authlib.GameProfile;
import org.redthsgayclub.no7ter.asm.accessors.EntityPlayerAccessor;
import org.redthsgayclub.no7ter.asm.accessors.NetworkPlayerInfoAccessor;
import org.redthsgayclub.no7ter.asm.hooks.NetHandlerPlayClientHook;
import org.redthsgayclub.no7ter.chat.ChatHandler;
import org.redthsgayclub.no7ter.config.ConfigHandler;
import org.redthsgayclub.no7ter.data.AliasData;
import org.redthsgayclub.no7ter.data.MWPlayerData;
import org.redthsgayclub.no7ter.data.PrestigeVCache;
import org.redthsgayclub.no7ter.data.ScangameData;
import org.redthsgayclub.no7ter.enums.MWClass;
import org.redthsgayclub.no7ter.features.LeatherArmorManager;
import org.redthsgayclub.no7ter.features.SquadHandler;
import org.redthsgayclub.no7ter.nocheaters.WDR;
import org.redthsgayclub.no7ter.nocheaters.WarningMessagesHandler;
import org.redthsgayclub.no7ter.nocheaters.WdrData;
import org.redthsgayclub.no7ter.scoreboard.ScoreboardTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What this class does :
 * on new NetworkPlayerInfo instance creation :
 * - updates/creates MWPlayerData stored in the cache
 * - assigns custom displayName or null
 * - assigns the finals of the player
 * on Scoreboard Team packets :
 * - transforms the name in the tablist
 * on playerJoin :
 * - look the MWPlayerData and assigns the custom fields in EntityPlayer (prestige 5)
 * - print warning message
 * <p>
 * When the world loads, for EntityPlayerSP it does :
 * - fire on playerjoin event twice
 * - then receive two new Networkplayerinfo packets
 * When swapping lobbys on hypixel for entityplayerSP :
 * - fires the playerjoin event 6 times
 * - then receive two new Networkplayerinfo packets
 * When swapping lobbys on hypixel for other players :
 * - receive two new Networkplayerinfo packets
 * - fires the playerjoin event once
 * - then receives one networkplayerinfo packet
 * When a player enters our render distance :
 * - receive once new Networkplayerinfo packet
 * - fire the playerjoin event once
 */
public class NameUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final String WARNING_ICON = EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "\u26a0 " + EnumChatFormatting.RESET;
    public static final String RED_WARNING_ICON = EnumChatFormatting.DARK_RED.toString() + EnumChatFormatting.BOLD + "\u26a0 " + EnumChatFormatting.RESET;
    public static final String PINK_WARNING_ICON = EnumChatFormatting.LIGHT_PURPLE.toString() + EnumChatFormatting.BOLD + "\u26a0 " + EnumChatFormatting.RESET;
    public static final String SQUAD_ICON = EnumChatFormatting.GOLD + "[" + EnumChatFormatting.DARK_GREEN + "S" + EnumChatFormatting.GOLD + "] " + EnumChatFormatting.RESET;
    private static final IChatComponent IWARNING_ICON = new ChatComponentText(WARNING_ICON);
    private static final IChatComponent IRED_WARNING_ICON = new ChatComponentText(RED_WARNING_ICON);
    private static final IChatComponent IPINK_WARNING_ICON = new ChatComponentText(PINK_WARNING_ICON);
    private static final IChatComponent ISQUAD_ICON = new ChatComponentText(SQUAD_ICON);
    private static final List<IChatComponent> ALL_ICONS_LIST = Arrays.asList(IWARNING_ICON, IRED_WARNING_ICON, IPINK_WARNING_ICON, ISQUAD_ICON);
    private static final Pattern PATTERN_CLASS_TAG = Pattern.compile("\\[([A-Z]{3})\\]");
    private static final Set<UUID> warningMsgPrinted = new HashSet<>();

    /**
     * This updates the infos storred in MWPlayerData.dataCache for the player : playername
     * and refreshes the name in the tablist and the nametag.
     * Set refreshDisplayName to true to fire the NameFormat Event and
     * update the name of the player as well, in case you changed it via a command
     * for example : /squad add player as aliasname
     */
    public static void updateMWPlayerDataAndEntityData(String playername, boolean refreshDisplayName) {
        if (isValidMinecraftName(playername)) {
            final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.getPlayerInfo(playername);
            if (networkPlayerInfo != null) {
                ((NetworkPlayerInfoAccessor) networkPlayerInfo).setCustomDisplayname(fetchMWPlayerData(networkPlayerInfo.getGameProfile(), true).displayName);
            }
            final EntityPlayer player = mc.theWorld.getPlayerEntityByName(playername);
            if (player != null) {
                updateEntityPlayerFields(player, false);
                if (refreshDisplayName) {
                    player.refreshDisplayName();
                }
            }
        }
    }

    public static void updateMWPlayerDataAndEntityData(EntityPlayer player, boolean refreshDisplayName) {
        final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.getPlayerInfo(player.getName());
        if (networkPlayerInfo != null) {
            ((NetworkPlayerInfoAccessor) networkPlayerInfo).setCustomDisplayname(fetchMWPlayerData(networkPlayerInfo.getGameProfile(), true).displayName);
        }
        updateEntityPlayerFields(player, false);
        if (refreshDisplayName) {
            player.refreshDisplayName();
        }
    }

    /**
     * This updates the infos storred in GameProfile.MWPlayerData and refreshes the name in the tablist and the nametag
     */
    public static void updateMWPlayerDataAndEntityData(NetworkPlayerInfo networkPlayerInfo, boolean refreshDisplayName) {
        ((NetworkPlayerInfoAccessor) networkPlayerInfo).setCustomDisplayname(fetchMWPlayerData(networkPlayerInfo.getGameProfile(), true).displayName);
        final EntityPlayer player = mc.theWorld.getPlayerEntityByName(networkPlayerInfo.getGameProfile().getName());
        if (player != null) {
            updateEntityPlayerFields(player, false);
            if (refreshDisplayName) {
                player.refreshDisplayName();
            }
        }
    }

    public static void refreshAllNamesInWorld() {
        for (final NetworkPlayerInfo netInfo : mc.getNetHandler().getPlayerInfoMap()) {
            if (netInfo != null) updateMWPlayerDataAndEntityData(netInfo, true);
        }
    }

    private static final Pattern MINECRAFT_NAME_PATTERN = Pattern.compile("\\w{1,16}");

    public static boolean isValidMinecraftName(String playername) {
        return !StringUtil.isNullOrEmpty(playername) &&
                (MINECRAFT_NAME_PATTERN.matcher(playername).matches() || ScoreboardTracker.isReplayMode);
    }

    public static void onScoreboardPacket(String playername) {
        if (isValidMinecraftName(playername)) {
            final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.getPlayerInfo(playername);
            if (networkPlayerInfo != null) {
                final MWPlayerData.PlayerData mwPlayerData = fetchMWPlayerData(networkPlayerInfo.getGameProfile(), true);
                ((NetworkPlayerInfoAccessor) networkPlayerInfo).setCustomDisplayname(mwPlayerData.displayName);
                if (mc.theWorld != null) {
                    final EntityPlayer player;
                    if (playername.equals(ConfigHandler.hypixelNick)) {
                        player = mc.thePlayer;
                    } else {
                        player = mc.theWorld.getPlayerEntityByName(networkPlayerInfo.getGameProfile().getName());
                    }
                    if (player != null) {
                        final EntityPlayerAccessor playerAccessor = (EntityPlayerAccessor) player;
                        final int oldColor = playerAccessor.getPlayerTeamColorInt();
                        playerAccessor.setPlayerTeamColor(mwPlayerData.teamColor);
                        playerAccessor.setPlayerTeamColorInt(ColorUtil.getColorInt(mwPlayerData.teamColor));
                        playerAccessor.setMWClass(mwPlayerData.mwClass);
                        LeatherArmorManager.onColorChange(player, oldColor, playerAccessor.getPlayerTeamColorInt());
                    }
                }
            }
        }
    }

    /**
     * Updates the custom fields in the entity player, such as the prestige V tag,
     * so current prestige 4 tag, the icon on nametags and also checks to print
     * the warning message if player was reported and is currently joining the world
     */
    public static void updateEntityPlayerFields(EntityPlayer player, boolean onPlayerJoin) {

        final MWPlayerData.PlayerData mwPlayerData = MWPlayerData.get(player.getUniqueID());
        if (mwPlayerData == null) {
            return;
        }

        final EntityPlayerAccessor playerAccessor = (EntityPlayerAccessor) player;
        playerAccessor.setPrestige4Tag(mwPlayerData.originalP4Tag);
        playerAccessor.setPrestige5Tag(mwPlayerData.P5Tag);

        final int oldColor = playerAccessor.getPlayerTeamColorInt();
        playerAccessor.setPlayerTeamColor(mwPlayerData.teamColor);
        if (ConfigHandler.pinkSquadmates && mwPlayerData.squadname != null) {
            playerAccessor.setPlayerTeamColorInt(ColorUtil.getColorInt('d'));
        } else {
            playerAccessor.setPlayerTeamColorInt(ColorUtil.getColorInt(mwPlayerData.teamColor));
        }
        LeatherArmorManager.onColorChange(player, oldColor, playerAccessor.getPlayerTeamColorInt());

        playerAccessor.setMWClass(mwPlayerData.mwClass);

        player.getPrefixes().removeAll(ALL_ICONS_LIST);

        if (mwPlayerData.extraPrefix != null) {
            if (mwPlayerData.extraPrefix == ISQUAD_ICON) {
                if (!ConfigHandler.squadIconTabOnly) {
                    player.addPrefix(mwPlayerData.extraPrefix);
                }
            } else {
                if (!ConfigHandler.warningIconsTabOnly) {
                    player.addPrefix(mwPlayerData.extraPrefix);
                }
            }
        }

        if (onPlayerJoin && mwPlayerData.wdr != null) {
            final String playerName = player.getName();
            if (ConfigHandler.warningMessages) {
                if (!warningMsgPrinted.contains(player.getUniqueID())) {
                    warningMsgPrinted.add(player.getUniqueID());
                    ChatHandler.deleteWarningFromChat(playerName);
                    WarningMessagesHandler.printWarningMessage(
                            player.getUniqueID(),
                            ScorePlayerTeam.formatPlayerName(player.getTeam(), playerName),
                            playerName,
                            mwPlayerData.wdr
                    );
                }
            }
        }

    }

    public static void clearWarningMessagesPrinted() {
        warningMsgPrinted.clear();
    }

    /**
     * Transforms the infos storred in MWPlayerData.dataCache and returns the MWplayerData for the player
     * For each new player spawned in the world it will create a new networkplayerinfo instance
     * a rerun all the code in the method to generate the MWPlayerData instance
     */
    @Nonnull
    public static MWPlayerData.PlayerData fetchMWPlayerData(GameProfile gameProfileIn, boolean forceRefresh) {

        final UUID id = gameProfileIn.getId();
        MWPlayerData.PlayerData mwPlayerData = MWPlayerData.get(id);

        if (!forceRefresh && mwPlayerData != null) {
            return mwPlayerData;
        }

        final String username = gameProfileIn.getName();
        final String uuid = id.toString().replace("-", "");
        final WDR wdr = WdrData.getWdr(id, username);
        String extraPrefix = "";
        IChatComponent iExtraPrefix = null;
        final String squadname = SquadHandler.getSquad().get(username);
        final boolean isSquadMate = squadname != null;

        if (isSquadMate) {
            if (ConfigHandler.squadIconOnNames || ConfigHandler.squadIconTabOnly) {
                extraPrefix = SQUAD_ICON;
                iExtraPrefix = ISQUAD_ICON;
            }
        } else {
            if (ConfigHandler.warningIconsOnNames || ConfigHandler.warningIconsTabOnly) {
                if (wdr != null) {
                    if (wdr.shouldPutRedIcon()) {
                        extraPrefix = RED_WARNING_ICON;
                        iExtraPrefix = IRED_WARNING_ICON;
                    } else {
                        extraPrefix = WARNING_ICON;
                        iExtraPrefix = IWARNING_ICON;
                    }
                } else {
                    if (ScangameData.doesPlayerFlag(id)) {
                        extraPrefix = PINK_WARNING_ICON;
                        iExtraPrefix = IPINK_WARNING_ICON;
                    }
                }
            }
        }

        IChatComponent displayName = null;
        String formattedPrestigeVstring = null;
        String colorSuffix = null;
        char teamColor = '\0';
        MWClass mwClass = null;
        if (mc.theWorld != null) {
            final ScorePlayerTeam team = mc.theWorld.getScoreboard().getPlayersTeam(username);
            if (team != null) {
                final String teamprefix = team.getColorPrefix();
                colorSuffix = team.getColorSuffix();
                teamColor = StringUtil.getLastColorCharOf(teamprefix);
                mwClass = MWClass.fromTeamTag(ScoreboardTracker.isMWReplay ? teamprefix : colorSuffix);
                if (ConfigHandler.prestigeV && colorSuffix.contains(EnumChatFormatting.GOLD.toString())) {
                    final Matcher matcher = PATTERN_CLASS_TAG.matcher(colorSuffix);
                    if (matcher.find()) {
                        final String tag = matcher.group(1);
                        final EnumChatFormatting prestigeVcolor = PrestigeVCache.checkCacheAndUpdate(uuid, gameProfileIn.getName(), tag);
                        if (prestigeVcolor != null) {
                            formattedPrestigeVstring = " " + prestigeVcolor + "[" + tag + "]";
                        }
                    }
                }
                final boolean isobf = teamprefix.contains("\u00a7k");
                final boolean isNicked = id.version() == 1;
                final String alias = AliasData.getAlias(isNicked ? username : uuid);
                if (iExtraPrefix != null || isSquadMate || formattedPrestigeVstring != null || alias != null) {
                    displayName = new ChatComponentText(
                            (isobf ? "" : extraPrefix)
                                    + teamprefix
                                    + (isSquadMate ? squadname : username)
                                    + (formattedPrestigeVstring != null ? formattedPrestigeVstring : colorSuffix)
                                    + (isobf || alias == null ? "" : EnumChatFormatting.RESET + " (" + EnumChatFormatting.GOLD + alias + EnumChatFormatting.RESET + ")")
                    );
                }
            } else if (mc.thePlayer != null && mc.thePlayer.getName().equals(username)) {
                if (!ConfigHandler.hypixelNick.isEmpty()) {
                    final ScorePlayerTeam teamNick = mc.theWorld.getScoreboard().getPlayersTeam(ConfigHandler.hypixelNick);
                    if (teamNick != null) {
                        teamColor = StringUtil.getLastColorCharOf(teamNick.getColorPrefix());
                    }
                }
            }
        }

        if (mwPlayerData == null) {
            mwPlayerData = new MWPlayerData.PlayerData(id, wdr, iExtraPrefix, squadname, displayName, colorSuffix, formattedPrestigeVstring, teamColor, mwClass);
        } else {
            mwPlayerData.setData(wdr, iExtraPrefix, squadname, displayName, colorSuffix, formattedPrestigeVstring, teamColor, mwClass);
        }

        return mwPlayerData;

    }

    private static final Pattern obfPattern = Pattern.compile("\u00a7k[OX]*");

    private static String deobfString(String obfText) {
        return obfPattern.matcher(obfText).replaceAll("");
    }

    /**
     * Returns the formatted name of the player, additionnal icons, squadname, alias and prestive V tag included
     * Same method that the one in {@link net.minecraft.client.gui.GuiPlayerTabOverlay#getPlayerName}
     */
    public static String getFormattedName(String playername) {
        final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.getPlayerInfo(playername);
        if (networkPlayerInfo == null) {
            return playername;
        }
        return getFormattedName(networkPlayerInfo);
    }

    /**
     * Returns the formatted name of the player, additionnal icons, squadname, alias and prestive V tag included
     * Same method that the one in {@link net.minecraft.client.gui.GuiPlayerTabOverlay#getPlayerName}
     */
    public static String getFormattedName(NetworkPlayerInfo networkPlayerInfo) {
        if (networkPlayerInfo.getDisplayName() == null) {
            return ScorePlayerTeam.formatPlayerName(networkPlayerInfo.getPlayerTeam(), networkPlayerInfo.getGameProfile().getName());
        }
        return networkPlayerInfo.getDisplayName().getFormattedText();
    }

    /**
     * Returns the formatted team name with additionnaly a custom prestige V tag and a squadname
     * This doesn't return the icons in front that the player may have.
     */
    public static String getFormattedNameWithoutIcons(String playername) {
        final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.getPlayerInfo(playername);
        if (networkPlayerInfo == null) {
            return SquadHandler.getSquadname(playername);
        }
        return getFormattedNameWithoutIcons(networkPlayerInfo);
    }

    /**
     * Returns the formatted team name with additionnaly a custom prestige V tag and a squadname
     * This doesn't return the icons in front that the player may have.
     */
    public static String getFormattedNameWithoutIcons(NetworkPlayerInfo networkPlayerInfo) {
        final MWPlayerData.PlayerData mwPlayerData = MWPlayerData.get(networkPlayerInfo.getGameProfile().getId());
        if (mwPlayerData != null && mwPlayerData.P5Tag != null && mwPlayerData.originalP4Tag != null) {
            return formatPlayerNameUnscrambled(networkPlayerInfo.getPlayerTeam(), networkPlayerInfo.getGameProfile().getName()).replace(mwPlayerData.originalP4Tag, mwPlayerData.P5Tag);
        }
        return formatPlayerNameUnscrambled(networkPlayerInfo.getPlayerTeam(), networkPlayerInfo.getGameProfile().getName());
    }

    /**
     * Equivalent of {@link ScorePlayerTeam#formatPlayerName}
     * but with eventually a squadname
     */
    private static String formatPlayerNameUnscrambled(ScorePlayerTeam team, String playername) {
        if (team == null) {
            return SquadHandler.getSquadname(playername);
        }
        return deobfString(team.getColorPrefix()) + SquadHandler.getSquadname(playername) + team.getColorSuffix();
    }

    /**
     * Used for /scangame
     */
    public static IChatComponent getFormattedNameWithPlanckeClickEvent(NetworkPlayerInfo networkPlayerInfoIn) {
        final String formattedName;
        if (networkPlayerInfoIn.getPlayerTeam() == null) {
            formattedName = networkPlayerInfoIn.getGameProfile().getName();
        } else {
            final ScorePlayerTeam team = networkPlayerInfoIn.getPlayerTeam();
            formattedName = deobfString(team.getColorPrefix()) + networkPlayerInfoIn.getGameProfile().getName() + team.getColorSuffix();
        }
        return new ChatComponentText(formattedName)
                .setChatStyle(new ChatStyle()
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW + "Click to see the mega walls stats of that player")))
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plancke " + networkPlayerInfoIn.getGameProfile().getName() + " mw")));
    }

    /**
     * Returns true if it's the uuid of an NPC
     * from experimentation, on Hypixel :
     * - nicked players are v1
     * - NPCs are v2
     * - real players are v4
     */
    public static boolean filterNPC(UUID uuid) {
        return uuid.version() == 2;
    }

    public static boolean isntRealPlayer(UUID uuid) {
        return uuid.version() != 4;
    }

    /**
     * Returns true if the player is using a random class
     */
    public static boolean isPlayerUsingRandom(NetworkPlayerInfo networkPlayerInfo) {
        final String randomSkinLocation = "512a44f6c022dfaa6f61274c85aa1594cb304f0136fd5d1d3a27c1379e875692";
        if (!networkPlayerInfo.hasLocationSkin()) {
            return false;
        }
        return randomSkinLocation.equals(networkPlayerInfo.getLocationSkin().toString().substring(16));
    }

}

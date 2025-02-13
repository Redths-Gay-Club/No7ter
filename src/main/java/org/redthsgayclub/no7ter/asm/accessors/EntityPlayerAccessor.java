package org.redthsgayclub.no7ter.asm.accessors;

import org.redthsgayclub.no7ter.enums.MWClass;
import org.redthsgayclub.no7ter.hackerdetector.data.PlayerDataSamples;

public interface EntityPlayerAccessor {
    String getPrestige4Tag();
    void setPrestige4Tag(String prestige4tag);
    String getPrestige5Tag();
    void setPrestige5Tag(String prestige5tag);
    PlayerDataSamples getPlayerDataSamples();
    char getPlayerTeamColor();
    void setPlayerTeamColor(char color);
    int getPlayerTeamColorInt();
    void setPlayerTeamColorInt(int color);
    MWClass getMWClass();
    void setMWClass(MWClass mwclass);
}

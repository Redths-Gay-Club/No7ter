package org.redthsgayclub.no7ter.asm.accessors;

import net.minecraft.client.gui.ChatLine;

import java.util.List;

public interface GuiNewChatAccessor {
    List<ChatLine> getChatLines();
    List<ChatLine> getDrawnChatLines();
}

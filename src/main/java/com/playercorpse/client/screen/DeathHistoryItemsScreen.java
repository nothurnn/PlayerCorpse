package com.playercorpse.client.screen;

import com.playercorpse.history.DeathHistoryEntry;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeathHistoryItemsScreen extends Screen {
   private static final int COLUMNS = 9;
   private static final int SLOT_SIZE = 18;
   private final Screen parent;
   private final DeathHistoryEntry entry;

   public DeathHistoryItemsScreen(Screen parent, DeathHistoryEntry entry) {
      super(Component.literal("Death History - Items"));
      this.parent = parent;
      this.entry = entry;
   }

   protected void init() {
      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> Minecraft.getInstance().setScreen(this.parent))
            .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
            .build()
      );
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      List<ItemStack> items = this.entry.items();
      int gridWidth = 162;
      int startX = (this.width - gridWidth) / 2;
      int startY = 40;
      guiGraphics.drawCenteredString(this.font, this.entry.playerName() + "'s items", this.width / 2, startY - 16, 16777215);
      ItemStack hovered = ItemStack.EMPTY;

      for (int i = 0; i < items.size(); i++) {
         ItemStack stack = items.get(i);
         if (!stack.isEmpty()) {
            int col = i % 9;
            int row = i / 9;
            int x = startX + col * 18 + 1;
            int y = startY + row * 18 + 1;
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 1426063360);
            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);
            if (mouseX >= x - 1 && mouseX < x + 17 && mouseY >= y - 1 && mouseY < y + 17) {
               hovered = stack;
            }
         }
      }

      if (!hovered.isEmpty()) {
         guiGraphics.renderTooltip(this.font, hovered, mouseX, mouseY);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}

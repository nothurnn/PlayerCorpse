package com.playercorpse.client.screen;

import com.playercorpse.PlayerCorpseNetworking;
import com.playercorpse.menu.PlayerCorpseMenu;
import com.playercorpse.menu.PlayerCorpseMenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class PlayerCorpseScreen extends AbstractContainerScreen<PlayerCorpseMenu> {
   private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

   public PlayerCorpseScreen(PlayerCorpseMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
      this.imageWidth = 176;
      this.imageHeight = 272;
      this.inventoryLabelY = 174;
   }

   protected void init() {
      super.init();
      int buttonWidth = this.imageWidth - 16;
      this.addRenderableWidget(
         Button.builder(
               Component.literal("Transfer Items"),
               button -> PacketDistributor.sendToServer(new PlayerCorpseNetworking.TransferItemsPayload(), new CustomPacketPayload[0])
            )
            .bounds(this.leftPos + 8, this.topPos + 146, buttonWidth, 18)
            .build()
      );
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.blit(PlayerCorpseMenuLayout.STORAGE_PANEL_TEXTURE, this.leftPos, this.topPos + 18, 0, 0, 176, 122);
      guiGraphics.blit(CONTAINER_BACKGROUND, this.leftPos, this.topPos + 170, 0, 126, 176, 96);
   }
}

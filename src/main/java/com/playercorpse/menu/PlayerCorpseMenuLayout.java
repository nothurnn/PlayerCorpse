package com.playercorpse.menu;

import net.minecraft.resources.ResourceLocation;

public final class PlayerCorpseMenuLayout {
   public static final ResourceLocation STORAGE_PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
      "playercorpse", "textures/gui/container/corpse_storage.png"
   );
   public static final int IMAGE_WIDTH = 176;
   public static final int COLUMNS = 9;
   public static final int ROW_HEIGHT = 18;
   public static final int MAIN_STORAGE_SLOTS = 27;
   public static final int MAIN_STORAGE_ROWS = 3;
   public static final int TOOLS_SLOTS = 9;
   public static final int STORAGE_PANEL_Y = 18;
   private static final int TEXTURE_TOP_BORDER = 17;
   private static final int TEXTURE_ROW_GAP = 4;
   private static final int TEXTURE_BOTTOM_BORDER = 7;
   public static final int STORAGE_PANEL_HEIGHT = 122;
   public static final int EQUIPMENT_GROUP_GAP = 18;
   public static final int EQUIPMENT_SLOT_Y = 35;
   public static final int MAIN_STORAGE_SLOT_Y = 57;
   public static final int TOOLS_SLOT_Y = 115;
   public static final int BUTTON_GAP = 6;
   public static final int BUTTON_HEIGHT = 18;
   public static final int BUTTON_Y = 146;
   public static final int PLAYER_INV_GAP = 6;
   public static final int PLAYER_INV_PANEL_Y = 170;
   public static final int PLAYER_INV_PANEL_HEIGHT = 96;
   public static final int BOTTOM_PADDING = 6;
   public static final int IMAGE_HEIGHT = 272;

   private PlayerCorpseMenuLayout() {
   }
}

package com.playercorpse.client;

import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.playercorpse.PlayerCorpseConfig;
import com.playercorpse.PlayerCorpseNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import org.slf4j.Logger;

@EventBusSubscriber(modid = "playercorpse", value = Dist.CLIENT)
public final class PlayerCorpseHudArrow {
   private static final int SCREEN_Y = 24;
   private static final int TEXT_COLOR = -858993460;
   private static final float ICON_SCALE = 2.5F;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean active = false;
   private static String dimensionId = "";
   private static double targetX;
   private static double targetY;
   private static double targetZ;
   private static boolean loggedFirstRenderTick = false;

   private PlayerCorpseHudArrow() {
   }

   public static void handleCorpseTrackerSync(PlayerCorpseNetworking.CorpseTrackerSyncPayload payload) {
      LOGGER.info(
         "[PlayerCorpse-DEBUG] handleCorpseTrackerSync received: active={}, dimensionId={}, pos=({}, {}, {})",
         new Object[]{payload.active(), payload.dimensionId(), payload.x(), payload.y(), payload.z()}
      );
      active = payload.active();
      dimensionId = payload.dimensionId();
      targetX = payload.x();
      targetY = payload.y();
      targetZ = payload.z();
   }

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      if (!loggedFirstRenderTick) {
         loggedFirstRenderTick = true;
         LOGGER.info("[PlayerCorpse-DEBUG] PlayerCorpseHudArrow.onRenderGui fired for the first time - render event registration confirmed.");
      }

      if (active && (Boolean)PlayerCorpseConfig.HUD_ARROW_ENABLED.get()) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null && player.level() != null && minecraft.screen == null) {
            String currentDimensionId = player.level().dimension().location().toString();
            if (currentDimensionId.equals(dimensionId)) {
               double dx = targetX - player.getX();
               double dz = targetZ - player.getZ();
               double bearingToTarget = Math.toDegrees(Math.atan2(-dx, dz));
               float relativeAngle = (float)Mth.wrapDegrees(bearingToTarget - player.getYRot());
               GuiGraphics guiGraphics = event.getGuiGraphics();
               int centerX = guiGraphics.guiWidth() / 2;
               guiGraphics.pose().pushPose();
               guiGraphics.pose().translate(centerX, 24.0F, 0.0F);
               guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(relativeAngle));
               guiGraphics.pose().scale(2.5F, 2.5F, 1.0F);
               int glyphWidth = minecraft.font.width("^");
               guiGraphics.drawString(minecraft.font, "^", -glyphWidth / 2.0F, -4.0F, -858993460, false);
               guiGraphics.pose().popPose();
            }
         }
      }
   }
}

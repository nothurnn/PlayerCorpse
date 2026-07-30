package com.playercorpse.client;

import com.playercorpse.PlayerCorpseNetworking;
import com.playercorpse.client.renderer.PlayerCorpseRenderer;
import com.playercorpse.client.screen.PlayerCorpseDeathHistoryScreen;
import com.playercorpse.client.screen.PlayerCorpseScreen;
import com.playercorpse.registry.PlayerCorpseEntities;
import com.playercorpse.registry.PlayerCorpseMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@EventBusSubscriber(modid = "playercorpse", bus = Bus.MOD, value = Dist.CLIENT)
public final class PlayerCorpseClientEvents {
   private PlayerCorpseClientEvents() {
   }

   @SubscribeEvent
   public static void onRegisterRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)PlayerCorpseEntities.PLAYER_CORPSE.get(), PlayerCorpseRenderer::new);
   }

   @SubscribeEvent
   public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
      event.register((MenuType)PlayerCorpseMenus.CORPSE_MENU.get(), PlayerCorpseScreen::new);
   }

   public static void handleDeathHistorySync(PlayerCorpseNetworking.DeathHistorySyncPayload payload) {
      Minecraft.getInstance().setScreen(new PlayerCorpseDeathHistoryScreen(payload.entries()));
   }
}

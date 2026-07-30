package com.playercorpse.client;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = "playercorpse", bus = Bus.MOD, value = Dist.CLIENT)
public final class PlayerCorpseKeyBindings {
   public static final KeyMapping OPEN_DEATH_HISTORY = new KeyMapping("key.playercorpse.open_death_history", Type.KEYSYM, 67, "key.categories.playercorpse");

   private PlayerCorpseKeyBindings() {
   }

   @SubscribeEvent
   public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
      event.register(OPEN_DEATH_HISTORY);
   }
}

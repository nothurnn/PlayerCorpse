package com.playercorpse.client;

import com.playercorpse.PlayerCorpseNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "playercorpse", value = Dist.CLIENT)
public final class PlayerCorpseClientTick {
   private PlayerCorpseClientTick() {
   }

   @SubscribeEvent
   public static void onClientTick(Post event) {
      while (PlayerCorpseKeyBindings.OPEN_DEATH_HISTORY.consumeClick()) {
         if (Minecraft.getInstance().player != null) {
            PacketDistributor.sendToServer(new PlayerCorpseNetworking.RequestDeathHistoryPayload(), new CustomPacketPayload[0]);
         }
      }
   }
}

package com.playercorpse.client;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public final class PlayerCorpseSkinResolver {
   private PlayerCorpseSkinResolver() {
   }

   public static PlayerSkin resolveSkin(UUID uuid, String fallbackName) {
      ClientPacketListener connection = Minecraft.getInstance().getConnection();
      if (connection != null) {
         PlayerInfo playerInfo = connection.getPlayerInfo(uuid);
         if (playerInfo != null) {
            return playerInfo.getSkin();
         }
      }

      Optional<GameProfile> fetchedProfile = SkullBlockEntity.fetchGameProfile(uuid).getNow(Optional.empty());
      return fetchedProfile.isPresent()
         ? Minecraft.getInstance().getSkinManager().getInsecureSkin(fetchedProfile.get())
         : DefaultPlayerSkin.get(new GameProfile(uuid, fallbackName));
   }
}

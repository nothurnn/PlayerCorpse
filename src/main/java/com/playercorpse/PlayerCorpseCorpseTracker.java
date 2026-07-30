package com.playercorpse;

import com.mojang.logging.LogUtils;
import com.playercorpse.entity.PlayerCorpseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

@EventBusSubscriber(modid = "playercorpse")
public final class PlayerCorpseCorpseTracker {
   private static final int CHECK_INTERVAL_TICKS = 20;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<UUID, PlayerCorpseCorpseTracker.TrackedCorpse> ACTIVE_CORPSE = new HashMap<>();
   private static boolean loggedFirstTick = false;

   private PlayerCorpseCorpseTracker() {
   }

   public static void onCorpseOpened(ServerLevel level, PlayerCorpseEntity corpse) {
      Optional<UUID> ownerUuid = corpse.getOwnerUuid();
      if (!ownerUuid.isEmpty()) {
         UUID owner = ownerUuid.get();
         PlayerCorpseCorpseTracker.TrackedCorpse tracked = ACTIVE_CORPSE.get(owner);
         if (tracked != null && tracked.entityId() == corpse.getId()) {
            ACTIVE_CORPSE.remove(owner);
            ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);
            if (ownerPlayer != null) {
               sendSync(ownerPlayer, false, tracked.dimension(), 0.0, 0.0, 0.0);
            }
         }
      }
   }

   public static void setActiveCorpse(ServerPlayer player, ServerLevel level, PlayerCorpseEntity corpse) {
      LOGGER.info(
         "[PlayerCorpse-DEBUG] setActiveCorpse called for {}: corpse id={}, dim={}, pos=({}, {}, {})",
         new Object[]{player.getGameProfile().getName(), corpse.getId(), level.dimension().location(), corpse.getX(), corpse.getY(), corpse.getZ()}
      );
      ACTIVE_CORPSE.put(player.getUUID(), new PlayerCorpseCorpseTracker.TrackedCorpse(level.dimension(), corpse.getId()));
      sendSync(player, true, level.dimension(), corpse.getX(), corpse.getY(), corpse.getZ());
   }

   @SubscribeEvent
   public static void onLevelTick(Post event) {
      if (!loggedFirstTick) {
         loggedFirstTick = true;
         LOGGER.info("[PlayerCorpse-DEBUG] PlayerCorpseCorpseTracker.onLevelTick fired for the first time - tick registration confirmed.");
      }

      if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % 20L == 0L) {
         for (ServerPlayer player : level.players()) {
            PlayerCorpseCorpseTracker.TrackedCorpse tracked = ACTIVE_CORPSE.get(player.getUUID());
            if (tracked != null) {
               ServerLevel corpseLevel = level.getServer().getLevel(tracked.dimension());
               if (corpseLevel != null) {
                  Entity entity = corpseLevel.getEntity(tracked.entityId());
                  if (entity != null) {
                     if (entity instanceof PlayerCorpseEntity corpse && !corpse.isDecayed()) {
                        sendSync(player, true, tracked.dimension(), corpse.getX(), corpse.getY(), corpse.getZ());
                     } else {
                        ACTIVE_CORPSE.remove(player.getUUID());
                        sendSync(player, false, tracked.dimension(), 0.0, 0.0, 0.0);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      ACTIVE_CORPSE.remove(event.getEntity().getUUID());
   }

   private static void sendSync(ServerPlayer player, boolean active, ResourceKey<Level> dimension, double x, double y, double z) {
      LOGGER.info(
         "[PlayerCorpse-DEBUG] sendSync to {}: active={}, dim={}, pos=({}, {}, {})",
         new Object[]{player.getGameProfile().getName(), active, dimension.location(), x, y, z}
      );
      PacketDistributor.sendToPlayer(
         player, new PlayerCorpseNetworking.CorpseTrackerSyncPayload(active, dimension.location().toString(), x, y, z), new CustomPacketPayload[0]
      );
   }

   private record TrackedCorpse(ResourceKey<Level> dimension, int entityId) {
   }
}

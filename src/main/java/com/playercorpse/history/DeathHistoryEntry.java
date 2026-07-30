package com.playercorpse.history;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record DeathHistoryEntry(
   long timestampMillis, UUID playerUuid, String playerName, DeathHistoryEntry.Position position, String deathCause, List<ItemStack> items
) {
   public static final StreamCodec<RegistryFriendlyByteBuf, DeathHistoryEntry> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_LONG,
      DeathHistoryEntry::timestampMillis,
      UUIDUtil.STREAM_CODEC,
      DeathHistoryEntry::playerUuid,
      ByteBufCodecs.STRING_UTF8,
      DeathHistoryEntry::playerName,
      DeathHistoryEntry.Position.STREAM_CODEC,
      DeathHistoryEntry::position,
      ByteBufCodecs.STRING_UTF8,
      DeathHistoryEntry::deathCause,
      ItemStack.OPTIONAL_LIST_STREAM_CODEC,
      DeathHistoryEntry::items,
      DeathHistoryEntry::new
   );

   public DeathHistoryEntry {
      items = List.copyOf(items);
   }

   public record Position(String dimensionId, double x, double y, double z) {
      public static final StreamCodec<RegistryFriendlyByteBuf, DeathHistoryEntry.Position> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         DeathHistoryEntry.Position::dimensionId,
         ByteBufCodecs.DOUBLE,
         DeathHistoryEntry.Position::x,
         ByteBufCodecs.DOUBLE,
         DeathHistoryEntry.Position::y,
         ByteBufCodecs.DOUBLE,
         DeathHistoryEntry.Position::z,
         DeathHistoryEntry.Position::new
      );
   }
}

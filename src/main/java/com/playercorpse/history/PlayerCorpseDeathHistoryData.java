package com.playercorpse.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public class PlayerCorpseDeathHistoryData extends SavedData {
   public static final int MAX_ENTRIES_PER_PLAYER = 30;
   private static final String DATA_NAME = "playercorpse_death_history";
   private static final Factory<PlayerCorpseDeathHistoryData> FACTORY = new Factory<>(PlayerCorpseDeathHistoryData::new, PlayerCorpseDeathHistoryData::load);
   private final Map<UUID, List<DeathHistoryEntry>> history = new HashMap<>();

   public static PlayerCorpseDeathHistoryData get(ServerLevel level) {
      return (PlayerCorpseDeathHistoryData)level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "playercorpse_death_history");
   }

   public void addEntry(UUID playerUuid, DeathHistoryEntry entry) {
      List<DeathHistoryEntry> entries = this.history.computeIfAbsent(playerUuid, key -> new ArrayList<>());
      entries.add(0, entry);

      while (entries.size() > 30) {
         entries.remove(entries.size() - 1);
      }

      this.setDirty();
   }

   public List<DeathHistoryEntry> getEntries(UUID playerUuid) {
      return List.copyOf(this.history.getOrDefault(playerUuid, List.of()));
   }

   private static PlayerCorpseDeathHistoryData load(CompoundTag tag, Provider registries) {
      PlayerCorpseDeathHistoryData data = new PlayerCorpseDeathHistoryData();
      ListTag playersTag = tag.getList("Players", 10);

      for (int i = 0; i < playersTag.size(); i++) {
         CompoundTag playerTag = playersTag.getCompound(i);
         UUID playerUuid = playerTag.getUUID("PlayerUuid");
         List<DeathHistoryEntry> entries = new ArrayList<>();
         ListTag entriesTag = playerTag.getList("Entries", 10);

         for (int j = 0; j < entriesTag.size(); j++) {
            entries.add(readEntry(entriesTag.getCompound(j), registries));
         }

         data.history.put(playerUuid, entries);
      }

      return data;
   }

   public CompoundTag save(CompoundTag tag, Provider registries) {
      ListTag playersTag = new ListTag();

      for (Entry<UUID, List<DeathHistoryEntry>> playerEntry : this.history.entrySet()) {
         CompoundTag playerTag = new CompoundTag();
         playerTag.putUUID("PlayerUuid", playerEntry.getKey());
         ListTag entriesTag = new ListTag();

         for (DeathHistoryEntry entry : playerEntry.getValue()) {
            entriesTag.add(writeEntry(entry, registries));
         }

         playerTag.put("Entries", entriesTag);
         playersTag.add(playerTag);
      }

      tag.put("Players", playersTag);
      return tag;
   }

   private static DeathHistoryEntry readEntry(CompoundTag tag, Provider registries) {
      long timestampMillis = tag.getLong("Timestamp");
      UUID playerUuid = tag.getUUID("PlayerUuid");
      String playerName = tag.getString("PlayerName");
      DeathHistoryEntry.Position position = new DeathHistoryEntry.Position(
         tag.getString("Dimension"), tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")
      );
      String deathCause = tag.getString("DeathCause");
      List<ItemStack> items = new ArrayList<>();
      ListTag itemsTag = tag.getList("Items", 10);

      for (int i = 0; i < itemsTag.size(); i++) {
         items.add(ItemStack.parseOptional(registries, itemsTag.getCompound(i)));
      }

      return new DeathHistoryEntry(timestampMillis, playerUuid, playerName, position, deathCause, items);
   }

   private static CompoundTag writeEntry(DeathHistoryEntry entry, Provider registries) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("Timestamp", entry.timestampMillis());
      tag.putUUID("PlayerUuid", entry.playerUuid());
      tag.putString("PlayerName", entry.playerName());
      tag.putString("Dimension", entry.position().dimensionId());
      tag.putDouble("X", entry.position().x());
      tag.putDouble("Y", entry.position().y());
      tag.putDouble("Z", entry.position().z());
      tag.putString("DeathCause", entry.deathCause());
      ListTag itemsTag = new ListTag();

      for (ItemStack stack : entry.items()) {
         if (!stack.isEmpty()) {
            itemsTag.add(stack.saveOptional(registries));
         }
      }

      tag.put("Items", itemsTag);
      return tag;
   }
}

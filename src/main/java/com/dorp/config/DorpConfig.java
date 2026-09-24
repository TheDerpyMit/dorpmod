package com.dorp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import net.neoforged.fml.loading.FMLPaths;

public class DorpConfig {
   public static final DorpConfig.DoubleValue PROXIMITY_RANGE_NORMAL = new DorpConfig.DoubleValue(75.0);
   public static final DorpConfig.DoubleValue PROXIMITY_RANGE_WHISPER = new DorpConfig.DoubleValue(3.0);
   public static final DorpConfig.DoubleValue RADIO_BLOCK_TRANSMISSION_RANGE = new DorpConfig.DoubleValue(7.0);
   public static final DorpConfig.DoubleValue RADIO_BLOCK_RECEPTION_RANGE = new DorpConfig.DoubleValue(40.0);
   public static final DorpConfig.DoubleValue WALKIE_TALKIE_RANGE = new DorpConfig.DoubleValue(930.0);
   public static final DorpConfig.DoubleValue HEADSET_RANGE = new DorpConfig.DoubleValue(630.0);
   public static final DorpConfig.DoubleValue INTERCEPTOR_RANGE = new DorpConfig.DoubleValue(1200.0);
   public static final DorpConfig.DoubleValue INTERCEPTOR_RECEIVE_RANGE = new DorpConfig.DoubleValue(30.0);
   public static final DorpConfig.DoubleValue SHOUT_RANGE = new DorpConfig.DoubleValue(160.0);
   public static final DorpConfig.LongValue SHOUT_COOLDOWN_MS = new DorpConfig.LongValue(300000L);
   public static final DorpConfig.IntValue WALKIE_TALKIE_MAX_ENERGY = new DorpConfig.IntValue(50000);
   public static final DorpConfig.IntValue WALKIE_TALKIE_ENERGY_PER_TICK = new DorpConfig.IntValue(1);
   public static final DorpConfig.IntValue WALKIE_TALKIE_ENERGY_PER_TX = new DorpConfig.IntValue(100);
   public static final DorpConfig.IntValue HEADSET_MAX_ENERGY = new DorpConfig.IntValue(50000);
   public static final DorpConfig.IntValue HEADSET_ENERGY_PER_TICK = new DorpConfig.IntValue(1);
   public static final DorpConfig.IntValue HEADSET_ENERGY_PER_TX = new DorpConfig.IntValue(100);
   public static final DorpConfig.IntValue INTERCEPTOR_MAX_ENERGY = new DorpConfig.IntValue(5000);
   public static final DorpConfig.IntValue INTERCEPTOR_ENERGY_PER_MESSAGE = new DorpConfig.IntValue(100);
   public static final DorpConfig.IntValue SOS_MAX_USES = new DorpConfig.IntValue(3);
   public static final DorpConfig.BooleanValue ENABLE_CHAT_DISTORTION = new DorpConfig.BooleanValue(true);
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   private static File getConfigFile() {
      return FMLPaths.CONFIGDIR.get().resolve("dorps.json").toFile();
   }

   public static void load() {
      File file = getConfigFile();
      if (!file.exists()) {
         save();
      } else {
         try (FileReader reader = new FileReader(file)) {
            DorpConfig.ConfigData data = (DorpConfig.ConfigData)GSON.fromJson(reader, DorpConfig.ConfigData.class);
            if (data != null) {
               if (data.proximity_range_normal != null) {
                  PROXIMITY_RANGE_NORMAL.set(data.proximity_range_normal);
               }

               if (data.proximity_range_whisper != null) {
                  PROXIMITY_RANGE_WHISPER.set(data.proximity_range_whisper);
               }

               if (data.radio_block_transmission_range != null) {
                  RADIO_BLOCK_TRANSMISSION_RANGE.set(data.radio_block_transmission_range);
               }

               if (data.radio_block_reception_range != null) {
                  RADIO_BLOCK_RECEPTION_RANGE.set(data.radio_block_reception_range);
               }

               if (data.walkie_talkie_range != null) {
                  WALKIE_TALKIE_RANGE.set(data.walkie_talkie_range);
               }

               if (data.headset_range != null) {
                  HEADSET_RANGE.set(data.headset_range);
               }

               if (data.interceptor_range != null) {
                  INTERCEPTOR_RANGE.set(data.interceptor_range);
               }

               if (data.interceptor_receive_range != null) {
                  INTERCEPTOR_RECEIVE_RANGE.set(data.interceptor_receive_range);
               }

               if (data.shout_range != null) {
                  SHOUT_RANGE.set(data.shout_range);
               }

               if (data.shout_cooldown_ms != null) {
                  SHOUT_COOLDOWN_MS.set(data.shout_cooldown_ms);
               }

               if (data.walkie_talkie_max_energy != null) {
                  WALKIE_TALKIE_MAX_ENERGY.set(data.walkie_talkie_max_energy);
               }

               if (data.walkie_talkie_energy_per_tick != null) {
                  WALKIE_TALKIE_ENERGY_PER_TICK.set(data.walkie_talkie_energy_per_tick);
               }

               if (data.walkie_talkie_energy_per_tx != null) {
                  WALKIE_TALKIE_ENERGY_PER_TX.set(data.walkie_talkie_energy_per_tx);
               }

               if (data.headset_max_energy != null) {
                  HEADSET_MAX_ENERGY.set(data.headset_max_energy);
               }

               if (data.headset_energy_per_tick != null) {
                  HEADSET_ENERGY_PER_TICK.set(data.headset_energy_per_tick);
               }

               if (data.headset_energy_per_tx != null) {
                  HEADSET_ENERGY_PER_TX.set(data.headset_energy_per_tx);
               }

               if (data.interceptor_max_energy != null) {
                  INTERCEPTOR_MAX_ENERGY.set(data.interceptor_max_energy);
               }

               if (data.interceptor_energy_per_message != null) {
                  INTERCEPTOR_ENERGY_PER_MESSAGE.set(data.interceptor_energy_per_message);
               }

               if (data.sos_max_uses != null) {
                  SOS_MAX_USES.set(data.sos_max_uses);
               }

               if (data.enable_chat_distortion != null) {
                  ENABLE_CHAT_DISTORTION.set(data.enable_chat_distortion);
               }
            }
         } catch (Exception var6) {
            System.err.println("Failed to load Dorp mod config: " + var6.getMessage());
         }
      }
   }

   public static void save() {
      File file = getConfigFile();
      File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
         parent.mkdirs();
      }

      DorpConfig.ConfigData data = new DorpConfig.ConfigData();
      data.proximity_range_normal = PROXIMITY_RANGE_NORMAL.get();
      data.proximity_range_whisper = PROXIMITY_RANGE_WHISPER.get();
      data.radio_block_transmission_range = RADIO_BLOCK_TRANSMISSION_RANGE.get();
      data.radio_block_reception_range = RADIO_BLOCK_RECEPTION_RANGE.get();
      data.walkie_talkie_range = WALKIE_TALKIE_RANGE.get();
      data.headset_range = HEADSET_RANGE.get();
      data.interceptor_range = INTERCEPTOR_RANGE.get();
      data.interceptor_receive_range = INTERCEPTOR_RECEIVE_RANGE.get();
      data.shout_range = SHOUT_RANGE.get();
      data.shout_cooldown_ms = SHOUT_COOLDOWN_MS.get();
      data.walkie_talkie_max_energy = WALKIE_TALKIE_MAX_ENERGY.get();
      data.walkie_talkie_energy_per_tick = WALKIE_TALKIE_ENERGY_PER_TICK.get();
      data.walkie_talkie_energy_per_tx = WALKIE_TALKIE_ENERGY_PER_TX.get();
      data.headset_max_energy = HEADSET_MAX_ENERGY.get();
      data.headset_energy_per_tick = HEADSET_ENERGY_PER_TICK.get();
      data.headset_energy_per_tx = HEADSET_ENERGY_PER_TX.get();
      data.interceptor_max_energy = INTERCEPTOR_MAX_ENERGY.get();
      data.interceptor_energy_per_message = INTERCEPTOR_ENERGY_PER_MESSAGE.get();
      data.sos_max_uses = SOS_MAX_USES.get();
      data.enable_chat_distortion = ENABLE_CHAT_DISTORTION.get();

      try (FileWriter writer = new FileWriter(file)) {
         GSON.toJson(data, writer);
      } catch (Exception var8) {
         System.err.println("Failed to save Dorp mod config: " + var8.getMessage());
      }
   }

   public static class BooleanValue {
      private final boolean defaultValue;
      private boolean value;

      public BooleanValue(boolean defaultValue) {
         this.defaultValue = defaultValue;
         this.value = defaultValue;
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean val) {
         this.value = val;
      }
   }

   private static class ConfigData {
      Double proximity_range_normal;
      Double proximity_range_whisper;
      Double radio_block_transmission_range;
      Double radio_block_reception_range;
      Double walkie_talkie_range;
      Double headset_range;
      Double interceptor_range;
      Double interceptor_receive_range;
      Double shout_range;
      Long shout_cooldown_ms;
      Integer walkie_talkie_max_energy;
      Integer walkie_talkie_energy_per_tick;
      Integer walkie_talkie_energy_per_tx;
      Integer headset_max_energy;
      Integer headset_energy_per_tick;
      Integer headset_energy_per_tx;
      Integer interceptor_max_energy;
      Integer interceptor_energy_per_message;
      Integer sos_max_uses;
      Boolean enable_chat_distortion;
   }

   public static class DoubleValue {
      private final double defaultValue;
      private double value;

      public DoubleValue(double defaultValue) {
         this.defaultValue = defaultValue;
         this.value = defaultValue;
      }

      public double get() {
         return this.value;
      }

      public void set(double val) {
         this.value = val;
      }
   }

   public static class IntValue {
      private final int defaultValue;
      private int value;

      public IntValue(int defaultValue) {
         this.defaultValue = defaultValue;
         this.value = defaultValue;
      }

      public int get() {
         return this.value;
      }

      public void set(int val) {
         this.value = val;
      }
   }

   public static class LongValue {
      private final long defaultValue;
      private long value;

      public LongValue(long defaultValue) {
         this.defaultValue = defaultValue;
         this.value = defaultValue;
      }

      public long get() {
         return this.value;
      }

      public void set(long val) {
         this.value = val;
      }
   }
}

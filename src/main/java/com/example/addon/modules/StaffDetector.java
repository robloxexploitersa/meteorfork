
package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.world.GameMode;

public class StaffDetector extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

   public final Setting<Boolean> detectCreative = sgGeneral.add(new BoolSetting.Builder()
      .name("detect-creative")
      .description("Flags players who are in creative mode as potential staff.")
      .defaultValue(true)
      .build()
   );

   private static final List<String> onlineStaff = new ArrayList<>();
   
   // Added "sr admin" to the target detection list
   private static final String[] STAFF_KEYWORDS = {
      "sr admin", "sradmin", "admin", "mod", "owner", "helper", "staff", "dev"
   };

   public StaffDetector() {
      super(AddonTemplate.CATEGORY, "staff-detector", "Scans the server tab list natively to detect active staff members.");
   }

   @Override
   public void onActivate() {
      onlineStaff.clear();
   }

   @Override
   public void onDeactivate() {
      onlineStaff.clear();
   }

   @EventHandler
   private void onTick(TickEvent.Post event) {
      if (mc.player == null || mc.getNetworkHandler() == null) return;

      List<String> currentStaff = new ArrayList<>();

      for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
         String name = entry.getProfile().getName();
         
         if (detectCreative.get() && entry.getGameMode() == GameMode.CREATIVE) {
            if (!name.equals(mc.player.getEntityName()) && !currentStaff.contains(name)) {
               currentStaff.add(name + " §7[Creative]");
               continue;
            }
         }

         // Standardize input string checking to safely catch multi-word strings like "sr admin"
         String displayName = entry.getDisplayName() != null ? entry.getDisplayName().getString().toLowerCase() : "";
         String lowerName = name.toLowerCase();

         for (String keyword : STAFF_KEYWORDS) {
            if (lowerName.contains(keyword) || displayName.contains(keyword)) {
               if (!currentStaff.contains(name)) {
                  currentStaff.add(name);
                  break;
               }
            }
         }
      }

      onlineStaff.clear();
      onlineStaff.addAll(currentStaff);
   }

   public static List<String> getOnlineStaff() {
      return onlineStaff;
   }
}

package org.stepan1411.pvp_bot.utils;

import net.minecraft.entity.player.PlayerInventory;

import java.lang.reflect.Field;

public class InventoryHelper {
    private static Field selectedSlotField;
    
    static {
        try {
            selectedSlotField = PlayerInventory.class.getDeclaredField("selectedSlot");
            selectedSlotField.setAccessible(true);
        } catch (NoSuchFieldException e) {

            try {
                selectedSlotField = PlayerInventory.class.getDeclaredField("field_7545");
                selectedSlotField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException("Failed to find selectedSlot field", ex);
            }
        }
    }
    
    public static void setSelectedSlot(PlayerInventory inventory, int slot) {
        try {
            selectedSlotField.setInt(inventory, slot);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set selectedSlot", e);
        }
    }
    
    public static int getSelectedSlot(PlayerInventory inventory) {
        try {
            return selectedSlotField.getInt(inventory);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get selectedSlot", e);
        }
    }
}

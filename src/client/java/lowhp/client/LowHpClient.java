package lowhp.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class LowHpClient implements ClientModInitializer {

    private static final float WARN_HP = 6.0f;
    // 20 ticks = 1 second between sounds
    private static final int REPEAT_TICKS = 20;

    private int cooldown = 0;
    private boolean enabled = true;

    private KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("lowhp", "main"));

        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.lowhp.toggle", InputConstants.KEY_GRAVE, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;

            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                cooldown = 0;
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("LowHP alert: " + (enabled ? "ON" : "OFF"))
                                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED),
                            true);
                }
            }

            if (player == null || !enabled) {
                cooldown = 0;
                return;
            }

            float hp = player.getHealth();
            boolean totemInOffhand = player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
            boolean totemInInventory = hasTotemInInventory(player);

            boolean shouldAlert = hp > 0 && hp <= WARN_HP && totemInInventory && !totemInOffhand;

            if (shouldAlert) {
                if (cooldown <= 0) {
                    client.getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f));
                    cooldown = REPEAT_TICKS;
                }
                cooldown--;
            } else {
                cooldown = 0;
            }
        });
    }

    private boolean hasTotemInInventory(LocalPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                return true;
            }
        }
        return false;
    }
}

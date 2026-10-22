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
    private static final float RESET_HP = 8.0f;

    private boolean warned = false;
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
                warned = false;
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("LowHP alert: " + (enabled ? "ON" : "OFF"))
                                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED),
                            true);
                }
            }

            if (player == null || !enabled) {
                warned = false;
                return;
            }

            float hp = player.getHealth();
            boolean totemInOffhand = player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
            boolean totemInInventory = hasTotemInInventory(player);

            if (hp > 0 && hp <= WARN_HP && totemInInventory && !totemInOffhand) {
                if (!warned) {
                    warned = true;
                    client.getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f));
                }
            } else if (hp > RESET_HP || totemInOffhand || !totemInInventory) {
                warned = false;
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

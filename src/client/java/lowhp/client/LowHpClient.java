package lowhp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class LowHpClient implements ClientModInitializer {

    // 3 hearts = 6 HP
    private static final float WARN_HP = 6.0f;
    // is se upar health jaaye to warning reset hogi
    private static final float RESET_HP = 8.0f;

    private boolean warned = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null) {
                warned = false;
                return;
            }

            float hp = player.getHealth();
            boolean totemInOffhand = player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
            boolean totemInInventory = hasTotemInInventory(player);

            // warning sirf tab jab totem hai par offhand me nahi
            if (hp > 0 && hp <= WARN_HP && totemInInventory && !totemInOffhand) {
                if (!warned) {
                    warned = true;
                    client.gui.setTimes(2, 25, 8);
                    client.gui.setSubtitle(Component.literal("Switch Your Totem To OffHand!").withStyle(ChatFormatting.YELLOW));
                    client.gui.setTitle(Component.literal("TOTEM!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
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

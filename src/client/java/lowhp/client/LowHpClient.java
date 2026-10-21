package lowhp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LowHpClient implements ClientModInitializer {

    // 1.5 hearts = 3 HP
    private static final float WARN_HP = 3.0f;
    // is se upar health jaaye to warning reset hogi
    private static final float RESET_HP = 6.0f;

    private boolean warned = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) {
                warned = false;
                return;
            }

            float hp = player.getHealth();
            boolean totemInOffhand = player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);

            if (hp > 0 && hp <= WARN_HP && !totemInOffhand) {
                if (!warned) {
                    warned = true;
                    client.inGameHud.setTitleTicks(2, 25, 8);
                    client.inGameHud.setSubtitle(Text.literal("Offhand me switch kar!").formatted(Formatting.YELLOW));
                    client.inGameHud.setTitle(Text.literal("TOTEM!").formatted(Formatting.RED, Formatting.BOLD));
                }
            } else if (hp > RESET_HP || totemInOffhand) {
                warned = false;
            }
        });
    }
}

package com.example.headflip.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class TemplateModClient implements ClientModInitializer {
    private static KeyMapping flipKey;

    // -1 = hamesha LEFT, +1 = hamesha RIGHT
    private static final float DIRECTION = -1.0f;
    // ek turn kitne degree ka hai
    private static final float DEGREES = 180.0f;
    // ek click mein kitne turns (2 = 180 phir 180)
    private static final int TURNS_PER_CLICK = 2;
    // ek 180 kitne ticks mein ho (1 tick = 0.05 sec). 2 = tez, 3 = thoda smooth
    private static final int TICKS_PER_TURN = 2;

    private static float remaining = 0.0f;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("headflip", "main")
        );

        flipKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.headflip.flip",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            category
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            while (flipKey.consumeClick()) {
                remaining += DEGREES * TURNS_PER_CLICK;
            }

            if (client.player == null) {
                remaining = 0.0f;
                return;
            }

            if (remaining > 0.001f) {
                float step = Math.min(DEGREES / TICKS_PER_TURN, remaining);
                client.player.setYRot(client.player.getYRot() + DIRECTION * step);
                remaining -= step;
            }
        });
    }
}

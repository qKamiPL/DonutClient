
package pl.donutclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DonutClient implements ClientModInitializer {
    private KeyBinding openMenu;

    @Override
    public void onInitializeClient() {
        openMenu = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.donutclient.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_CONTROL,
                KeyBinding.Category.MISC
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen instanceof DonutScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new DonutScreen());
                }
            }
        });
    }

    private static class DonutScreen extends Screen {
        protected DonutScreen() {
            super(Text.literal("DonutClient"));
        }

        @Override
        protected void init() {
            int x = (width - 360) / 2;
            int y = (height - 220) / 2;

            addDrawableChild(ButtonWidget.builder(
                Text.literal("CLOSE MENU"),
                button -> close()
            ).dimensions(x + 100, y + 165, 160, 24).build());
        }

        @Override
        public void render(
            DrawContext context, int mouseX, int mouseY, float delta
        ) {
            renderBackground(context, mouseX, mouseY, delta);

            int x = (width - 360) / 2;
            int y = (height - 220) / 2;

            context.fill(x, y, x + 360, y + 220, 0xF0121725);
            context.fill(x, y, x + 360, y + 3, 0xFF438AFF);

            context.drawTextWithShadow(
                textRenderer, Text.literal("DONUTCLIENT"),
                x + 20, y + 18, 0xFFFFFFFF
            );

            context.drawTextWithShadow(
                textRenderer, Text.literal("Client Dashboard"),
                x + 20, y + 42, 0xFF9AAAC4
            );

            context.fill(
                x + 20, y + 65, x + 340, y + 66, 0xFF30394B
            );

            context.drawTextWithShadow(
                textRenderer, Text.literal("Welcome to DonutClient"),
                x + 20, y + 85, 0xFFFFFFFF
            );

            context.drawTextWithShadow(
                textRenderer, Text.literal("Right Ctrl - Toggle menu"),
                x + 20, y + 110, 0xFF9AAAC4
            );

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}

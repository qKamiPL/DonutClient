
package pl.donutclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DonutClient implements ClientModInitializer {
    private KeyBinding openMenu;

   
package pl.donutclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DonutClient implements ClientModInitializer {
    private KeyBinding openMenu;

    private static final KeyBinding.Category DONUT_CATEGORY =
        KeyBinding.Category.create(Identifier.of("donutclient", "main"));

    @Override
    public void onInitializeClient() {
        openMenu = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.donutclient.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_CONTROL,
                DONUT_CATEGORY
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
public void onInitializeClient() {
    openMenu = KeyBindingHelper.registerKeyBinding(
        KeyBindingHelper.createKeyBinding(
            "key.donutclient.open",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_CONTROL,
            "category.donutclient"
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

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}

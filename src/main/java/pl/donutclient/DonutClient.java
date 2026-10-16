
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
                KeyBinding.MISC_CATEGORY
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
        private String activeTab = "HOME";
        private float animation = 0.0f;

        private static final int PANEL_WIDTH = 520;
        private static final int PANEL_HEIGHT = 310;
        private static final int SIDEBAR_WIDTH = 145;

        protected DonutScreen() {
            super(Text.literal("DonutClient"));
        }

        private void switchTab(String tab) {
            if (!activeTab.equals(tab)) {
                activeTab = tab;
                animation = 0.0f;
            }
        }

        @Override
        protected void init() {
            clearChildren();

            int x = (width - PANEL_WIDTH) / 2;
            int y = (height - PANEL_HEIGHT) / 2;

            addDrawableChild(ButtonWidget.builder(
                Text.literal("HOME"),
                button -> switchTab("HOME")
            ).dimensions(x + 12, y + 60, 121, 25).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("VISUALS"),
                button -> switchTab("VISUALS")
            ).dimensions(x + 12, y + 92, 121, 25).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("SETTINGS"),
                button -> switchTab("SETTINGS")
            ).dimensions(x + 12, y + 124, 121, 25).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("ABOUT"),
                button -> switchTab("ABOUT")
            ).dimensions(x + 12, y + 156, 121, 25).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("CLOSE MENU"),
                button -> close()
            ).dimensions(x + PANEL_WIDTH - 130, y + PANEL_HEIGHT - 39, 115, 25).build());
        }

        @Override
        public void render(
            DrawContext context, int mouseX, int mouseY, float delta
        ) {
            context.fill(0, 0, this.width, this.height, 0x90000000);

            animation = Math.min(1.0f, animation + 0.09f);

            int x = (width - PANEL_WIDTH) / 2;
            int y = (height - PANEL_HEIGHT) / 2;

            int contentOffset = (int) ((1.0f - animation) * 18);
            int contentX = x + SIDEBAR_WIDTH + 20 + contentOffset;
            int contentY = y + 65;

            // Main panel and retro accents
            context.fill(
                x - 2, y - 2,
                x + PANEL_WIDTH + 2, y + PANEL_HEIGHT + 2,
                0xFF080B12
            );

            context.fill(
                x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT,
                0xF0121725
            );

            context.fill(
                x, y, x + PANEL_WIDTH, y + 3,
                0xFF438AFF
            );

            // Sidebar
            context.fill(
                x, y + 3,
                x + SIDEBAR_WIDTH, y + PANEL_HEIGHT,
                0xFF0C111D
            );

            context.fill(
                x + SIDEBAR_WIDTH, y + 3,
                x + SIDEBAR_WIDTH + 1, y + PANEL_HEIGHT,
                0xFF30394B
            );

            // Active tab indicator
            int activeIndex = switch (activeTab) {
                case "VISUALS" -> 1;
                case "SETTINGS" -> 2;
                case "ABOUT" -> 3;
                default -> 0;
            };

            int activeY = y + 60 + activeIndex * 32;

            context.fill(
                x + 7, activeY,
                x + 137, activeY + 25,
                0xFF202F49
            );

            context.fill(
                x + 7, activeY,
                x + 10, activeY + 25,
                0xFF438AFF
            );

            // Header
            context.drawTextWithShadow(
                textRenderer,
                Text.literal("DONUTCLIENT"),
                x + 15, y + 18,
                0xFFFFFFFF
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("RETRO EDITION  /  1.0"),
                x + 15, y + 35,
                0xFF7186A8
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("CLIENT MENU"),
                x + SIDEBAR_WIDTH + 20,
                y + 20,
                0xFF438AFF
            );

            context.fill(
                x + SIDEBAR_WIDTH + 20, y + 42,
                x + PANEL_WIDTH - 15, y + 43,
                0xFF30394B
            );

            // Tab content
            switch (activeTab) {
                case "VISUALS" -> {
                    context.drawTextWithShadow(
                        textRenderer, Text.literal("VISUALS"),
                        contentX, contentY, 0xFFFFFFFF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Visual options"),
                        contentX, contentY + 28, 0xFF9AAAC4
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("This page is ready for"),
                        contentX, contentY + 55, 0xFFD5DEEF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("visual and accessibility settings."),
                        contentX, contentY + 69, 0xFFD5DEEF
                    );
                }

                case "SETTINGS" -> {
                    context.drawTextWithShadow(
                        textRenderer, Text.literal("SETTINGS"),
                        contentX, contentY, 0xFFFFFFFF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Interface settings"),
                        contentX, contentY + 28, 0xFF9AAAC4
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Menu key: RIGHT CTRL"),
                        contentX, contentY + 55, 0xFFD5DEEF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Design: Retro / Blue"),
                        contentX, contentY + 75, 0xFFD5DEEF
                    );
                }

                case "ABOUT" -> {
                    context.drawTextWithShadow(
                        textRenderer, Text.literal("ABOUT DONUTCLIENT"),
                        contentX, contentY, 0xFFFFFFFF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Version 1.0.0"),
                        contentX, contentY + 30, 0xFFD5DEEF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Fabric  /  Minecraft 1.21.8"),
                        contentX, contentY + 50, 0xFF9AAAC4
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Made for your own Minecraft setup."),
                        contentX, contentY + 80, 0xFF9AAAC4
                    );
                }

                default -> {
                    context.drawTextWithShadow(
                        textRenderer, Text.literal("WELCOME BACK"),
                        contentX, contentY, 0xFFFFFFFF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Your Minecraft client dashboard."),
                        contentX, contentY + 30, 0xFFD5DEEF
                    );

                    context.fill(
                        contentX, contentY + 60,
                        contentX + 290, contentY + 105,
                        0xFF192438
                    );

                    context.fill(
                        contentX, contentY + 60,
                        contentX + 3, contentY + 105,
                        0xFF438AFF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("DONUTCLIENT"),
                        contentX + 14, contentY + 72,
                        0xFFFFFFFF
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Retro interface loaded"),
                        contentX + 14, contentY + 87,
                        0xFF9AAAC4
                    );

                    context.drawTextWithShadow(
                        textRenderer, Text.literal("Select a page from the sidebar."),
                        contentX, contentY + 125,
                        0xFF9AAAC4
                    );
                }
            }

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("DONUTCLIENT  /  READY"),
                x + 15, y + PANEL_HEIGHT - 22,
                0xFF7186A8
            );

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}

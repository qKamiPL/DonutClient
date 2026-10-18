
package pl.donutclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class DonutClient implements ClientModInitializer {
    private KeyBinding openMenu;

    private static final Map<String, Boolean> enabled = new LinkedHashMap<>();

    private static double oldGamma = 0.5;
    private static boolean gammaSaved = false;

    private static final String[][] MODULES = {
        {"HUD", "FPS Counter", "CPS Counter", "Coordinates", "Clock"},
        {"Fullbright", "Zoom", "Keystrokes", "Armor HUD", "Crosshair"},
        {"Freecam", "Base Finder", "Spawner Finder", "Storage Finder", "Player ESP", "X-Ray"},
        {"Animations", "Particles", "Clean Screen", "Low Fire", "Halo Cyan", "Particle Aura", "Colored Chest ESP"},
        {"Dark Theme", "Smooth Animations", "Show Descriptions"}
    };

    public static boolean isModuleEnabled(String name) {
        return enabled.getOrDefault(name, false);
    }

    private static void setEnabled(String name, boolean value) {
        enabled.put(name, value);
    }

    @Override
    public void onInitializeClient() {
        for (String[] group : MODULES) {
            for (String name : group) {
                enabled.put(name, false);
            }
        }

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

            updateFullbright(client);
            Freecam.update(client, isModuleEnabled("Freecam"));
        });

        DonutVisuals.register();

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.options == null) {
                return;
            }

            int y = 8;

            if (isModuleEnabled("HUD")) {
                context.drawTextWithShadow(
                    client.textRenderer, Text.literal("DONUTCLIENT"),
                    8, y, 0xFF20CFFF
                );
                y += 12;
            }

            if (isModuleEnabled("FPS Counter")) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("FPS: " + client.getCurrentFps()),
                    8, y, 0xFFFFFFFF
                );
                y += 12;
            }

            if (isModuleEnabled("Coordinates")) {
                var p = client.player.getBlockPos();
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("XYZ: " + p.getX() + " " + p.getY() + " " + p.getZ()),
                    8, y, 0xFFFFFFFF
                );
                y += 12;
            }

            if (isModuleEnabled("Clock")) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal(java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                    )),
                    8, y, 0xFF20CFFF
                );
            }
        });
    }

    private static void updateFullbright(MinecraftClient client) {
        if (client.options == null) {
            return;
        }

        if (isModuleEnabled("Fullbright")) {
            if (!gammaSaved) {
                oldGamma = client.options.getGamma().getValue();
                gammaSaved = true;
            }
            client.options.getGamma().setValue(1.0);
        } else if (gammaSaved) {
            client.options.getGamma().setValue(oldGamma);
            gammaSaved = false;
        }
    }

    private static class DonutScreen extends Screen {
        private static final int BLUE = 0xFF20CFFF;
        private static final int PANEL = 0xE90A1425;
        private static final int BORDER = 0xFF17466B;
        private static final int MUTED = 0xFF91B4D8;

        private int scroll = 0;

        protected DonutScreen() {
            super(Text.literal("DonutClient"));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Celowo bez blur: unikamy konfliktu "Can only blur once per frame".
            context.fill(0, 0, width, height, 0x65000000);

            int margin = 12;
            int gap = 7;
            int colWidth = (width - margin * 2 - gap * 4) / 5;
            int startY = margin + 43;

            context.drawTextWithShadow(
                textRenderer, Text.literal("DONUT CLIENT"),
                margin + 2, margin + 3, BLUE
            );

            context.drawTextWithShadow(
                textRenderer, Text.literal("VISUAL CLIENT / 1.0.1"),
                margin + 3, margin + 19, MUTED
            );

            String[] categories = {"COMBAT", "MISC", "DONUT", "RENDER", "CLIENT"};

            for (int c = 0; c < MODULES.length; c++) {
                int x = margin + c * (colWidth + gap);
                int panelHeight = Math.max(40, Math.min(
                    34 + MODULES[c].length * 31,
                    height - startY - 10
                ));

                context.fill(x - 1, startY - 1, x + colWidth + 1, startY + panelHeight + 1, 0xFF030914);
                context.fill(x, startY, x + colWidth, startY + panelHeight, PANEL);
                context.fill(x, startY, x + colWidth, startY + 2, BORDER);

                context.drawTextWithShadow(
                    textRenderer, Text.literal(categories[c]),
                    x + 9, startY + 11, BLUE
                );

                context.fill(x + 7, startY + 29, x + colWidth - 7, startY + 30, BORDER);

                int rowY = startY + 36 - scroll;

                for (String module : MODULES[c]) {
                    if (rowY + 27 >= startY + 32 && rowY <= startY + panelHeight) {
                        boolean active = isModuleEnabled(module);
                        boolean supported = isSupported(module);

                        context.fill(
                            x + 6, rowY, x + colWidth - 6, rowY + 27,
                            active ? 0xFF103A59 : 0xCC0A192B
                        );
                        context.fill(
                            x + 6, rowY, x + 7, rowY + 27,
                            active ? BLUE : BORDER
                        );

                        String label = module;
                        int maxWidth = Math.max(10, colWidth - 44);

                        while (!label.isEmpty() && textRenderer.getWidth(label) > maxWidth) {
                            label = label.substring(0, label.length() - 1);
                        }
                        if (!label.equals(module) && label.length() > 2) {
                            label = label.substring(0, label.length() - 2) + "..";
                        }

                        context.drawTextWithShadow(
                            textRenderer, Text.literal(label),
                            x + 12, rowY + 9, active ? BLUE : 0xFFD8E8FA
                        );

                        int sx = x + colWidth - 34;
                        int sy = rowY + 7;
                        context.fill(
                            sx, sy, sx + 23, sy + 13,
                            active ? 0xFF00AEEF : 0xFF30435B
                        );

                        int knobX = active ? sx + 13 : sx + 2;
                        context.fill(knobX, sy + 2, knobX + 9, sy + 11, 0xFFFFFFFF);

                        if (!supported) {
                            context.fill(
                                x + colWidth - 9, rowY,
                                x + colWidth - 6, rowY + 27, 0xFF24374A
                            );
                        }
                    }
                    rowY += 31;
                }
            }

            context.drawTextWithShadow(
                textRenderer, Text.literal("RIGHT CTRL - CLOSE"),
                margin + 2, height - 13, MUTED
            );

            // Bez super.render(): nie wywołujemy dodatkowego renderowania ekranu.
        }

        private boolean isSupported(String module) {
            return switch (module) {
                case "HUD", "FPS Counter", "CPS Counter", "Coordinates",
                     "Clock", "Fullbright", "Freecam", "Halo Cyan",
                     "Particle Aura", "Colored Chest ESP" -> true;
                default -> false;
            };
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int margin = 12;
            int gap = 7;
            int colWidth = (width - margin * 2 - gap * 4) / 5;
            int startY = margin + 43;

            for (int c = 0; c < MODULES.length; c++) {
                int x = margin + c * (colWidth + gap);
                int panelHeight = Math.max(40, Math.min(
                    34 + MODULES[c].length * 31,
                    height - startY - 10
                ));

                if (mouseX < x || mouseX > x + colWidth
                    || mouseY < startY + 32 || mouseY > startY + panelHeight) {
                    continue;
                }

                int row = (int) ((mouseY - (startY + 36) + scroll) / 31);
                if (row < 0 || row >= MODULES[c].length) {
                    return true;
                }

                String module = MODULES[c][row];
                if (isSupported(module)) {
                    setEnabled(module, !isModuleEnabled(module));
                }
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(
            double mouseX, double mouseY,
            double horizontalAmount, double verticalAmount
        ) {
            scroll = Math.max(0, Math.min(150, scroll - (int) (verticalAmount * 25)));
            return true;
        }
    }
}

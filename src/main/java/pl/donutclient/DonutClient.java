
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
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DonutClient implements ClientModInitializer {

    private KeyBinding openMenu;

    private static final Map<String, Boolean> enabled =
        new LinkedHashMap<>();

    private static final double DEFAULT_GAMMA = 0.5;
    private static double oldGamma = DEFAULT_GAMMA;
    private static boolean gammaSaved = false;

    private static final String[][] MODULES = {
        {
            "HUD", "FPS Counter", "CPS Counter",
            "Coordinates", "Clock"
        },
        {
            "Fullbright", "Zoom", "Keystrokes",
            "Armor HUD", "Crosshair"
        },
        {
            "Freecam", "Base Finder", "Spawner Finder",
            "Storage Finder", "Player ESP", "X-Ray"
        },
        {
            "Animations", "Particles", "Clean Screen",
            "Low Fire"
        },
        {
            "Dark Theme", "Smooth Animations",
            "Show Descriptions"
        }
    };

    private static final String[] CATEGORIES = {
        "COMBAT", "MISC", "DONUT", "RENDER", "CLIENT"
    };

    public static boolean isEnabled(String name) {
        return enabled.getOrDefault(name, false);
    }

    private static void setEnabled(String name, boolean value) {
        enabled.put(name, value);
    }

    @Override
    public void onInitializeClient() {
        for (String[] group : MODULES) {
            for (String module : group) {
                enabled.put(module, false);
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

        FinderEsp.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen instanceof DonutScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new DonutScreen());
                }
            }

            updateFullbright(client);
            Freecam.update(client, isEnabled("Freecam"));

            FinderModules.update(
                client,
                isEnabled("Storage Finder"),
                isEnabled("Spawner Finder"),
                isEnabled("Base Finder")
            );

            FinderEsp.setEnabled(
                isEnabled("Storage Finder"),
                isEnabled("Spawner Finder")
            );
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.options == null) {
                return;
            }

            int y = 8;

            if (isEnabled("HUD")) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("DONUTCLIENT"),
                    8, y, 0xFF36B9FF
                );
                y += 12;
            }

            if (isEnabled("FPS Counter")) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("FPS: " + client.getCurrentFps()),
                    8, y, 0xFFFFFFFF
                );
                y += 12;
            }

            if (isEnabled("Coordinates")) {
                var pos = client.player.getBlockPos();

                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal(
                        "XYZ: " + pos.getX() + " "
                            + pos.getY() + " " + pos.getZ()
                    ),
                    8, y, 0xFFFFFFFF
                );
                y += 12;
            }

            if (isEnabled("Clock")) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal(
                        LocalTime.now().format(
                            DateTimeFormatter.ofPattern("HH:mm:ss")
                        )
                    ),
                    8, y, 0xFF36B9FF
                );
                y += 12;
            }

            if (isEnabled("Base Finder")
                || isEnabled("Storage Finder")
                || isEnabled("Spawner Finder")) {

                for (FinderModules.Result result : FinderModules.getResults()) {
                    String label = result.type() + " "
                        + result.pos().getX() + " "
                        + result.pos().getY() + " "
                        + result.pos().getZ();

                    context.drawTextWithShadow(
                        client.textRenderer,
                        Text.literal(label),
                        8, y, 0xFF36B9FF
                    );
                    y += 12;

                    if (y > client.getWindow().getScaledHeight() - 20) {
                        break;
                    }
                }
            }
        });
    }

    private static void updateFullbright(MinecraftClient client) {
        if (client.options == null) return;

        if (isEnabled("Fullbright")) {
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

        private static final int BLUE = 0xFF36B9FF;
        private static final int PANEL = 0xF20D1422;
        private static final int CARD = 0xFF151F30;
        private static final int BORDER = 0xFF26364D;
        private static final int TEXT = 0xFFF0F6FF;
        private static final int MUTED = 0xFF879BB5;

        private TextFieldWidget searchField;

        private float openAnimation = 0.0f;
        private int selectedCategory = -1;
        private int scroll = 0;

        private final Map<String, Float> toggleAnimations =
            new HashMap<>();

        protected DonutScreen() {
            super(Text.literal("DonutClient"));
        }

        @Override
        protected void init() {
            int panelWidth = (int) (width * 0.92);
            int panelHeight = (int) (height * 0.88);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;

            searchField = new TextFieldWidget(
                textRenderer,
                panelX + 218,
                panelY + 19,
                Math.max(130, panelWidth - 260),
                24,
                Text.literal("Search modules")
            );

            searchField.setPlaceholder(
                Text.literal("Search modules...")
            );
            searchField.setMaxLength(40);

            addDrawableChild(searchField);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
        ) {
            openAnimation = Math.min(
                1.0f,
                openAnimation + delta * 4.0f
            );

            float eased = 1.0f
                - (1.0f - openAnimation) * (1.0f - openAnimation);

            context.fill(0, 0, width, height, 0x99030912);

            int panelWidth = (int) (width * 0.92);
            int panelHeight = (int) (height * 0.88);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;

            int animatedHeight = (int) (panelHeight * eased);
            int animatedY = panelY
                + (panelHeight - animatedHeight) / 2;

            context.fill(
                panelX - 2, animatedY - 2,
                panelX + panelWidth + 2,
                animatedY + animatedHeight + 2,
                0xFF080E18
            );

            context.fill(
                panelX, animatedY,
                panelX + panelWidth,
                animatedY + animatedHeight,
                PANEL
            );

            context.fill(
                panelX, animatedY,
                panelX + panelWidth, animatedY + 3,
                BLUE
            );

            if (openAnimation < 0.99f) {
                super.render(context, mouseX, mouseY, delta);
                return;
            }

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("DONUT"),
                panelX + 22, panelY + 18, BLUE
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("CLIENT"),
                panelX + 75, panelY + 18, TEXT
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("MODIFICATION PANEL / 1.0.0"),
                panelX + 22, panelY + 34, MUTED
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("RIGHT CTRL  •  CLOSE"),
                panelX + panelWidth - 145,
                panelY + 18, MUTED
            );

            int sidebarX = panelX + 12;
            int sidebarY = panelY + 62;
            int sidebarWidth = 174;
            int bottomY = panelY + panelHeight - 25;

            int contentX = sidebarX + sidebarWidth + 12;
            int contentWidth = panelWidth - sidebarWidth - 42;

            context.fill(
                sidebarX, sidebarY,
                sidebarX + sidebarWidth, bottomY,
                0xCC0A111E
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("NAVIGATION"),
                sidebarX + 12, sidebarY + 12, MUTED
            );

            for (int i = 0; i < CATEGORIES.length; i++) {
                int y = sidebarY + 34 + i * 38;

                boolean selected = selectedCategory == i;
                boolean hovered = mouseX >= sidebarX + 7
                    && mouseX <= sidebarX + sidebarWidth - 7
                    && mouseY >= y && mouseY <= y + 30;

                if (selected || hovered) {
                    context.fill(
                        sidebarX + 7, y,
                        sidebarX + sidebarWidth - 7, y + 30,
                        selected ? 0xFF173650 : 0xFF192538
                    );
                }

                if (selected) {
                    context.fill(
                        sidebarX + 7, y,
                        sidebarX + 10, y + 30, BLUE
                    );
                }

                context.drawTextWithShadow(
                    textRenderer,
                    Text.literal(CATEGORIES[i]),
                    sidebarX + 19, y + 11,
                    selected ? BLUE : TEXT
                );

                int count = 0;
                for (String module : MODULES[i]) {
                    if (matchesSearch(module)) count++;
                }

                context.drawTextWithShadow(
                    textRenderer,
                    Text.literal(String.valueOf(count)),
                    sidebarX + sidebarWidth - 24, y + 11, MUTED
                );
            }

            context.drawTextWithShadow(
                textRenderer,
                Text.literal(
                    selectedCategory < 0
                        ? "ALL MODULES"
                        : CATEGORIES[selectedCategory] + " MODULES"
                ),
                contentX + 3, sidebarY + 8, TEXT
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("Toggle a module to enable or disable it"),
                contentX + 3, sidebarY + 23, MUTED
            );

            int gridY = sidebarY + 43;
            int gridBottom = bottomY - 8;
            int cardGap = 9;
            int cardWidth = (contentWidth - cardGap) / 2;
            int cardHeight = 39;
            int rowGap = 7;
            int row = 0;

            for (int c = 0; c < MODULES.length; c++) {
                if (selectedCategory >= 0 && selectedCategory != c) {
                    continue;
                }

                for (String module : MODULES[c]) {
                    if (!matchesSearch(module)) continue;

                    int col = row % 2;
                    int visualRow = row / 2;

                    int x = contentX
                        + col * (cardWidth + cardGap);
                    int y = gridY
                        + visualRow * (cardHeight + rowGap) - scroll;

                    row++;

                    if (y + cardHeight < gridY || y > gridBottom) {
                        continue;
                    }

                    boolean active = isEnabled(module);
                    boolean supported = isSupported(module);

                    boolean hovered = mouseX >= x
                        && mouseX <= x + cardWidth
                        && mouseY >= y
                        && mouseY <= y + cardHeight;

                    float progress = toggleAnimations.getOrDefault(
                        module, active ? 1.0f : 0.0f
                    );

                    progress += ((active ? 1.0f : 0.0f) - progress)
                        * Math.min(1.0f, delta * 12.0f);

                    toggleAnimations.put(module, progress);

                    context.fill(
                        x, y, x + cardWidth, y + cardHeight,
                        hovered ? 0xFF202E43 : CARD
                    );

                    context.fill(
                        x, y, x + 2, y + cardHeight,
                        active ? BLUE : BORDER
                    );

                    context.drawTextWithShadow(
                        textRenderer,
                        Text.literal(module),
                        x + 11, y + 8,
                        active ? BLUE : TEXT
                    );

                    context.drawTextWithShadow(
                        textRenderer,
                        Text.literal(
                            !supported ? "SOON" : active ? "ON" : "OFF"
                        ),
                        x + 11, y + 23,
                        !supported ? MUTED : active ? BLUE : MUTED
                    );

                    int toggleX = x + cardWidth - 40;
                    int toggleY = y + 11;

                    context.fill(
                        toggleX, toggleY,
                        toggleX + 30, toggleY + 16,
                        blendColor(0xFF35445A, BLUE, progress)
                    );

                    int knobX = toggleX + 2
                        + Math.round(progress * 14);

                    context.fill(
                        knobX, toggleY + 2,
                        knobX + 12, toggleY + 14,
                        0xFFF4F8FF
                    );
                }
            }

            if (row == 0) {
                context.drawTextWithShadow(
                    textRenderer,
                    Text.literal("No modules found"),
                    contentX + 8, gridY + 12, MUTED
                );
            }

            context.fill(
                panelX + 12, bottomY,
                panelX + panelWidth - 12, bottomY + 1,
                BORDER
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("DONUTCLIENT"),
                panelX + 18, bottomY + 8, BLUE
            );

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("Minecraft 1.21.8  •  Fabric"),
                panelX + panelWidth - 190,
                bottomY + 8, MUTED
            );

            super.render(context, mouseX, mouseY, delta);
        }

        private boolean matchesSearch(String module) {
            if (searchField == null) return true;

            String query = searchField.getText()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

            return query.isEmpty()
                || module.toLowerCase(java.util.Locale.ROOT)
                    .contains(query);
        }

        private int blendColor(int from, int to, float amount) {
            amount = Math.max(0.0f, Math.min(1.0f, amount));

            int a = (int) (((from >>> 24) & 255) * (1 - amount)
                + ((to >>> 24) & 255) * amount);
            int r = (int) (((from >>> 16) & 255) * (1 - amount)
                + ((to >>> 16) & 255) * amount);
            int g = (int) (((from >>> 8) & 255) * (1 - amount)
                + ((to >>> 8) & 255) * amount);
            int b = (int) ((from & 255) * (1 - amount)
                + (to & 255) * amount);

            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private boolean isSupported(String module) {
            return switch (module) {
                case "HUD", "FPS Counter", "Coordinates", "Clock",
                     "Fullbright", "Freecam", "Storage Finder",
                     "Spawner Finder", "Base Finder" -> true;
                default -> false;
            };
        }

        @Override
        public boolean mouseClicked(
            double mouseX, double mouseY, int button
        ) {
            if (button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int panelWidth = (int) (width * 0.92);
            int panelHeight = (int) (height * 0.88);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;

            int sidebarX = panelX + 12;
            int sidebarY = panelY + 62;
            int sidebarWidth = 174;
            int bottomY = panelY + panelHeight - 25;

            for (int i = 0; i < CATEGORIES.length; i++) {
                int y = sidebarY + 34 + i * 38;

                if (mouseX >= sidebarX + 7
                    && mouseX <= sidebarX + sidebarWidth - 7
                    && mouseY >= y && mouseY <= y + 30) {
                    selectedCategory = selectedCategory == i ? -1 : i;
                    scroll = 0;
                    return true;
                }
            }

            int contentX = sidebarX + sidebarWidth + 12;
            int contentWidth = panelWidth - sidebarWidth - 42;
            int gridY = sidebarY + 43;
            int gridBottom = bottomY - 8;
            int cardGap = 9;
            int cardWidth = (contentWidth - cardGap) / 2;
            int cardHeight = 39;
            int rowGap = 7;
            int row = 0;

            for (int c = 0; c < MODULES.length; c++) {
                if (selectedCategory >= 0 && selectedCategory != c) {
                    continue;
                }

                for (String module : MODULES[c]) {
                    if (!matchesSearch(module)) continue;

                    int col = row % 2;
                    int visualRow = row / 2;

                    int x = contentX + col * (cardWidth + cardGap);
                    int y = gridY
                        + visualRow * (cardHeight + rowGap) - scroll;

                    row++;

                    if (y + cardHeight < gridY || y > gridBottom) {
                        continue;
                    }

                    if (mouseX >= x && mouseX <= x + cardWidth
                        && mouseY >= y && mouseY <= y + cardHeight) {
                        if (!isSupported(module)) return true;

                        setEnabled(module, !isEnabled(module));
                        return true;
                    }
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
        ) {
            scroll = Math.max(
                0,
                Math.min(600, scroll - (int) (verticalAmount * 35))
            );

            return true;
        }
    }
}

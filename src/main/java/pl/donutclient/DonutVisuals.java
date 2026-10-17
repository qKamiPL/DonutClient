
package pl.donutclient;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.glfw.GLFW;

public final class DonutVisuals {
    private static long ticks;
    private static long lastSecond;
    private static int cps;
    private static int clicks;
    private static boolean leftWasDown;

    private DonutVisuals() {}

    public static void register() {
        lastSecond = System.currentTimeMillis();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }

            long handle = client.getWindow().getHandle();
            boolean leftDown =
                GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    == GLFW.GLFW_PRESS;

            if (leftDown && !leftWasDown) {
                clicks++;
            }

            leftWasDown = leftDown;

            long now = System.currentTimeMillis();

            if (now - lastSecond >= 1000L) {
                cps = clicks;
                clicks = 0;
                lastSecond = now;
            }

            ticks++;

            if (ticks % 2 != 0) {
                return;
            }

            double px = client.player.getX();
            double py = client.player.getY();
            double pz = client.player.getZ();

            // Cyan halo: trzy punkty poruszają się po pierścieniu.
            if (DonutClient.isModuleEnabled("Halo Cyan")) {
                double angle = ticks * 0.13;
                double radius = 0.55;
                double y = py + client.player.getHeight() + 0.22;

                for (int i = 0; i < 3; i++) {
                    double a = angle + i * Math.PI * 2.0 / 3.0;

                    client.world.addParticle(
                        ParticleTypes.END_ROD,
                        px + Math.cos(a) * radius,
                        y,
                        pz + Math.sin(a) * radius,
                        0.0, 0.01, 0.0
                    );
                }
            }

            // Particle Aura: obracające się cząsteczki przy graczu.
            if (DonutClient.isModuleEnabled("Particle Aura")) {
                double angle = ticks * 0.09;
                double radius = 0.8;

                client.world.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    px + Math.cos(angle) * radius,
                    py + 0.7 + Math.sin(angle * 1.7) * 0.35,
                    pz + Math.sin(angle) * radius,
                    0.0, 0.01, 0.0
                );

                client.world.addParticle(
                    ParticleTypes.END_ROD,
                    px + Math.cos(angle + Math.PI) * radius,
                    py + 1.2,
                    pz + Math.sin(angle + Math.PI) * radius,
                    0.0, 0.01, 0.0
                );
            }

            // Efekt cząsteczek przy pobliskich skrzyniach.
            if (DonutClient.isModuleEnabled("Colored Chest ESP")
                && ticks % 6 == 0) {
                showNearbyChestParticles(client);
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null) {
                return;
            }

            int y = 8;

            if (DonutClient.isModuleEnabled("CPS Counter")) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("CPS: " + cps),
                    8, y, 0xFF20CFFF
                );
            }
        });
    }

    private static void showNearbyChestParticles(
        MinecraftClient client
    ) {
        BlockPos center = client.player.getBlockPos();
        int radius = 6;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    var state = client.world.getBlockState(pos);

                    if (state.isOf(Blocks.CHEST)
                        || state.isOf(Blocks.TRAPPED_CHEST)
                        || state.isOf(Blocks.ENDER_CHEST)
                        || state.isOf(Blocks.BARREL)) {

                        double[][] corners = {
                            {0.03, 0.03, 0.03},
                            {0.97, 0.03, 0.03},
                            {0.03, 0.97, 0.03},
                            {0.97, 0.97, 0.03},
                            {0.03, 0.03, 0.97},
                            {0.97, 0.03, 0.97},
                            {0.03, 0.97, 0.97},
                            {0.97, 0.97, 0.97}
                        };

                        for (double[] corner : corners) {
                            client.world.addParticle(
                                ParticleTypes.END_ROD,
                                pos.getX() + corner[0],
                                pos.getY() + corner[1],
                                pos.getZ() + corner[2],
                                0.0, 0.0, 0.0
                            );
                        }
                    }
                }
            }
        }
    }
}

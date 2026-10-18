
package pl.donutclient;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.glfw.GLFW;

public final class DonutVisuals {

    private static int cps = 0;
    private static int ticksSinceCpsReset = 0;
    private static int chestScanTicks = 0;
    private static boolean previousLeftMouseState = false;

    private DonutVisuals() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(DonutVisuals::onClientTick);
        HudRenderCallback.EVENT.register(DonutVisuals::renderHud);
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        updateCps(client);
        updateHalo(client);
        updateParticleAura(client);
        updateChestParticles(client);
    }

    private static void updateCps(MinecraftClient client) {
        boolean pressed = GLFW.glfwGetMouseButton(
                client.getWindow().getHandle(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        if (pressed && !previousLeftMouseState) {
            cps++;
        }

        previousLeftMouseState = pressed;
        ticksSinceCpsReset++;

        if (ticksSinceCpsReset >= 20) {
            cps = 0;
            ticksSinceCpsReset = 0;
        }
    }

    private static void updateHalo(MinecraftClient client) {
        if (!DonutClient.isModuleEnabled("Halo Cyan")) {
            return;
        }

        double time = client.player.age * 0.08;
        double radius = 0.65;
        double x0 = client.player.getX();
        double y = client.player.getY() + 2.15;
        double z0 = client.player.getZ();

        for (int i = 0; i < 3; i++) {
            double angle = time + Math.PI * 2.0 * i / 3.0;

            client.particleManager.addParticle(
                    ParticleTypes.END_ROD,
                    x0 + Math.cos(angle) * radius,
                    y,
                    z0 + Math.sin(angle) * radius,
                    0.0, 0.0, 0.0
            );
        }
    }

    private static void updateParticleAura(MinecraftClient client) {
        if (!DonutClient.isModuleEnabled("Particle Aura")) {
            return;
        }

        double time = client.player.age * 0.12;
        double radius = 0.8;

        for (int i = 0; i < 2; i++) {
            double angle = time + Math.PI * i;

            client.particleManager.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    client.player.getX() + Math.cos(angle) * radius,
                    client.player.getY() + 0.5
                            + Math.sin(time * 1.5 + i) * 0.35,
                    client.player.getZ() + Math.sin(angle) * radius,
                    0.0, 0.01, 0.0
            );
        }
    }

    private static void updateChestParticles(MinecraftClient client) {
        if (!DonutClient.isModuleEnabled("Colored Chest ESP")) {
            return;
        }

        chestScanTicks++;
        if (chestScanTicks < 6) {
            return;
        }
        chestScanTicks = 0;

        BlockPos playerPos = client.player.getBlockPos();
        int radius = 6;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = client.world.getBlockState(pos);

                    if (isStorageBlock(state)) {
                        spawnChestParticles(client, pos);
                    }
                }
            }
        }
    }

    private static boolean isStorageBlock(BlockState state) {
        return state.isOf(Blocks.CHEST)
                || state.isOf(Blocks.TRAPPED_CHEST)
                || state.isOf(Blocks.ENDER_CHEST)
                || state.isOf(Blocks.BARREL);
    }

    private static void spawnChestParticles(
            MinecraftClient client,
            BlockPos pos
    ) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        double[][] corners = {
                {x + 0.05, y + 0.05, z + 0.05},
                {x + 0.95, y + 0.05, z + 0.05},
                {x + 0.05, y + 0.95, z + 0.05},
                {x + 0.95, y + 0.95, z + 0.05},
                {x + 0.05, y + 0.05, z + 0.95},
                {x + 0.95, y + 0.05, z + 0.95},
                {x + 0.05, y + 0.95, z + 0.95},
                {x + 0.95, y + 0.95, z + 0.95}
        };

        for (double[] corner : corners) {
            client.particleManager.addParticle(
                    ParticleTypes.END_ROD,
                    corner[0], corner[1], corner[2],
                    0.0, 0.0, 0.0
            );
        }
    }

    private static void renderHud(
            DrawContext context,
            net.minecraft.client.render.RenderTickCounter tickCounter
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.options.hudHidden) {
            return;
        }

        if (DonutClient.isModuleEnabled("CPS Counter")) {
            context.drawText(
                    client.textRenderer,
                    Text.literal("CPS: " + cps),
                    8, 30,
                    0xFFFFFF,
                    true
            );
        }
    }
}

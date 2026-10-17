
package pl.donutclient;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class FinderEsp {
    private static boolean storageEnabled = false;
    private static boolean spawnerEnabled = false;

    private FinderEsp() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(FinderEsp::render);
    }

    public static void setEnabled(boolean storage, boolean spawner) {
        storageEnabled = storage;
        spawnerEnabled = spawner;
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;
        if (!storageEnabled && !spawnerEnabled) return;

        var matrices = context.matrixStack();
        if (matrices == null || context.consumers() == null) return;

        Vec3d camera = context.camera().getPos();
        VertexConsumer lines = context.consumers()
            .getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        for (FinderModules.Result result : FinderModules.getResults()) {
            boolean isSpawner = result.type().equals("Spawner");

            if (isSpawner && !spawnerEnabled) continue;
            if (!isSpawner && !storageEnabled) continue;

            BlockPos pos = result.pos();

            // Skip blocks outside the current render distance.
            if (pos.getSquaredDistance(client.player.getBlockPos()) > 64 * 64) {
                continue;
            }

            Box box = new Box(pos).expand(0.003);

            float red = isSpawner ? 0.75f : 0.10f;
            float green = isSpawner ? 0.20f : 0.65f;
            float blue = isSpawner ? 1.00f : 1.00f;

            WorldRenderer.drawBox(
                matrices,
                lines,
                box,
                red,
                green,
                blue,
                1.0f
            );
        }

        matrices.pop();
    }
}

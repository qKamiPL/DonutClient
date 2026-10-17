
package pl.donutclient;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
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

        MatrixStack matrices = context.matrixStack();
        if (matrices == null || context.consumers() == null) return;

        Vec3d camera = context.camera().getPos();
        VertexConsumer lines =
            context.consumers().getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        for (FinderModules.Result result : FinderModules.getResults()) {
            boolean isSpawner = result.type().equals("Spawner");

            if (isSpawner && !spawnerEnabled) continue;
            if (!isSpawner && !storageEnabled) continue;

            BlockPos pos = result.pos();

            if (pos.getSquaredDistance(client.player.getBlockPos()) > 64 * 64) {
                continue;
            }

            Box box = new Box(pos).expand(0.003);

            float red = isSpawner ? 0.75f : 0.10f;
            float green = isSpawner ? 0.20f : 0.65f;
            float blue = 1.0f;

            drawBoxLines(matrices, lines, box, red, green, blue);
        }

        matrices.pop();
    }

    private static void drawBoxLines(
        MatrixStack matrices,
        VertexConsumer lines,
        Box box,
        float red,
        float green,
        float blue
    ) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // Dolna krawędź
        line(matrices, lines, x1, y1, z1, x2, y1, z1, red, green, blue);
        line(matrices, lines, x2, y1, z1, x2, y1, z2, red, green, blue);
        line(matrices, lines, x2, y1, z2, x1, y1, z2, red, green, blue);
        line(matrices, lines, x1, y1, z2, x1, y1, z1, red, green, blue);

        // Górna krawędź
        line(matrices, lines, x1, y2, z1, x2, y2, z1, red, green, blue);
        line(matrices, lines, x2, y2, z1, x2, y2, z2, red, green, blue);
        line(matrices, lines, x2, y2, z2, x1, y2, z2, red, green, blue);
        line(matrices, lines, x1, y2, z2, x1, y2, z1, red, green, blue);

        // Pionowe krawędzie
        line(matrices, lines, x1, y1, z1, x1, y2, z1, red, green, blue);
        line(matrices, lines, x2, y1, z1, x2, y2, z1, red, green, blue);
        line(matrices, lines, x2, y1, z2, x2, y2, z2, red, green, blue);
        line(matrices, lines, x1, y1, z2, x1, y2, z2, red, green, blue);
    }

    private static void line(
        MatrixStack matrices,
        VertexConsumer consumer,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float red, float green, float blue
    ) {
        MatrixStack.Entry entry = matrices.peek();

        consumer.vertex(entry, x1, y1, z1)
            .color(red, green, blue, 1.0f)
            .normal(entry, 0.0f, 1.0f, 0.0f);
        consumer.vertex(entry, x2, y2, z2)
            .color(red, green, blue, 1.0f)
            .normal(entry, 0.0f, 1.0f, 0.0f);
    }
}

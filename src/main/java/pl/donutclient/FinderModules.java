
package pl.donutclient;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FinderModules {
    private static final int RADIUS = 48;
    private static final int MAX_RESULTS = 30;

    private static final List<Result> results = new ArrayList<>();
    private static long lastScan = 0;
    private static String lastMode = "";

    private FinderModules() {}

    public record Result(String type, BlockPos pos, double distance) {}

    public static List<Result> getResults() {
        return List.copyOf(results);
    }

    public static void update(
        MinecraftClient client,
        boolean storage,
        boolean spawner,
        boolean base
    ) {
        if (client.player == null || client.world == null) {
            results.clear();
            lastMode = "";
            return;
        }

        String mode = (storage ? "S" : "")
            + (spawner ? "P" : "")
            + (base ? "B" : "");

        if (mode.isEmpty()) {
            results.clear();
            lastMode = "";
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastScan < 1500 && mode.equals(lastMode)) return;

        lastScan = now;
        lastMode = mode;
        results.clear();

        BlockPos center = client.player.getBlockPos();
        int minX = center.getX() - RADIUS;
        int maxX = center.getX() + RADIUS;
        int minY = Math.max(client.world.getBottomY(), center.getY() - RADIUS);
        int maxY = Math.min(client.world.getTopYInclusive(), center.getY() + RADIUS);
        int minZ = center.getZ() - RADIUS;
        int maxZ = center.getZ() + RADIUS;

        // Scan only chunks that are already loaded by the client.
        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                if (!client.world.isChunkLoaded(new ChunkPos(cx, cz).getStartPos())) {
                    continue;
                }

                int x0 = Math.max(minX, cx << 4);
                int x1 = Math.min(maxX, (cx << 4) + 15);
                int z0 = Math.max(minZ, cz << 4);
                int z1 = Math.min(maxZ, (cz << 4) + 15);

                for (int x = x0; x <= x1; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = z0; z <= z1; z++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = client.world.getBlockState(pos);
                            Block block = state.getBlock();

                            String type = null;

                            if (storage && isStorage(block)) {
                                type = "Storage";
                            } else if (spawner && block == Blocks.SPAWNER) {
                                type = "Spawner";
                            } else if (base && isBaseClue(block)) {
                                type = "Base clue";
                            }

                            if (type != null) {
                                results.add(new Result(
                                    type,
                                    pos.toImmutable(),
                                    Math.sqrt(pos.getSquaredDistance(center))
                                ));
                            }
                        }
                    }
                }
            }
        }

        results.sort(Comparator.comparingDouble(Result::distance));

        if (results.size() > MAX_RESULTS) {
            results.subList(MAX_RESULTS, results.size()).clear();
        }

        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§b[Donut Finder] §fZnaleziono: §e"
                    + results.size() + " §fwyników w załadowanym obszarze."),
                true
            );
        }
    }

   
private static boolean isBaseClue(Block block) {
    return isStorage(block)
        || block == Blocks.CRAFTING_TABLE
        || block == Blocks.FURNACE
        || block == Blocks.BLAST_FURNACE
        || block == Blocks.SMOKER
        || block == Blocks.ANVIL
        || block == Blocks.CHIPPED_ANVIL
        || block == Blocks.DAMAGED_ANVIL
        || block == Blocks.WHITE_BED
        || block == Blocks.ORANGE_BED
        || block == Blocks.MAGENTA_BED
        || block == Blocks.LIGHT_BLUE_BED
        || block == Blocks.YELLOW_BED
        || block == Blocks.LIME_BED
        || block == Blocks.PINK_BED
        || block == Blocks.GRAY_BED
        || block == Blocks.LIGHT_GRAY_BED
        || block == Blocks.CYAN_BED
        || block == Blocks.PURPLE_BED
        || block == Blocks.BLUE_BED
        || block == Blocks.BROWN_BED
        || block == Blocks.GREEN_BED
        || block == Blocks.RED_BED
        || block == Blocks.BLACK_BED
        || block == Blocks.RESPAWN_ANCHOR
        || block == Blocks.ENCHANTING_TABLE;
}

    private static boolean isBaseClue(Block block) {
        return isStorage(block)
            || block == Blocks.CRAFTING_TABLE
            || block == Blocks.FURNACE
            || block == Blocks.BLAST_FURNACE
            || block == Blocks.SMOKER
            || block == Blocks.ANVIL
            || block == Blocks.BED
            || block == Blocks.RESPAWN_ANCHOR
            || block == Blocks.ENCHANTING_TABLE;
    }
}

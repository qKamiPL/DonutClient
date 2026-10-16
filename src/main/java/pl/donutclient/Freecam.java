
package pl.donutclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;

public final class Freecam {
    private static Entity camera;
    private static boolean active = false;

    private Freecam() {}

    public static void update(MinecraftClient client, boolean enabled) {
        if (client.player == null || client.world == null) {
            stop(client);
            return;
        }

        if (enabled && !active) {
            start(client);
        } else if (!enabled && active) {
            stop(client);
        }

        if (!active || camera == null) return;

        double speed = client.options.sprintKey.isPressed() ? 0.8 : 0.25;

        float yaw = camera.getYaw();
        double radians = Math.toRadians(yaw);

        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);
        double rightX = Math.cos(radians);
        double rightZ = Math.sin(radians);

        double dx = 0;
        double dy = 0;
        double dz = 0;

        if (client.options.forwardKey.isPressed()) {
            dx += forwardX;
            dz += forwardZ;
        }
        if (client.options.backKey.isPressed()) {
            dx -= forwardX;
            dz -= forwardZ;
        }
        if (client.options.rightKey.isPressed()) {
            dx += rightX;
            dz += rightZ;
        }
        if (client.options.leftKey.isPressed()) {
            dx -= rightX;
            dz -= rightZ;
        }
        if (client.options.jumpKey.isPressed()) dy += 1;
        if (client.options.sneakKey.isPressed()) dy -= 1;

        Vec3d movement = new Vec3d(dx, dy, dz);
        if (movement.lengthSquared() > 0) {
            movement = movement.normalize().multiply(speed);
            camera.setPosition(
                camera.getX() + movement.x,
                camera.getY() + movement.y,
                camera.getZ() + movement.z
            );
        }
    }

    private static void start(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ArmorStandEntity stand =
            EntityType.ARMOR_STAND.create(
    client.world,
    net.minecraft.entity.SpawnReason.TRIGGERED
);

        if (stand == null) return;

        stand.refreshPositionAndAngles(
            client.player.getX(),
            client.player.getY(),
            client.player.getZ(),
            client.player.getYaw(),
            client.player.getPitch()
        );

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.noClip = true;

        camera = stand;
        client.world.addEntity(-123456, stand);
        client.setCameraEntity(stand);
        active = true;
    }

    private static void stop(MinecraftClient client) {
        if (!active) return;

        if (client.player != null) {
            client.setCameraEntity(client.player);
        }

        if (camera != null) {
            camera.setRemoved(Entity.RemovalReason.DISCARDED);
            camera = null;
        }

        active = false;
    }
}

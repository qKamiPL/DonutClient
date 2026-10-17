
package pl.donutclient.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pl.donutclient.Freecam;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;changeLookDirection(DD)V"
        )
    )
    private void donutclient$redirectCameraLook(
        Entity entity,
        double deltaX,
        double deltaY
    ) {
        if (Freecam.isActive()) {
            Freecam.rotate(deltaX, deltaY);
        } else {
            entity.changeLookDirection(deltaX, deltaY);
        }
    }
}

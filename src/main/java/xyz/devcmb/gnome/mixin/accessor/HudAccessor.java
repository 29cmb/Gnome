package xyz.devcmb.gnome.mixin.accessor;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {
    @Accessor("overlayMessageString")
    @Nullable Component gnome$getOverlayMessageString();

    @Accessor("overlayMessageString")
    void gnome$setOverlayMessageString(@Nullable Component overlayMessageString);
}

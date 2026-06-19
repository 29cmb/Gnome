package xyz.devcmb.gnome.mixin.accessor;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface TabListAccessor {
    @Accessor("footer")
    @Nullable Component gnome$getFooter();
}
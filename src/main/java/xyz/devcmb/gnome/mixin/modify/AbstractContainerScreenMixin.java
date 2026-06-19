package xyz.devcmb.gnome.mixin.modify;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.devcmb.gnome.Gnome;
import xyz.devcmb.gnome.feature.IslandCompletion;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        Gnome.INSTANCE
                .getFeature(IslandCompletion.class)
                .renderSlot(graphics, slot);
    }
}

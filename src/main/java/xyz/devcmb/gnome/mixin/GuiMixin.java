package xyz.devcmb.gnome.mixin;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.devcmb.gnome.UtilKt;

@Mixin(Gui.class)
public class GuiMixin {
    @ModifyVariable(method = "extractSelectedItemName", at = @At("STORE"), name = "y")
    private int raiseItem(int y) {
        if(UtilKt.isOnIsland() && UtilKt.isOnFishing()) return y - 12;
        else return y;
    }
}

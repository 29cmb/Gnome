package xyz.devcmb.gnome.mixin.modify;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.devcmb.gnome.Gnome;
import xyz.devcmb.gnome.data.Island;
import xyz.devcmb.gnome.feature.IslandFishTracker;
import xyz.devcmb.gnome.util.UtilKt;

import java.util.Arrays;

@Mixin(Gui.class)
public class GuiMixin {
    @ModifyVariable(method = "extractSelectedItemName", at = @At("STORE"), name = "y")
    private int raiseItem(int y) {
        if(UtilKt.isOnIsland() && UtilKt.isOnFishing()) return y - 12;
        else return y;
    }

    @ModifyExpressionValue(
        method = "displayScoreboardSidebar",
        at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toArray(Ljava/util/function/IntFunction;)[Ljava/lang/Object;")
    )
    private Object[] addEntries(Object[] original) {
        try {
            if(!UtilKt.isOnIsland() || !UtilKt.isOnFishing()) return original;

            Component[] components = Gnome.INSTANCE.getFeature(IslandFishTracker.class).addScoreboardEntries().toArray(new Component[0]);
            Class<?> entryClass = original.getClass().getComponentType();

            Object[] modified = Arrays.copyOf(original, original.length + components.length);
            for(int i = 0; i < components.length; i++) {
                modified[original.length + i] = entryClass.getDeclaredConstructors()[0].newInstance(
                    components[i],
                    Component.empty(),
                    0
                );
            }

            return modified;
        } catch(Exception e) {
            Gnome.INSTANCE.getLogger().error("Failed to add entries to scoreboard: {}", e.getMessage());
            return original;
        }
    }

    @Inject(method = "displayScoreboardSidebar", at = @At("TAIL"))
    void displayScoreboardSidebar(GuiGraphicsExtractor graphics, Objective objective, CallbackInfo ci) {
        if(!UtilKt.isOnIsland() || !UtilKt.isOnFishing()) return;
        String scoreboardTitle = objective.getDisplayName().getString();
        if(!scoreboardTitle.contains("MCCI: ")) return;

        Island.Companion.updateCurrentIsland(scoreboardTitle);
    }
}

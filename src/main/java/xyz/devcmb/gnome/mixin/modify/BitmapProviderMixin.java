package xyz.devcmb.gnome.mixin.modify;

import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.devcmb.gnome.util.Font;

@Mixin(BitmapProvider.Definition.class)
public class BitmapProviderMixin {
    // "i wouldn't recommend copying my font lookup class" - pe3ep 2026
    // its 12am, nothing works, jank is better than nothing
    // thank you pe3ep the goat
    // https://github.com/pe3ep/Trident/blob/master/src/main/java/cc/pe3epwithyou/trident/mixin/FontLoaderMixin.java

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Identifier file, int height, int ascent, int[][] codepointGrid, CallbackInfo ci) {
        if(!file.getNamespace().equals("mcc") || !file.getPath().contains("_fonts")) return;

        StringBuilder builder = new StringBuilder();
        for(int codePoint : codepointGrid[0]) {
            builder.appendCodePoint(codePoint);
        }

        Font.INSTANCE.registerGlyph(file.getPath(), builder.toString());
    }
}
package de.dafuqs.chalk.common.items;

import de.dafuqs.chalk.common.*;
import net.minecraft.block.*;
import net.minecraft.util.*;

public class GlowChalkItem extends ChalkItem {
    public GlowChalkItem(Settings settings, DyeColor dyeColor) {
        super(settings, dyeColor);
    }
    
    public Block getChalkMarkBlock() {
        return ChalkRegistry.chalkVariants.get(this.dyeColor).glowChalkBlock;
    }
}
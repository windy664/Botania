package vazkii.botania.api.internal;

import net.minecraft.world.item.DyeColor;

/**
 * Specifies the color of a thing. That doesn't necessarily mean that thing is dyeable.
 */
public interface Colored {
	DyeColor getColor();
}

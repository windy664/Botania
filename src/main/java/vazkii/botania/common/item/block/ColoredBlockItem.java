package vazkii.botania.common.item.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import vazkii.botania.api.internal.Colored;

public class ColoredBlockItem extends BlockItem implements Colored {
	private final DyeColor color;

	public ColoredBlockItem(Block block, DyeColor color, Properties properties) {
		super(block, properties);
		this.color = color;
	}

	@Override
	public DyeColor getColor() {
		return color;
	}
}

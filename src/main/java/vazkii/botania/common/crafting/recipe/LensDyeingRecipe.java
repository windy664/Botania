/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;


import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.LensItem;


public class LensDyeingRecipe extends CustomRecipe {
	public static final RecipeSerializer<LensDyeingRecipe> SERIALIZER = SimpleRecipeSerializerHelper.of(LensDyeingRecipe::new);

	public LensDyeingRecipe() {}

	@Override
	public RecipeSerializer<LensDyeingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundLens = false;
		boolean foundDye = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof BasicLensItem && !foundLens) {
					foundLens = true;
				} else if (!foundDye) {
					// TODO: rainbow tinting items should be a tag
					if (stack.getItem() instanceof DyeItem || stack.is(BotaniaItems.manaPearl)) {
						foundDye = true;
					} else {
						return false;
					}
				} else {
					return false;//This means we have an additional item in the recipe after the lens and dye
				}
			}
		}

		return foundLens && foundDye;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack lens = ItemStack.EMPTY;
		DyeColor color = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof BasicLensItem && lens.isEmpty()) {
					lens = stack;
				} else if (stack.getItem() instanceof DyeItem) {
					// we can assume that otherwise it's rainbow color, as we matched the ingredients already
					for (DyeColor dyeColor : DyeColor.values()) {
						if (stack.is(Items.DYE.pick(dyeColor))) {
							color = dyeColor;
							break;
						}
					}
				}
			}
		}

		if (lens.getItem() instanceof BasicLensItem) {
			ItemStack lensCopy = lens.copyWithCount(1);
			if (color != null) {
				LensItem.setLensColor(lensCopy, color);
			} else {
				LensItem.setLensRainbow(lensCopy);
			}

			return lensCopy;
		}

		return ItemStack.EMPTY;
	}

}

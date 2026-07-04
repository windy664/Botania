/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import vazkii.botania.mixin.ShapedRecipeAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.function.Function;

public class ManaUpgradeRecipe extends ShapedRecipe {
	public static final WrappingRecipeSerializer<ManaUpgradeRecipe> SERIALIZER = new Serializer();

	private ManaUpgradeRecipe(ShapedRecipe recipe) {
		super(new Recipe.CommonInfo(recipe.showNotification()),
				new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
				((ShapedRecipeAccessor) recipe).botania_getPattern(),
				((ShapedRecipeAccessor) recipe).botania_getResult());
	}

	public static ItemStack output(ItemStack output, CraftingInput inv) {
		ItemStack out = output.copy();
		var outItem = XplatAbstractions.INSTANCE.findManaItem(out);
		if (outItem == null) {
			return out;
		}
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			var item = XplatAbstractions.INSTANCE.findManaItem(stack);
			if (!stack.isEmpty() && item != null) {
				outItem.addMana(item.getMana());
			}
		}
		return out;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		return output(super.assemble(inv), inv);
	}

	@Override
	public RecipeSerializer<ManaUpgradeRecipe> getSerializer() {
		return SERIALIZER.serializer;
	}

	private static class Serializer extends WrappingRecipeSerializer<ManaUpgradeRecipe> {
		public static final MapCodec<ManaUpgradeRecipe> CODEC = ShapedRecipe.MAP_CODEC
				.xmap(ManaUpgradeRecipe::new, Function.identity());
		public static final StreamCodec<RegistryFriendlyByteBuf, ManaUpgradeRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC
				.map(ManaUpgradeRecipe::new, Function.identity());

		Serializer() {
			super(CODEC, STREAM_CODEC);
		}

		@Override
		public ManaUpgradeRecipe wrap(Recipe<?> recipe) {
			if (!(recipe instanceof ShapedRecipe shapedRecipe)) {
				throw new IllegalArgumentException("Unsupported recipe type to wrap: " + recipe.getType());
			}
			return new ManaUpgradeRecipe(shapedRecipe);
		}
	}
}

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
import net.minecraft.world.item.crafting.ShapelessRecipe;

import vazkii.botania.mixin.ShapelessRecipeAccessor;

import java.util.function.Function;

public class ShapelessManaUpgradeRecipe extends ShapelessRecipe {
	public static final WrappingRecipeSerializer<ShapelessManaUpgradeRecipe> SERIALIZER = new Serializer();

	private ShapelessManaUpgradeRecipe(ShapelessRecipe recipe) {
		super(new Recipe.CommonInfo(recipe.showNotification()),
				new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
				((ShapelessRecipeAccessor) recipe).botania_getResult(),
				((ShapelessRecipeAccessor) recipe).botania_getIngredients());
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		return ManaUpgradeRecipe.output(super.assemble(inv), inv);
	}

	@Override
	public RecipeSerializer<ShapelessManaUpgradeRecipe> getSerializer() {
		return SERIALIZER.serializer;
	}

	private static class Serializer extends WrappingRecipeSerializer<ShapelessManaUpgradeRecipe> {
		public static final MapCodec<ShapelessManaUpgradeRecipe> CODEC = ShapelessRecipe.MAP_CODEC
				.xmap(ShapelessManaUpgradeRecipe::new, Function.identity());
		public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessManaUpgradeRecipe> STREAM_CODEC = ShapelessRecipe.STREAM_CODEC
				.map(ShapelessManaUpgradeRecipe::new, Function.identity());

		Serializer() {
			super(CODEC, STREAM_CODEC);
		}

		@Override
		public ShapelessManaUpgradeRecipe wrap(Recipe<?> recipe) {
			if (!(recipe instanceof ShapelessRecipe shapelessRecipe)) {
				throw new IllegalArgumentException("Unsupported recipe type to wrap: " + recipe.getType());
			}
			return new ShapelessManaUpgradeRecipe(shapelessRecipe);
		}
	}
}

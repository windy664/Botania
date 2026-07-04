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

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import vazkii.botania.mixin.ShapedRecipeAccessor;
import vazkii.botania.mixin.ShapedRecipePatternAccessor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class WaterBottleMatchingRecipe extends ShapedRecipe {
	public static final WrappingRecipeSerializer<WaterBottleMatchingRecipe> SERIALIZER = new Serializer();

	private static ShapedRecipePattern transformPattern(ShapedRecipePattern pattern) {
		final var testPotion = new ItemStack(Items.POTION);
		final List<Optional<Ingredient>> ingredients = pattern.ingredients().stream()
				.map(opt -> opt.map(i -> i.test(testPotion)
						? Ingredient.of(Items.POTION)
						: i))
				.toList();
		return new ShapedRecipePattern(pattern.width(), pattern.height(), ingredients,
				// TODO: verify
				((ShapedRecipePatternAccessor) (Object) pattern).getData());
	}

	public WaterBottleMatchingRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
		super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(category, group),
				transformPattern(pattern), ItemStackTemplate.fromStack(result));
	}

	private WaterBottleMatchingRecipe(ShapedRecipe recipe) {
		super(new Recipe.CommonInfo(recipe.showNotification()),
				new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
				transformPattern(((ShapedRecipeAccessor) recipe).botania_getPattern()),
				((ShapedRecipeAccessor) recipe).botania_getResult());
	}

	@Override
	public boolean matches(CraftingInput craftingContainer, Level level) {
		if (!super.matches(craftingContainer, level)) {
			return false;
		}
		for (int i = 0; i < craftingContainer.size(); i++) {
			var item = craftingContainer.getItem(i);
			if (item.is(Items.POTION) && !item.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER.serializer;
	}

	private static class Serializer extends WrappingRecipeSerializer<WaterBottleMatchingRecipe> {
		public static final MapCodec<WaterBottleMatchingRecipe> CODEC = ShapedRecipe.MAP_CODEC
				.xmap(WaterBottleMatchingRecipe::new, Function.identity());
		public static final StreamCodec<RegistryFriendlyByteBuf, WaterBottleMatchingRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC
				.map(WaterBottleMatchingRecipe::new, Function.identity());

		Serializer() {
			super(CODEC, STREAM_CODEC);
		}

		@Override
		public WaterBottleMatchingRecipe wrap(Recipe<?> recipe) {
			if (!(recipe instanceof ShapedRecipe shapedRecipe)) {
				throw new IllegalArgumentException("Unsupported recipe type to wrap: " + recipe.getType());
			}
			return new WaterBottleMatchingRecipe(shapedRecipe);
		}
	}
}

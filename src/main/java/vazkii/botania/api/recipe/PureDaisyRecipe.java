/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface PureDaisyRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID = botaniaRL("pure_daisy");

	/**
	 * This gets called every tick, please be careful with your checks.
	 */
	boolean matches(Level world, BlockPos pos, BlockState state);

	/**
	 * Returns the input block state definition.
	 *
	 */
	StateIngredient getInput();

	/**
	 * Returns the output block state definition. If it matches multiple block states,
	 * one of those it picked at random with equal weights when the conversion takes place.
	 */
	StateIngredient getOutput();

	/**
	 * Returns whether any relevant block state properties of the matched block will be copied over to the
	 * converted block as the conversion takes place. (Used to e.g. keep the rotation of converted logs.)
	 */
	boolean isCopyInputProperties();

	/**
	 * Returns the optional mcfunction to execute when the conversion takes place.
	 * (Might not be available on client-side mirrors of the recipe definition.)
	 */
	Optional<CacheableFunction> getSuccessFunction();

	/**
	 * Returns the number of times a source block must be ticked by the flower before it converts.
	 * Note that the Pure Daisy ticks its surrounding blocks in a round-robin way, one at a time.
	 */
	int getTime();

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends PureDaisyRecipe> getType() {
		return (RecipeType<? extends PureDaisyRecipe>) (RecipeType<?>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	/**
	 * @deprecated Not meant to be used for item crafting in a container.
	 */
	@Override
	@Deprecated
	default boolean matches(RecipeInput container, Level level) {
		return false;
	}

	/**
	 * @deprecated Not meant to be used for item crafting in a container.
	 */
	@Override
	@Deprecated
	default ItemStack assemble(RecipeInput container) {
		return ItemStack.EMPTY;
	}

	/**
	 * @deprecated Not meant to be used for item crafting in a container.
	 */
	@Deprecated
	default ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean isSpecial() {
		return true;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}
}

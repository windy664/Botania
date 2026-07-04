/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

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

import vazkii.botania.api.brew.Brew;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface BotanicalBreweryRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID = botaniaRL("brew");

	Brew getBrew();

	int getManaUsage();

	ItemStack getOutput(ItemStack container);

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends BotanicalBreweryRecipe> getType() {
		return (RecipeType<? extends BotanicalBreweryRecipe>) (RecipeType<?>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	default ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	default ItemStack assemble(RecipeInput inv) {
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

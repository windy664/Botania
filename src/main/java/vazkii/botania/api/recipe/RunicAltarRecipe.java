/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface RunicAltarRecipe extends RecipeWithReagent {
	ResourceLocation TYPE_ID = botaniaRL("runic_altar");
	ResourceLocation HEAD_TYPE_ID = botaniaRL("runic_altar_head");

	int getMana();

	NonNullList<Ingredient> getCatalysts();

	@Override
	NonNullList<ItemStack> getRemainingItems(RecipeInput container);

	@Override
	default RecipeType<?> getType() {
		return Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.get(TYPE_ID));
	}
}

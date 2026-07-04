/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface PetalApothecaryRecipe extends RecipeWithReagent {
	Identifier TYPE_ID = botaniaRL("petal_apothecary");

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends PetalApothecaryRecipe> getType() {
		return (RecipeType<? extends PetalApothecaryRecipe>) (RecipeType<?>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}
}

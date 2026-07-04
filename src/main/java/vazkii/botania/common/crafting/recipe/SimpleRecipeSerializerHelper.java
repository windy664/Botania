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
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/**
 * Replacement for the removed {@code SimpleCraftingRecipeSerializer}: builds a {@link RecipeSerializer} for a
 * stateless {@link CustomRecipe}. 26.2 dropped the {@code CraftingBookCategory} constructor argument from
 * {@code CustomRecipe}, so these recipes now carry no serialized data at all.
 */
public final class SimpleRecipeSerializerHelper {
	private SimpleRecipeSerializerHelper() {}

	public static <T extends CustomRecipe> RecipeSerializer<T> of(Supplier<T> factory) {
		MapCodec<T> codec = MapCodec.unit(factory);
		StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.unit(factory.get());
		return new RecipeSerializer<>(codec, streamCodec);
	}
}

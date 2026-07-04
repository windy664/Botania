/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Holds a {@link RecipeSerializer} (26.2 made that class final, so we can no longer subclass it) plus the ability to
 * wrap a plain recipe into a Botania variant at datagen time.
 */
public abstract class WrappingRecipeSerializer<T extends Recipe<?>> {
	public final RecipeSerializer<T> serializer;

	protected WrappingRecipeSerializer(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		this.serializer = new RecipeSerializer<>(codec, streamCodec);
	}

	public abstract T wrap(Recipe<?> recipe);
}

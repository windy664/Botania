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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/**
 * Replacement for the removed {@code SimpleCraftingRecipeSerializer}: builds a {@link RecipeSerializer} for a
 * {@link CustomRecipe} that carries nothing but its {@link CraftingBookCategory}.
 */
public final class SimpleRecipeSerializerHelper {
	private SimpleRecipeSerializerHelper() {}

	public static <T extends CustomRecipe> RecipeSerializer<T> of(Function<CraftingBookCategory, T> factory) {
		MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CustomRecipe::category)
		).apply(instance, factory));
		StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
				CraftingBookCategory.STREAM_CODEC, CustomRecipe::category,
				factory::apply
		);
		return new RecipeSerializer<>(codec, streamCodec);
	}
}

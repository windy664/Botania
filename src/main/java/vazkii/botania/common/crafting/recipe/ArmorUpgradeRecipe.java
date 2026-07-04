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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import vazkii.botania.mixin.ShapedRecipeAccessor;

import java.util.function.Function;

public class ArmorUpgradeRecipe extends ShapedRecipe {
	public static final WrappingRecipeSerializer<ArmorUpgradeRecipe> SERIALIZER = new Serializer();

	private ArmorUpgradeRecipe(ShapedRecipe recipe) {
		super(new Recipe.CommonInfo(recipe.showNotification()),
				new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
				((ShapedRecipeAccessor) recipe).botania_getPattern(),
				((ShapedRecipeAccessor) recipe).botania_getResult());
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack out = super.assemble(inv);
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
			if (equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				// TODO: verify this works as intended
				out.applyComponents(stack.getComponentsPatch());
				break;
			}
		}
		return out;
	}

	@Override
	public RecipeSerializer<ArmorUpgradeRecipe> getSerializer() {
		return SERIALIZER.serializer;
	}

	private static class Serializer extends WrappingRecipeSerializer<ArmorUpgradeRecipe> {
		public static final MapCodec<ArmorUpgradeRecipe> CODEC = ShapedRecipe.MAP_CODEC
				.xmap(ArmorUpgradeRecipe::new, Function.identity());
		public static final StreamCodec<RegistryFriendlyByteBuf, ArmorUpgradeRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC
				.map(ArmorUpgradeRecipe::new, Function.identity());

		Serializer() {
			super(CODEC, STREAM_CODEC);
		}

		@Override
		public ArmorUpgradeRecipe wrap(Recipe<?> recipe) {
			if (!(recipe instanceof ShapedRecipe shapedRecipe)) {
				throw new IllegalArgumentException("Unsupported recipe type to wrap: " + recipe.getType());
			}
			return new ArmorUpgradeRecipe(shapedRecipe);
		}
	}
}

/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelPickaxeItem;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;

public class VitreousPickaxeItem extends ManasteelPickaxeItem implements
		vazkii.botania.api.item.SpecialBlockBreakingHandler {
	private static final int MANA_PER_DAMAGE = 160;
	private static final float SPEED = 3.9f;
	private static final ToolMaterial MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_WOODEN_TOOL, 125, SPEED, 0, 10, BotaniaTags.Items.MANASTEEL_TOOL_REPAIR);

	// 26.2: ToolMaterial no longer builds the Tool component, so set the custom mining rules directly on the properties.
	private static final Tool VITREOUS_TOOL = new Tool(
			List.of(
					// always correct tool for silktouched blocks, relevant e.g. for copper bulb
					Tool.Rule.minesAndDrops(BotaniaTags.Blocks.VITREOUS_PICKAXE_SILKTOUCHED, SPEED),
					Tool.Rule.deniesDrops(BlockTags.INCORRECT_FOR_WOODEN_TOOL),
					Tool.Rule.minesAndDrops(BotaniaTags.Blocks.MINEABLE_WITH_VITREOUS_PICKAXE, SPEED)
			),
			1.0F, 1);

	public VitreousPickaxeItem(Properties props) {
		super(MATERIAL, props.component(DataComponents.TOOL, VITREOUS_TOOL), -1);
	}

	/*
	* No way to modify the loot context so we're gonna go braindead hack workaround here:
	* - When block starting to break, if the tool doesn't have silktouch already, add it and add a "temp silk touch" flag
	* - Every tick, if the "temp silk touch" flag is present, remove it and remove any silk touch enchants from the stack
	*/
	@Override
	public void onBlockStartBreak(ServerLevel level, ItemStack itemstack, BlockPos pos, Player player) {
		BlockState state = level.getBlockState(pos);
		HolderLookup<Enchantment> enchantmentLookup = level.holderLookup(Registries.ENCHANTMENT);
		boolean hasSilk = EnchantmentHelper.getItemEnchantmentLevel(enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH), itemstack) > 0;
		if (hasSilk || !isGlass(state)) {
			return;
		}

		itemstack.enchant(enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH), 1);
		itemstack.set(BotaniaDataComponents.SILK_HACK, Unit.INSTANCE);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity player, EquipmentSlot slot) {
		super.inventoryTick(stack, level, player, slot);

		if (stack.has(BotaniaDataComponents.SILK_HACK)) {
			stack.remove(BotaniaDataComponents.SILK_HACK);
			EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.removeIf(
					enchantmentHolder -> enchantmentHolder.is(Enchantments.SILK_TOUCH)));
		}
	}

	private boolean isGlass(BlockState state) {
		return state.is(BotaniaTags.Blocks.VITREOUS_PICKAXE_SILKTOUCHED);
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		return isGlass(state) ? Integer.MAX_VALUE : 0;
	}

}

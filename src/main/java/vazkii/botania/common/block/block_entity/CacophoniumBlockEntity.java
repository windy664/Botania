/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import vazkii.botania.common.item.CacophoniumItem;

public class CacophoniumBlockEntity extends BotaniaBlockEntity {
	private static final String TAG_STACK = "stack";

	public ItemStack stack = ItemStack.EMPTY;

	public CacophoniumBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CACOPHONIUM, pos, state);
	}

	public void annoyDirewolf() {
		CacophoniumItem.playSound(level, stack, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundSource.BLOCKS, 1F);
		if (!level.isClientSide()) {
			float noteColor = level.getRandom().nextInt(25) / 24.0F;
			((ServerLevel) level).sendParticles(ParticleTypes.NOTE, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 0, noteColor, 0, 0, 1);
			level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, getBlockPos());
		}
	}

	@Override
	public void writePacketNBT(ValueOutput cmp) {
		super.writePacketNBT(cmp);

		if (!stack.isEmpty()) {
			cmp.store(TAG_STACK, ItemStack.CODEC, stack);
		}
	}

	@Override
	public void readPacketNBT(ValueInput cmp) {
		super.readPacketNBT(cmp);

		stack = cmp.read(TAG_STACK, ItemStack.CODEC).orElse(ItemStack.EMPTY);
	}

}

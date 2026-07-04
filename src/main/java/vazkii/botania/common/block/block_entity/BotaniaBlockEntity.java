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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

public class BotaniaBlockEntity extends BlockEntity {
	public BotaniaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		writePacketNBT(output);
	}

	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		writePacketNBT(output);
		return output.buildResult();
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		readPacketNBT(input);
	}

	public void writePacketNBT(ValueOutput cmp) {}

	public void readPacketNBT(ValueInput cmp) {}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}

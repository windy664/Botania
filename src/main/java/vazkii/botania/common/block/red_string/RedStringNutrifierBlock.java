/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.red_string;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.red_string.RedStringBlockEntity;
import vazkii.botania.common.block.block_entity.red_string.RedStringNutrifierBlockEntity;

public class RedStringNutrifierBlock extends RedStringBlock implements BonemealableBlock {

	public RedStringNutrifierBlock(BlockBehaviour.Properties builder) {
		super(builder);
		registerDefaultState(defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return ((RedStringNutrifierBlockEntity) world.getBlockEntity(pos)).canGrow(world);
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource rand, BlockPos pos, BlockState state) {
		return ((RedStringNutrifierBlockEntity) world.getBlockEntity(pos)).canUseBonemeal(world, rand);
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state) {
		((RedStringNutrifierBlockEntity) world.getBlockEntity(pos)).grow(world, rand);
	}

	@Override
	public RedStringBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RedStringNutrifierBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, BotaniaBlockEntities.RED_STRING_FERTILIZER, RedStringNutrifierBlockEntity::commonTick);
	}
}

package com.auroali.multiblocks.commands;

import com.auroali.multiblocks.Multiblock;
import com.auroali.multiblocks.MultiblockArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockRotationArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MultiblockCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("multiblocks")
                .requires(ctx -> ctx.hasPermissionLevel(2))
                .then(CommandManager.literal("place")
                        .then(CommandManager.argument("multiblock", MultiblockArgumentType.multiblock())
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .then(CommandManager.argument("rotation", BlockRotationArgumentType.blockRotation())
                                                .executes(ctx -> placeMultiblock(
                                                        ctx,
                                                        MultiblockArgumentType.getArgument(ctx, "multiblock"),
                                                        BlockPosArgumentType.getBlockPos(ctx, "pos"),
                                                        BlockRotationArgumentType.getBlockRotation(ctx, "rotation")
                                                ))
                                        )
                                        .executes(ctx -> placeMultiblock(
                                                ctx,
                                                MultiblockArgumentType.getArgument(ctx, "multiblock"),
                                                BlockPosArgumentType.getBlockPos(ctx, "pos"),
                                                BlockRotation.NONE
                                        ))
                                )
                                .executes(ctx -> placeMultiblock(
                                        ctx,
                                        MultiblockArgumentType.getArgument(ctx, "multiblock"),
                                        BlockPos.ofFloored(ctx.getSource().getPosition()),
                                        BlockRotation.NONE
                                )
                        )
                ))
                .then(CommandManager.literal("test")
                        .then(CommandManager.argument("multiblock", MultiblockArgumentType.multiblock())
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .then(CommandManager.argument("rotation", BlockRotationArgumentType.blockRotation())
                                                .executes(ctx -> testMultiblock(
                                                        ctx,
                                                        MultiblockArgumentType.getArgument(ctx, "multiblock"),
                                                        BlockPosArgumentType.getBlockPos(ctx, "pos"),
                                                        BlockRotationArgumentType.getBlockRotation(ctx, "rotation")
                                                ))
                                        )
                                        .executes(ctx -> testMultiblock(
                                                ctx,
                                                MultiblockArgumentType.getArgument(ctx, "multiblock"),
                                                BlockPosArgumentType.getBlockPos(ctx, "pos"),
                                                BlockRotation.NONE
                                        )
                                )
                        )
                ));
    }

    public static int testMultiblock(CommandContext<ServerCommandSource> ctx, Multiblock multiblock, BlockPos pos, BlockRotation rotation) {
        World world = ctx.getSource().getWorld();
        if(multiblock.matches(world, pos, rotation)) {
            ctx.getSource().sendFeedback(() -> Text.translatable("chat.multiblocks.matches"), true);
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.translatable("chat.multiblocks.doesnt_match"), true);
        return 0;
    }

    public static int placeMultiblock(CommandContext<ServerCommandSource> ctx, Multiblock multiblock, BlockPos pos, BlockRotation rotation) {
        World world = ctx.getSource().getWorld();
        multiblock.forEach(entry -> {
            BlockPos newPos = entry.offset().add(multiblock.getOffset()).add(pos);
            BlockState toPlace = entry.value()
                    .mapLeft(tag -> {
                        var blocks = Registries.BLOCK.getEntryList(tag);
                        return blocks
                                .flatMap(list -> list.getRandom(world.getRandom()))
                                .map(block -> block.value().getDefaultState().rotate(rotation))
                                .orElse(Blocks.AIR.getDefaultState());
                    }).map(state -> state, state -> state);

            world.setBlockState(newPos, toPlace, Block.NOTIFY_ALL);
        }, rotation);
        return 0;
    }
}

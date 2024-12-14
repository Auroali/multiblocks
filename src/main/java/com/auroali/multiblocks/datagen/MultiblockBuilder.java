package com.auroali.multiblocks.datagen;

import com.auroali.multiblocks.Multiblock;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class MultiblockBuilder {
    final Identifier id;
    final Set<Multiblock.Entry> structure = new HashSet<>();
    BlockPos offset = BlockPos.ORIGIN;

    protected MultiblockBuilder(Identifier id) {
        this.id = id;
    }

    public static MultiblockBuilder create(Identifier id) {
        return new MultiblockBuilder(id);
    }

    public MultiblockBuilder add(BlockPos pos, BlockState state) {
        this.structure.add(new Multiblock.Entry(pos, Either.right(state)));
        return this;
    }

    public MultiblockBuilder add(BlockPos pos, TagKey<Block> tag) {
        this.structure.add(new Multiblock.Entry(pos, Either.left(tag)));
        return this;
    }

    public MultiblockBuilder add(BlockPos pos, Block block) {
        return this.add(pos, block.getDefaultState());
    }

    public MultiblockBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    public void validate() {
        if(this.offset == null)
            throw new IllegalStateException("Offset cannot be null");
        if(this.structure.isEmpty())
            throw new IllegalStateException("Multiblock cannot be empty");
        if(this.id == null)
            throw new IllegalStateException("Multiblock ID cannot be null");
    }

    public void offerTo(MultiblockDataProvider.MultiblockConsumer provider) {
        this.validate();
        provider.accept(this.id, new Multiblock(this.structure, this.offset));
    }
}

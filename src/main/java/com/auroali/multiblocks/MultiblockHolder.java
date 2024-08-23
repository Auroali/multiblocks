package com.auroali.multiblocks;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Optional;

public class MultiblockHolder {
    protected static final HashMap<Identifier, MultiblockHolder> holders = new HashMap<>();

    Multiblock multiblock;

    protected MultiblockHolder() {}
    protected MultiblockHolder(Multiblock multiblock) {
        this.multiblock = multiblock;
    }


    public static MultiblockHolder getOrCreate(Identifier id) {
        return holders.computeIfAbsent(id, key ->
                MultiblockRegistry.REGISTERED_MULTIBLOCKS.containsKey(key)
                ? create(MultiblockRegistry.REGISTERED_MULTIBLOCKS.get(key))
                : createEmpty()
        );
    }

    public Optional<Multiblock> get() {
        return multiblock == null ? Optional.empty() : Optional.of(multiblock);
    }

    public boolean matches(World world, BlockPos pos, BlockRotation rotation) {
        return multiblock != null && multiblock.matches(world, pos, rotation);
    }

    public boolean matches(World world, BlockPos pos) {
        return multiblock != null && multiblock.matches(world, pos);
    }

    protected static MultiblockHolder createEmpty() {
        return new MultiblockHolder();
    }

    protected static MultiblockHolder create(Multiblock multiblock) {
        return new MultiblockHolder(multiblock);
    }
}

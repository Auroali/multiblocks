package com.auroali.multiblocks;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MultiblockRegistry {
    protected static final Map<Identifier, Multiblock> REGISTERED_MULTIBLOCKS = new HashMap<>();

    protected static void register(Identifier identifier, Multiblock multiblock) {
        REGISTERED_MULTIBLOCKS.put(identifier, multiblock);
    }
}

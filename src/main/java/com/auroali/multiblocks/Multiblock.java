package com.auroali.factions.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Multiblock {
    final List<MultiblockEntry> structure;

    private Multiblock(List<MultiblockEntry> entries) {
        this.structure = entries;
    }

    public boolean matches(World world, BlockPos pos) {
        for(MultiblockEntry entry : structure) {
            if(world.getBlockState(entry.pos.add(pos)) != entry.state) {
                return false;
            }
        }
        return true;
    }

    public static Multiblock compile(List<List<String>> pattern, Map<Character, BlockState> keys, BlockPos offset) {
        List<MultiblockEntry> entries = new ArrayList<>();
        for(int y = 0; y < pattern.size(); y++) {
            for(int z = 0; z < pattern.get(y).size(); z++) {
                String chars = pattern.get(y).get(z);
                for(int x = 0; x < chars.length(); x++) {
                    char k = chars.charAt(x);
                    if(k == ' ') continue;
                    BlockState state = keys.get(k);
                    BlockPos pos = new BlockPos(x + offset.getX(), y + offset.getY(), z + offset.getZ());
                    entries.add(new MultiblockEntry(pos, state));
                }
            }
        }
        return new Multiblock(entries);
    }

    public record MultiblockEntry(BlockPos pos, BlockState state) {}
}

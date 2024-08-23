package com.auroali.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.BlockRotation;
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
            if(!entry.value().matches(world.getBlockState(entry.pos.add(pos)))) {
                return false;
            }
        }
        return true;
    }
    public boolean matches(World world, BlockPos pos, BlockRotation rotation) {
        for(MultiblockEntry entry : structure) {
            if(!entry.value().matches(world.getBlockState(entry.pos.add(pos).rotate(rotation)).rotate(rotation), rotation)) {
                return false;
            }
        }
        return true;
    }

    public static Multiblock compile(List<List<String>> pattern, Map<Character, MultiblockKey> keys, BlockPos offset) {
        List<MultiblockEntry> entries = new ArrayList<>();
        for(int y = 0; y < pattern.size(); y++) {
            for(int z = 0; z < pattern.get(y).size(); z++) {
                String chars = pattern.get(y).get(z);
                for(int x = 0; x < chars.length(); x++) {
                    char k = chars.charAt(x);
                    if(k == ' ') continue;
                    MultiblockKey val = keys.get(k);
                    BlockPos pos = new BlockPos(x + offset.getX(), y + offset.getY(), z + offset.getZ());
                    entries.add(new MultiblockEntry(pos, val));
                }
            }
        }
        return new Multiblock(entries);
    }

    public record MultiblockEntry(BlockPos pos, MultiblockKey value) {}
    public static class MultiblockKey {
        BlockState state;
        TagKey<Block> tag;
        boolean isTag;
        public static MultiblockKey of(TagKey<Block> tag) {
            MultiblockKey v = new MultiblockKey();
            v.tag = tag;
            v.isTag = true;
            return v;
        }
        public static MultiblockKey of(BlockState state) {
            MultiblockKey v = new MultiblockKey();
            v.state = state;
            v.isTag = false;
            return v;
        }
        public boolean matches(BlockState state) {
            return isTag ? state.isIn(tag) : state == this.state;
        }
        public boolean matches(BlockState state, BlockRotation rotation) {
            return isTag ? state.isIn(tag) : state == this.state.rotate(rotation);
        }
    }
}

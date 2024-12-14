package com.auroali.multiblocks;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class Multiblock implements Iterable<Multiblock.Entry> {
    public static final Codec<Multiblock> CODEC = PalettedMultiblock.CODEC.xmap(PalettedMultiblock::toMultiblock, Multiblock::toPalettedMultiblock);
    final Set<Entry> structure;
    final BlockPos offset;

    public Multiblock(Set<Entry> entries, BlockPos offset) {
        this.structure = entries;
        this.offset = offset;
    }

    public boolean matches(WorldView world, BlockPos pos, BlockRotation rotation) {
        for(Entry entry : this.structure) {
            BlockState state = world.getBlockState(
                    entry.offset()
                            .rotate(rotation)
                            .add(this.offset)
                            .add(pos)
            );
            boolean matches = entry.value().map(state::isIn, testState -> testState.rotate(rotation) == state);
            if(!matches)
                return false;
        }
        return true;
    }

    public boolean matches(WorldView world, BlockPos pos) {
        return this.matches(world, pos, BlockRotation.NONE);
    }

    protected PalettedMultiblock toPalettedMultiblock() {
        List<Either<TagKey<Block>, BlockState>> palette = new ArrayList<>();
        Object2IntArrayMap<BlockPos> values = new Object2IntArrayMap<>();
        for(Entry entry : this.structure) {
            int paletteIndex = palette.indexOf(entry.value());
            if(paletteIndex == -1) {
                paletteIndex = palette.size();
                palette.add(entry.value());
            }

            values.put(entry.offset(), paletteIndex);
        }
        return new PalettedMultiblock(palette, values, this.offset);
    }

    @NotNull
    @Override
    public Iterator<Entry> iterator() {
        return this.structure.iterator();
    }

    @Override
    public void forEach(Consumer<? super Entry> action) {
        this.structure.forEach(action);
    }

    public void forEach(Consumer<? super Entry> action, BlockRotation rotation) {
        this.structure.forEach(entry -> {
            Entry rotated = new Entry(entry.offset().rotate(rotation), entry.value().mapRight(state -> state.rotate(rotation)));
            action.accept(rotated);
        });
    }

    @Override
    public Spliterator<Entry> spliterator() {
        return this.structure.spliterator();
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public record Entry(BlockPos offset, Either<TagKey<Block>, BlockState> value) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("offset").forGetter(Entry::offset),
                Codec.either(TagKey.codec(RegistryKeys.BLOCK), BlockState.CODEC).fieldOf("value").forGetter(Entry::value)
        ).apply(instance, Entry::new));

        @Override
        public int hashCode() {
            return offset.hashCode();
        }
    }
    protected record PalettedMultiblock(List<Either<TagKey<Block>, BlockState>> palette, Object2IntArrayMap<BlockPos> values, BlockPos offset) {
        public static final Codec<PalettedMultiblock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(TagKey.codec(RegistryKeys.BLOCK), BlockState.CODEC).listOf().fieldOf("palette").forGetter(PalettedMultiblock::palette),
                Codec.mapPair(BlockPos.CODEC.fieldOf("pos"), Codec.INT.fieldOf("index"))
                        .codec()
                        .listOf()
                        .xmap(pairs -> {
                            Object2IntArrayMap<BlockPos> map = new Object2IntArrayMap<>();
                            pairs.forEach(pair -> map.put(pair.getFirst(), pair.getSecond().intValue()));
                            return map;
                        }, map -> {
                            List<Pair<BlockPos, Integer>> pairs = new ArrayList<>(map.size());
                            map.forEach((key, value) -> pairs.add(new Pair<>(key, value)));
                            return pairs;
                        })
                        .fieldOf("entries")
                        .forGetter(PalettedMultiblock::values),
                BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ORIGIN).forGetter(PalettedMultiblock::offset)
        ).apply(instance, PalettedMultiblock::new));
        public Multiblock toMultiblock() {
            Set<Entry> structure = new HashSet<>();
            values.forEach((pos, index) -> {
                Entry entry = new Entry(pos, palette().get(index));
                structure.add(entry);
            });
            return new Multiblock(structure, this.offset());
        }
    }
}

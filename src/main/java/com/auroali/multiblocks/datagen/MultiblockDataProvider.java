package com.auroali.multiblocks.datagen;

import com.auroali.multiblocks.Multiblock;
import com.auroali.multiblocks.Multiblocks;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

public abstract class MultiblockDataProvider extends FabricCodecDataProvider<Multiblock> {
    protected MultiblockDataProvider(FabricDataOutput dataOutput) {
        super(dataOutput, DataOutput.OutputType.DATA_PACK, Multiblocks.MULTIBLOCK_PATH, Multiblock.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, Multiblock> provider) {
        this.generate(provider::accept);
    }

    protected abstract void generate(MultiblockConsumer consumer);

    @Override
    public String getName() {
        return "Multiblocks";
    }

    @FunctionalInterface
    public interface MultiblockConsumer {
        void accept(Identifier id, Multiblock mb);
    }
}

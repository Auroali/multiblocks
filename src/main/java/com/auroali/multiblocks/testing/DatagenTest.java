package com.auroali.multiblocks.testing;

import com.auroali.multiblocks.Multiblocks;
import com.auroali.multiblocks.datagen.MultiblockBuilder;
import com.auroali.multiblocks.datagen.MultiblockDataProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DatagenTest implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(MultiblockDatagenTest::new);
    }

    public static class MultiblockDatagenTest extends MultiblockDataProvider {

        protected MultiblockDatagenTest(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        protected void generate(MultiblockConsumer consumer) {
            MultiblockBuilder.create(new Identifier(Multiblocks.MODID, "test_structure"))
                    .add(new BlockPos(0, 1, 0), BlockTags.BASE_STONE_OVERWORLD)
                    .add(new BlockPos(0, 0, 0), Blocks.AMETHYST_BLOCK)
                    .add(new BlockPos(1, 0, 1), Blocks.DARK_OAK_BUTTON.getDefaultState().with(Properties.WALL_MOUNT_LOCATION, WallMountLocation.FLOOR))
                    .add(new BlockPos(1, 0, -1), Blocks.DARK_OAK_BUTTON.getDefaultState().with(Properties.WALL_MOUNT_LOCATION, WallMountLocation.FLOOR))
                    .add(new BlockPos(-1, 0, -1), Blocks.DARK_OAK_BUTTON.getDefaultState().with(Properties.WALL_MOUNT_LOCATION, WallMountLocation.FLOOR))
                    .add(new BlockPos(-1, 0, 1), Blocks.DARK_OAK_BUTTON.getDefaultState().with(Properties.WALL_MOUNT_LOCATION, WallMountLocation.FLOOR))
                    .custom("amethyst", new BlockPos(0, 0, 0))
                    .offerTo(consumer);
        }
    }
}

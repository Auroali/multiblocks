package com.auroali.multiblocks;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class MultiblockReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Identifier ID = new Identifier(Multiblocks.MOD_ID, "multiblock_loader");
    public MultiblockReloadListener(Gson gson, String dataType) {
        super(gson, dataType);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        profiler.push("multiblocks");
        MultiblockRegistry.REGISTERED_MULTIBLOCKS.clear();
        prepared.forEach((id, element) -> {
            long startTime = System.nanoTime();
            profiler.push("deserialize multiblock");
            Multiblock.CODEC.parse(JsonOps.INSTANCE, element)
                    .resultOrPartial(Multiblocks.LOGGER::error)
                    .ifPresent(mb -> MultiblockRegistry.REGISTERED_MULTIBLOCKS.put(id, mb));
            profiler.pop();
            long delta = System.nanoTime() - startTime;
            Multiblocks.LOGGER.info("Multiblocks took {}ms to read", TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS));
        });
        MultiblockHolder.holders.forEach((identifier, multiblockHolder) ->
            multiblockHolder.multiblock = MultiblockRegistry.REGISTERED_MULTIBLOCKS.get(identifier)
        );
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}

package com.auroali.multiblocks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MultiblockReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Identifier ID = new Identifier(Multiblocks.MODID, "multiblock_loader");
    public MultiblockReloadListener(Gson gson, String dataType) {
        super(gson, dataType);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        profiler.push("multiblocks");
        MultiblockRegistry.REGISTERED_MULTIBLOCKS.clear();
        prepared.forEach((id, element) -> {
            profiler.push("deserialize multiblock");
            Multiblock.CODEC.parse(JsonOps.INSTANCE, element)
                    .resultOrPartial(Multiblocks.LOGGER::error)
                    .ifPresent(mb -> MultiblockRegistry.REGISTERED_MULTIBLOCKS.put(id, mb));
            profiler.pop();
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

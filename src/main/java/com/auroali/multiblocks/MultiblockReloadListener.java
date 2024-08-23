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
            JsonObject multiblock = element.getAsJsonObject();
            BlockPos offset = BlockPos.CODEC.decode(JsonOps.INSTANCE, multiblock.get("offset"))
                    .resultOrPartial(s -> Multiblocks.LOGGER.warn("Failed to parse offset for {}: {}", id, s))
                    .map(Pair::getFirst)
                    .orElse(BlockPos.ORIGIN);
            JsonObject keys = multiblock.getAsJsonObject("keys");
            HashMap<Character, Multiblock.MultiblockKey> palette = new HashMap<>();
            profiler.push("palette");
            for(Iterator<String> it = keys.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if(key.length() != 1) {
                    Multiblocks.LOGGER.error("Failed to parse multiblock {}: Expected single character key, got {}", id, key);
                    return;
                }
                String value = keys.get(key).getAsString();
                Multiblock.MultiblockKey multiblockKey;
                if(value.startsWith("#")) {
                    Identifier tagId = Identifier.tryParse(value.substring(1));
                    if(tagId == null) {
                        Multiblocks.LOGGER.error("Failed to parse multiblock {}: Invalid Tag '{}'", id, value);
                    }
                    multiblockKey = Multiblock.MultiblockKey.of(TagKey.of(RegistryKeys.BLOCK, tagId));
                } else {
                    try {
                        multiblockKey = Multiblock.MultiblockKey.of(BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), value, false).blockState());
                    } catch (CommandSyntaxException e) {
                        Multiblocks.LOGGER.error("Failed to parse multiblock {}: Failed to parse blockstate {}", id, value);
                        return;
                    }
                }
                palette.put(key.charAt(0), multiblockKey);
            }
            profiler.swap("blocks");
            List<List<String>> multiblockLayers = new ArrayList<>();
            JsonArray layers = multiblock.get("blocks").getAsJsonArray();
            for(int i = 0; i < layers.size(); i++) {
                JsonArray layer = layers.get(i).getAsJsonArray();
                List<String> multiblockLayer = new ArrayList<>();
                multiblockLayers.add(multiblockLayer);
                for(int j = 0; j < layer.size(); j++) {
                    multiblockLayer.add(layer.get(j).getAsString());
                }
            }
            profiler.swap("compiling");
            profiler.pop();
            MultiblockRegistry.REGISTERED_MULTIBLOCKS.put(id, Multiblock.compile(multiblockLayers, palette, offset));
            profiler.pop();
        });
        MultiblockHolder.holders.forEach((identifier, multiblockHolder) -> {
            multiblockHolder.multiblock = MultiblockRegistry.REGISTERED_MULTIBLOCKS.get(identifier);
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}

package com.auroali.multiblocks;

import com.auroali.multiblocks.commands.MultiblockCommand;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Multiblocks implements ModInitializer {
	public static final String MOD_ID = "multiblocks";

	public static String MULTIBLOCK_PATH = "multiblocks";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new MultiblockReloadListener(new Gson(), MULTIBLOCK_PATH));

		ArgumentTypeRegistry.registerArgumentType(new Identifier("multiblocks", "multiblock_argument"), MultiblockArgumentType.class, ConstantArgumentSerializer.of(MultiblockArgumentType::multiblock));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(MultiblockCommand.register());
		});
	}
}
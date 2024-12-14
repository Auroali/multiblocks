package com.auroali.multiblocks;

import com.auroali.multiblocks.Multiblock;
import com.auroali.multiblocks.MultiblockHolder;
import com.auroali.multiblocks.MultiblockRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class MultiblockArgumentType implements ArgumentType<Multiblock> {
    public static MultiblockArgumentType multiblock() {
        return new MultiblockArgumentType();
    }

    public static Multiblock getArgument(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Multiblock.class);
    }

    @Override
    public Multiblock parse(StringReader reader) throws CommandSyntaxException {
        Identifier id = Identifier.fromCommandInput(reader);
        return MultiblockRegistry.REGISTERED_MULTIBLOCKS.get(id);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        MultiblockRegistry.REGISTERED_MULTIBLOCKS.keySet().forEach(id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }
}

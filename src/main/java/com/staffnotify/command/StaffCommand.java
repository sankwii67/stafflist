package com.staffnotify.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.staffnotify.config.StaffNotifyConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.nio.file.Paths;

public class StaffCommand {

    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYERS = (ctx, builder) -> {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            StaffNotifyConfig cfg = StaffNotifyConfig.getInstance();
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                String name = entry.getProfile().getName();
                if (!cfg.isStaff(name) && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(name);
                }
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> STAFF_PLAYERS = (ctx, builder) -> {
        for (String name : StaffNotifyConfig.getInstance().staffList) {
            if (name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> EXISTING_CONFIGS = (ctx, builder) -> {
        for (String name : StaffNotifyConfig.listConfigs()) {
            if (name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("staff")
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("nick", StringArgumentType.word())
                            .suggests(ONLINE_PLAYERS)
                            .executes(ctx -> {
                                String nick = StringArgumentType.getString(ctx, "nick");
                                boolean added = StaffNotifyConfig.getInstance().addStaff(nick);
                                String key = added ? "staffnotify.command.add.success" : "staffnotify.command.add.already";
                                ctx.getSource().sendFeedback(Text.translatable(key, nick));
                                return 1;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("nick", StringArgumentType.word())
                            .suggests(STAFF_PLAYERS)
                            .executes(ctx -> {
                                String nick = StringArgumentType.getString(ctx, "nick");
                                boolean removed = StaffNotifyConfig.getInstance().removeStaff(nick);
                                String key = removed ? "staffnotify.command.remove.success" : "staffnotify.command.remove.notfound";
                                ctx.getSource().sendFeedback(Text.translatable(key, nick));
                                return 1;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("list")
                        .executes(ctx -> {
                            var list = StaffNotifyConfig.getInstance().staffList;
                            if (list.isEmpty()) {
                                ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.list.empty"));
                            } else {
                                String header = Text.translatable("staffnotify.command.list.header", list.size()).getString();
                                ctx.getSource().sendFeedback(Text.literal("§6" + header + "§a" + String.join("§r, §a", list)));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("config")
                        .then(ClientCommandManager.literal("save")
                            .executes(ctx -> {
                                StaffNotifyConfig.save();
                                ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.config.saved"));
                                return 1;
                            })
                            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    StaffNotifyConfig.saveByName(name);
                                    ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.config.saved.name", name));
                                    return 1;
                                })
                            )
                        )
                        .then(ClientCommandManager.literal("load")
                            .executes(ctx -> {
                                StaffNotifyConfig.load();
                                ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.config.loaded"));
                                return 1;
                            })
                            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .suggests(EXISTING_CONFIGS)
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    StaffNotifyConfig.loadByName(name);
                                    ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.config.loaded.name", name));
                                    return 1;
                                })
                            )
                        )
                        .then(ClientCommandManager.literal("dir")
                            .then(ClientCommandManager.literal("open")
                                .executes(ctx -> {
                                    try {
                                        java.nio.file.Path dir = StaffNotifyConfig.getConfigDir();
                                        java.nio.file.Files.createDirectories(dir);
                                        new ProcessBuilder("explorer.exe", dir.toAbsolutePath().toString())
                                            .start();
                                        ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.config.dir.opened"));
                                    } catch (Exception e) {
                                        ctx.getSource().sendFeedback(Text.translatable("staffnotify.command.config.dir.open.failed"));
                                    }
                                    return 1;
                                })
                            )
                            .then(ClientCommandManager.literal("path")
                                .executes(ctx -> {
                                    ctx.getSource().sendFeedback(Text.translatable(
                                        "staffnotify.command.config.dir.show",
                                        StaffNotifyConfig.getConfigDir().toAbsolutePath().toString()));
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        String pathStr = StringArgumentType.getString(ctx, "path");
                                        try {
                                            StaffNotifyConfig.setConfigDir(Paths.get(pathStr));
                                            StaffNotifyConfig.load();
                                            ctx.getSource().sendFeedback(Text.translatable(
                                                "staffnotify.command.config.dir.changed", pathStr));
                                        } catch (Exception e) {
                                            ctx.getSource().sendFeedback(Text.translatable(
                                                "staffnotify.command.config.dir.invalid", pathStr));
                                        }
                                        return 1;
                                    })
                                )
                            )
                        )
                    )
            )
        );
    }
}

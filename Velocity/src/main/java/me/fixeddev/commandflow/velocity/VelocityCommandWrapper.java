package me.fixeddev.commandflow.velocity;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.Namespace;
import me.fixeddev.commandflow.NamespaceImpl;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.exception.CommandException;
import me.fixeddev.commandflow.translator.Translator;
import net.kyori.adventure.text.Component;

public class VelocityCommandWrapper {

    protected final CommandManager commandManager;
    protected final Translator translator;

    protected final String[] aliases;
    protected final String permission;
    protected final String command;

    public VelocityCommandWrapper(Command command, CommandManager commandManager,
                                  Translator translator) {

        this.command = command.getName();
        this.commandManager = commandManager;
        this.translator = translator;

        this.aliases = command.getAliases().toArray(new String[0]);
        this.permission = command.getPermission();
    }

    public BrigadierCommand brigadier() {
        final LiteralArgumentBuilder<CommandSource> literalBuilder = LiteralArgumentBuilder
                .<CommandSource>literal(command)
                .requires(source -> {
                    Namespace namespace = new NamespaceImpl();
                    namespace.setObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE, source);

                    return commandManager.getAuthorizer().isAuthorized(namespace, permission);
                })
                .executes(ctx -> {
                    CommandSource commandSource = ctx.getSource();
                    String input = ctx.getInput();
                    this.execution(commandSource, input);

                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }).then(RequiredArgumentBuilder.<CommandSource, String>argument("arguments", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> {
                            CommandSource commandSource = ctx.getSource();
                            String argumentLine = ctx.getInput();

                            Namespace namespace = new NamespaceImpl();
                            namespace.setObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE, commandSource);

                            commandManager.getSuggestions(namespace, argumentLine).forEach(builder::suggest);

                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            CommandSource commandSource = ctx.getSource();
                            String input = ctx.getInput();
                            this.execution(commandSource, input);

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                );


        return new BrigadierCommand(literalBuilder);
    }

    private void execution(CommandSource source, String argumentLine) {
        Namespace namespace = new NamespaceImpl();
        namespace.setObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE, source);
        namespace.setObject(String.class, "label", command);

        try {
            commandManager.execute(namespace, argumentLine);
        } catch (CommandException e) {
            CommandException exceptionToSend = e;

            if (e.getCause() instanceof CommandException) {
                exceptionToSend = (CommandException) e.getCause();
            }

            sendMessageToSender(e, namespace);

            throw new CommandException("An unexpected exception occurred while executing the command " + this.command, exceptionToSend);
        }
    }

    protected static void sendMessageToSender(CommandException exception, Namespace namespace) {
        CommandManager commandManager = namespace.getObject(CommandManager.class, "commandManager");
        CommandSource sender = namespace.getObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE);

        Component component = exception.getMessageComponent();
        Component translatedComponent = commandManager.getTranslator().translate(component, namespace);

        sender.sendMessage(translatedComponent);
    }
}

package tictim.ttmpdiscordbot.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.config.LoadableConfigType;
import tictim.ttmpdiscordbot.api.config.SavableConfigType;
import tictim.ttmpdiscordbot.api.wrapper.Profile;
import tictim.ttmpdiscordbot.config.Cfgs;
import tictim.ttmpdiscordbot.config.L10n;
import tictim.ttmpdiscordbot.config.SetupData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

@Mod.EventBusSubscriber(modid = "ttmpdiscordbot")
public final class ModCommands{
	private ModCommands(){}

	private static final DynamicCommandExceptionType RELOAD_UNKNOWN = new DynamicCommandExceptionType(o -> l10n("commands.discord.reload.unknown", o));
	private static final DynamicCommandExceptionType RELOAD_FAIL = new DynamicCommandExceptionType(o -> l10n("commands.discord.reload.fail", o));

	private static SimpleCommandExceptionType chatToOffline;

	private static SimpleCommandExceptionType getChatToOffline(){
		if(chatToOffline==null) chatToOffline = new SimpleCommandExceptionType(l10n("commands.discord.chatTo.offline"));
		return chatToOffline;
	}

	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event){
		CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();

		LiteralCommandNode<CommandSource> chatTo = dispatcher.register(literal("chatTo")
				.then(argument("channel", StringArgumentType.string())
						.suggests(TextChannelArguments::channelSuggestions)
						.then(argument("message", MessageArgument.message())
								.executes(ctx -> chatTo(ctx.getSource(), TextChannelArguments.parseChannel(StringArgumentType.getString(ctx, "channel")), MessageArgument.getMessage(ctx, "message")))
						)
				)
		);
		dispatcher.register(literal("chatto").redirect(chatTo));
		dispatcher.register(literal("discord")
				.requires(cs -> cs.hasPermissionLevel(4))
				.then(literal("status")
						.requires(cs -> cs.hasPermissionLevel(4))
						.executes(ctx -> status(ctx.getSource()))
				)
				.then(literal("setup")
						.requires(cs -> cs.hasPermissionLevel(4))
						.executes(ctx -> setup(ctx.getSource()))
				)
				.then(literal("error")
						.requires(cs -> cs.hasPermissionLevel(4))
						.executes(ctx -> error(ctx.getSource()))
				)
				.then(literal("reconnect")
						.requires(cs -> cs.hasPermissionLevel(4))
						.executes(ctx -> reconnect(ctx.getSource()))
				)
				.then(literal("reload")
						.requires(cs -> cs.hasPermissionLevel(4))
						.then(argument("config", StringArgumentType.word())
								.suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.stream(LoadableConfigType.values()).map(e -> e.name().toLowerCase()), builder))
								.executes(ctx -> reload(ctx.getSource(), StringArgumentType.getString(ctx, "config")))
						)
				)
				.then(literal("collectLocalization")
						.requires(cs -> cs.hasPermissionLevel(4))
						.then(argument("locale", StringArgumentType.word())
								.executes(ctx -> collectLocalization(ctx.getSource(), ctx.getArgument("locale", String.class)))
						)
				)
				.then(literal("stopCommandExecution")
						.requires(cs -> cs.hasPermissionLevel(4))
						.executes(ctx -> stopCommandExecution(ctx.getSource()))
				)
		);
		dispatcher.register(literal("rememberme")
				.then(argument("discordUser", StringArgumentType.string())
						.suggests(UserArguments::suggest)
						.executes(ctx -> remember(ctx.getSource(), UserArguments.parse(StringArgumentType.getString(ctx, "discordUser")), ctx.getSource().asPlayer().getGameProfile()))
				)
		);
		dispatcher.register(literal("forgetme")
				.executes(ctx -> forget(ctx.getSource(), ctx.getSource().asPlayer().getGameProfile()))
		);
		dispatcher.register(literal("remember")
				.requires(cs -> cs.hasPermissionLevel(1))
				.then(argument("discordUser", StringArgumentType.string())
						.suggests(UserArguments::suggest)
						.then(argument("player", GameProfileArgument.gameProfile())
								.executes(ctx -> remember(ctx.getSource(), UserArguments.parse(StringArgumentType.getString(ctx, "discordUser")), ctx.getArgument("player", GameProfile.class)))
						)
				)
		);
		dispatcher.register(literal("forget")
				.requires(cs -> cs.hasPermissionLevel(1))
				.then(argument("player", GameProfileArgument.gameProfile())
						.executes(ctx -> forget(ctx.getSource(), ctx.getArgument("player", GameProfile.class)))
				)
		);
	}

	private static int chatTo(CommandSource source, TextChannel channel, ITextComponent text) throws CommandSyntaxException{
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		if(bot.isShuttedDown()) throw getChatToOffline().create();
		else if(!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) source.sendErrorMessage(l10n("commands.discord.chatTo.noPermission"));
		else{
			bot.say("**<"+source.getName()+">** "+text.getString(), channel);
			bot.chat(text, source.getDisplayName(), channel);
		}
		return SINGLE_SUCCESS;
	}

	private static int error(CommandSource source){
		List<String> errors = TTMPDiscordBot.get().errors;
		source.sendFeedback(errors.isEmpty() ? l10n("commands.discord.error.noError") :
				l10n("commands.discord.error", errors.size(), String.join("\n  ", errors.stream().map(s -> s.replace("\n", "\n  ")).toArray(String[]::new))), false);
		return SINGLE_SUCCESS;
	}

	private static int status(CommandSource source){
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		if(!bot.isShuttedDown()){
			if(bot.isConnected()) source.sendFeedback(l10n("commands.discord.status.online"), true);
			else source.sendFeedback(l10n("commands.discord.status.restoringConnection"), true);
		}else source.sendFeedback(l10n("commands.discord.status.offline"), true);
		return SINGLE_SUCCESS;
	}

	private static int reconnect(CommandSource source){
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		if(!bot.isShuttedDown()) bot.disconnect();
		if(bot.connectWithMessage(l10n("discord.reconnect"))){
			source.sendFeedback(l10n("command.discord.reconnect.success"), false);
			return SINGLE_SUCCESS;
		}else return 0;
	}

	private static int reload(CommandSource source, String typeString) throws CommandSyntaxException{
		LoadableConfigType type;
		try{
			type = LoadableConfigType.valueOf(typeString.toUpperCase());
		}catch(IllegalArgumentException ex){
			throw RELOAD_UNKNOWN.create(typeString);
		}
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		List<String> errors = new ArrayList<>();
		bot.loadConfig(type, errors::add);

		if(type.contains(LoadableConfigType.GENERAL)){
			if(!bot.isShuttedDown()) bot.disconnect();
			bot.connectWithMessage(l10n("discord.reconnect"));
		}
		if(!errors.isEmpty()) throw RELOAD_FAIL.create(String.join("\n  ", errors));
		source.sendFeedback(l10n("commands.discord.reload.success"), false);
		return SINGLE_SUCCESS;
	}

	private static int remember(CommandSource source, User user, GameProfile profile){ // TODO can override option
		TTMPDiscordBot.get().userSettings().add(user.getIdLong(), new Profile(profile));
		source.sendFeedback(l10n("commands.discord.remember.success"), true);
		return SINGLE_SUCCESS;
	}

	private static int forget(CommandSource source, GameProfile profile){ // TODO can override option
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		User u = bot.userSettings().user(profile);
		if(u!=null){
			bot.userSettings().removeUser(u);
			source.sendFeedback(l10n("commands.discord.forget.success"), true);
			return SINGLE_SUCCESS;
		}else{
			source.sendErrorMessage(l10n("commands.discord.forget.fail"));
			return 0;
		}
	}

	private static int collectLocalization(CommandSource source, String locale){
		ExecutorService s = Executors.newFixedThreadPool(1);
		s.submit(() -> {
			L10n l10n = new L10n();
			l10n.collect(locale);
			if(!l10n.isEmpty()){
				AtomicBoolean ab = new AtomicBoolean();
				Cfgs.writeToConfig("localizations_collected", l10n, e -> {
					source.getServer().execute(() -> source.sendErrorMessage(l10n("commands.discord.collectLocalization.fail", e)));
					ab.set(true);
				});
				if(!ab.get()) source.getServer().execute(() -> source.sendFeedback(l10n("commands.discord.collectLocalization.success", l10n.size()), true));
			}else source.getServer().execute(() -> source.sendErrorMessage(l10n("commands.discord.collectLocalization.notFound", locale)));
		});
		s.shutdown();
		return SINGLE_SUCCESS;
	}

	private static int stopCommandExecution(CommandSource source){
		TTMPDiscordBot.get().commandExecutor().reset();
		source.sendFeedback(l10n("commands.discord.stopCommandExecution.success"), true);
		return SINGLE_SUCCESS;
	}

	private static int setup(CommandSource source){
		SetupData setupData = SetupData.read(ex -> source.sendErrorMessage(l10n("commands.discord.setup.fail", ex)));
		if(setupData!=null){
			String botToken = setupData.getBotToken();
			if(botToken.isEmpty()){
				source.sendFeedback(l10n("commands.discord.setup"), false);
				return SINGLE_SUCCESS;
			}else{
				Cfgs.setLocale(setupData.getLocale());
				boolean result = setupInternal(source, botToken);
				Cfgs.setLocale(null);
				if(result){
					source.sendFeedback(l10n("commands.discord.setup.success"), false);
					return SINGLE_SUCCESS;
				}
			}
		}
		return 0;
	}

	private static boolean setupInternal(CommandSource source, String botToken){
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		AtomicBoolean flag = new AtomicBoolean();

		// re-collect localization data
		bot.l10n().collect(Cfgs.getLocale());
		bot.saveConfig(SavableConfigType.LOCALIZATIONS, ex -> {
			source.sendErrorMessage(l10n("commands.discord.setup.fail.l10n", ex));
			flag.set(true);
		});
		if(flag.get()) return false;

		// regenerate commands
		Cfgs.generateDefaultBotCommands(Cfgs.configDirectory("commands"));
		bot.loadConfig(LoadableConfigType.COMMANDS, ex -> {
			source.sendErrorMessage(l10n("commands.discord.setup.fail.command", ex));
			flag.set(true);
		});
		if(flag.get()) return false;

		// regenerate activity
		Cfgs.generateDefaultActivity(Cfgs.configDirectory("activity"));
		bot.loadConfig(LoadableConfigType.ACTIVITY, ex -> {
			source.sendErrorMessage(l10n("commands.discord.setup.fail.activity", ex));
			flag.set(true);
		});
		if(flag.get()) return false;

		if(!bot.isShuttedDown()) bot.disconnect();
		bot.generalSettings().setBotToken(botToken);
		if(bot.connectWithMessage(l10n("discord.serverStart"))){
			return true;
		}else{
			source.sendErrorMessage(l10n("commands.discord.setup.fail.login"));
			return false;
		}
	}

	private static ITextComponent l10n(String key, Object... args){
		return TTMPDiscordBot.get().l10n().unfold(new TranslationTextComponent(key, args), false);
	}
}
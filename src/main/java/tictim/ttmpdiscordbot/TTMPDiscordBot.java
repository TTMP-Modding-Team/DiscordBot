package tictim.ttmpdiscordbot;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tictim.ttmpdiscordbot.api.Bot;
import tictim.ttmpdiscordbot.api.DiscordBotEvent;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.botcommand.BotCommandContext;
import tictim.ttmpdiscordbot.api.config.LoadableConfigType;
import tictim.ttmpdiscordbot.api.config.SavableConfigType;
import tictim.ttmpdiscordbot.ast.node.ChannelMentionNode;
import tictim.ttmpdiscordbot.ast.node.MemberMentionNode;
import tictim.ttmpdiscordbot.ast.node.Node;
import tictim.ttmpdiscordbot.ast.node.RoleMentionNode;
import tictim.ttmpdiscordbot.ast.parser.Parser;
import tictim.ttmpdiscordbot.ast.textcomponent.MarkdownRules;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;
import tictim.ttmpdiscordbot.botcommand.CommandExecutor;
import tictim.ttmpdiscordbot.botcommand.ExecuteResult;
import tictim.ttmpdiscordbot.botcommand.IngameChatContext;
import tictim.ttmpdiscordbot.botcommand.TextChannelContext;
import tictim.ttmpdiscordbot.config.*;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minecraft.util.text.event.ClickEvent.Action.OPEN_URL;
import static tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder.hoverTextEvent;

@Mod("ttmpdiscordbot")
public final class TTMPDiscordBot implements Bot{
	public static final Logger LOGGER = LogManager.getLogger("TTMP Discord Bot");

	private static final Pattern COMMAND_MATCH_PATTERN = Pattern.compile("^!(?!!)(\\S+) *(\\S.*)?$");
	private static final ThreadLocal<Matcher> COMMAND_MATCH = ThreadLocal.withInitial(() -> COMMAND_MATCH_PATTERN.matcher(""));

	private static TTMPDiscordBot instance;
	public static TTMPDiscordBot get(){
		return instance;
	}

	// region Fields

	public final List<String> errors = new ArrayList<>();

	private JDA jda;
	private ExecutorService discordChatSender;
	private ExecutorService minecraftChatSender;
	private final CommandExecutor commandExecutor = new CommandExecutor();

	private final ThreadLocal<Parser<TextChannel, Node<TextChannel>>> chatParser = ThreadLocal.withInitial(() -> {
		Parser<TextChannel, Node<TextChannel>> p = new Parser<>();
		p.addRules(MarkdownRules.createRules());
		return p;
	});

	private GeneralSettingsImpl generalSettings;
	private UserSettingsImpl userSettings;
	private ChannelSettingsImpl channelSettings;
	private final BotCommandsImpl botCommands = new BotCommandsImpl();
	private final BotActivity activity = new BotActivity();
	private L10n l10n;

	private final DiscordBotEventHandler eventHandler = new DiscordBotEventHandler();

	// endregion

	public TTMPDiscordBot(){
		if(instance==null) synchronized(TTMPDiscordBot.class){
			if(instance==null) instance = this;
			else throw new IllegalStateException("Multiple instances of Discord bot is not allowed!");
		}
		MinecraftForge.EVENT_BUS.register(eventHandler);
	}

	// region Bot

	@Override @Nullable public JDA getJDA(){
		return jda;
	}
	@Override public GeneralSettingsImpl generalSettings(){
		return generalSettings;
	}
	@Override public UserSettingsImpl userSettings(){
		return userSettings;
	}
	@Override public ChannelSettingsImpl channelSettings(){
		return channelSettings;
	}
	@Override public BotCommandsImpl botCommands(){
		return botCommands;
	}
	public L10n l10n(){
		return l10n;
	}
	public CommandExecutor commandExecutor(){
		return commandExecutor;
	}

	@Override public boolean isConnected(){
		return jda!=null&&jda.getStatus()==JDA.Status.CONNECTED;
	}
	@Override public boolean isShuttedDown(){
		return jda==null||jda.getStatus()==JDA.Status.SHUTDOWN;
	}

	@Override
	public boolean connect(){
		try{
			this.jda = new JDABuilder()
					.setToken(generalSettings.botToken())
					.setRequestTimeoutRetry(generalSettings.retryOnTimeout())
					.setMaxReconnectDelay(generalSettings.maxReconnectDelay())
					.addEventListeners(eventHandler)
					.build();
			discordChatSender.submit(() ->
					awaitReadyAnd(() ->
							ServerLifecycleHooks.getCurrentServer().execute(() ->
									MinecraftForge.EVENT_BUS.post(new DiscordBotEvent.Connected(this)))));
			return true;
		}catch(LoginException e){
			LOGGER.error("Couldn't connect to discord, failed to login.\n{}", e.toString());
		}catch(IllegalArgumentException e){
			LOGGER.error("Couldn't connect to discord, an unexpected error occurred.", e);
		}
		return false;
	}
	@Override
	public boolean connectWithMessage(ITextComponent message){
		if(isShuttedDown()){
			if(connect()){
				say(message);
				return true;
			}
		}else LOGGER.error("Couldn't connect to discord, since the bot is already connected.");
		return false;
	}

	@Override
	public void disconnect(){
		if(!isShuttedDown()){
			JDA jda = this.jda;
			discordChatSender.submit(() -> {
				jda.shutdown();
				jda.removeEventListener(eventHandler);
			});
			MinecraftForge.EVENT_BUS.post(new DiscordBotEvent.Disconnected(this));
		}else LOGGER.error("Couldn't disconnect from discord, since the bot is already disconnected.");
		jda = null;
	}
	@Override
	public void disconnectWithMessage(ITextComponent message){
		if(!isShuttedDown()){
			say(message);
			disconnect();
		}else LOGGER.error("Couldn't disconnect from discord, since the bot is already disconnected.");
	}

	@Override
	public void say(ITextComponent message, @Nullable TextChannel channel){
		if(!isShuttedDown()) discordChatSender.submit(channel!=null ?
				() -> awaitReadyAnd(() -> sayInternal(l10n.unfold(message, true).getString(), channel)) :
				() -> awaitReadyAnd(() -> {
					List<TextChannel> channels = getChannels(channelSettings::canSendMinecraftChat);
					if(!channels.isEmpty()) for(TextChannel c: channels) sayInternal(l10n.unfold(message, true).getString(), c);
					//LOGGER.debug("Sent message to {} channels.", channels.size());
				}));
		else LOGGER.error("Couldn't send message to discord, bot is shutted down.");
	}
	@Override
	public void say(String message, @Nullable TextChannel channel){
		if(!isShuttedDown()) discordChatSender.submit(channel!=null ?
				() -> awaitReadyAnd(() -> sayInternal(message, channel)) :
				() -> awaitReadyAnd(() -> {
					List<TextChannel> channels = getChannels(channelSettings::canSendMinecraftChat);
					if(!channels.isEmpty()) for(TextChannel c: channels) sayInternal(message, c);
					//LOGGER.debug("Sent message to {} channels.", channels.size());
				}));
		else LOGGER.error("Couldn't send message to discord, bot is shutted down.");
	}
	@Override
	public void chat(Message message){
		boolean isTldr = isTldr(message);
		ITextComponent chat = isTldr ? messageToTldrChat(message.getContentRaw(), message.getTextChannel()) : messageToChat(message.getContentRaw(), message.getTextChannel());

		TTMPDiscordBot.LOGGER.debug("{} tl;dr: {}", message.getContentStripped(), isTldr(message));

		if(message.getAttachments().isEmpty()) chat(chat, message.getMember(), message.getTextChannel());
		else{
			ITextComponent text = new StringTextComponent("");
			for(Message.Attachment a: message.getAttachments()){
				ITextComponent t2 = new TranslationTextComponent(a.isImage() ? "discord.chat.attachment.image" : a.isVideo() ? "discord.chat.attachment.video" : "discord.chat.attachment");
				t2.getStyle().setHoverEvent(hoverTextEvent(new TranslationTextComponent("discord.chat.link"))).setClickEvent(new ClickEvent(OPEN_URL, a.isImage()||a.isVideo() ? a.getProxyUrl() : a.getUrl()));
				text.appendSibling(t2).appendText(" ");
			}
			chat(text.appendSibling(chat), message.getMember(), message.getTextChannel());
		}
	}

	private boolean isTldr(Message message){
		return (generalSettings.tldrLineBreak()>0&&generalSettings.tldrLineBreak()<=message.getContentRaw().chars().filter(i -> i=='\n').count())||
				(generalSettings.tldrCharacters()>0&&generalSettings.tldrCharacters()<=message.getContentStripped().length());
	}

	private void sayInternal(String message, TextChannel channel){
		// 1. Convert #channel to channel ID, @mention to member ID, etc
		// 2. We don't need to cast magic here :))))))
		// 3. We don't have to do anything????
		channel.sendMessage(MessageFormatUtils.detectAndReplaceMentions(message, channel, this)).queue(null, e -> LOGGER.error("Couldn't send message to discord, unexpected exception occurred. ", e));
	}

	@Override public void chat(String message, Member member, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(messageToChat(message, channel), displayName(member), channel); });
	}
	@Override public void chat(ITextComponent message, Member member, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(message, displayName(member), channel); });
	}
	@Override public void chat(String message, User user, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(messageToChat(message, channel), displayName(user), channel); });
	}
	@Override public void chat(ITextComponent message, User user, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(message, displayName(user), channel); });
	}
	@Override public void chat(String message, String name, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(messageToChat(message, channel), new StringTextComponent(name), channel); });
	}
	@Override public void chat(ITextComponent message, String name, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(message, new StringTextComponent(name), channel); });
	}
	@Override public void chat(String message, Entity entity, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(messageToChat(message, channel), entity.getDisplayName(), channel); });
	}
	@Override public void chat(ITextComponent message, Entity entity, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(message, entity.getDisplayName(), channel); });
	}
	@Override public void chat(String message, ITextComponent name, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(messageToChat(message, channel), name, channel); });
	}
	@Override public void chat(ITextComponent message, ITextComponent name, @Nullable TextChannel channel){
		minecraftChatSender.submit(() -> { if(ServerLifecycleHooks.getCurrentServer()!=null) chatInternal(message, name, channel); });
	}

	private ITextComponent messageToChat(String message, @Nullable TextChannel channel){
		return messageToChat(TextComponentBuilder.create(), message, channel);
	}
	private ITextComponent messageToTldrChat(String message, @Nullable TextChannel channel){
		return messageToChat(TextComponentBuilder.createHoverTextBuilder(new TranslationTextComponent("discord.chat.tldr")), message, channel);
	}
	private ITextComponent messageToChat(TextComponentBuilder builder, String message, @Nullable TextChannel channel){
		// 1. convert unicode emojis
		// 2. magic
		return chatParser.get().castMagic(builder, EmojiParser.parseFromUnicode(message.replace("(?!\\\\):", "\\:"), TTMPDiscordBot::shortestName), channel);
	}

	private static String shortestName(EmojiParser.UnicodeCandidate emoji){
		List<String> list = emoji.getEmoji().getAliases();
		String str = list.get(0);
		for(int i = 1; i<list.size(); i++){
			if(str.length()>list.get(i).length()) str = list.get(i);
		}
		return emoji.hasFitzpatrick() ? String.format(":%s_tone%d:", str, emoji.getFitzpatrick().ordinal()+1) : String.format(":%s:", str);
	}

	private ITextComponent displayName(Member member){
		ITextComponent text = new StringTextComponent(member.getEffectiveName());
		MemberMentionNode.setEvent(text.getStyle(), member);
		for(Role r: member.getRoles()){
			if(r.getColor()==null) continue;
			text.getStyle().setColor(RoleMentionNode.getClosestColorToRoleColor(r));
			break;
		}
		return text;
	}

	private ITextComponent displayName(User user){
		ITextComponent text = new StringTextComponent(user.getName());
		MemberMentionNode.setEvent(text.getStyle(), user);
		return text;
	}

	private void chatInternal(ITextComponent message, ITextComponent name, @Nullable TextChannel channel){
		// TTMPDiscordBot.LOGGER.debug(MessageFormatUtils.toDebugString(message));
		ITextComponent text;
		if(channel!=null){
			ITextComponent text1 = new StringTextComponent("[#"+channel.getName()+"]");
			ChannelMentionNode.setEvent(text1.getStyle(), channel);
			text = new StringTextComponent("").appendSibling(text1).appendText(" <");
		}else text = new StringTextComponent("<");
		for(ITextComponent t: name) text.appendSibling(t);
		name.getSiblings().clear();
		// 3. unfold
		// 4. chat
		ServerLifecycleHooks.getCurrentServer().execute(() -> ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(l10n.unfold(text.appendText("> ").appendSibling(message), false), false));
	}

	private List<TextChannel> getChannels(Predicate<TextChannel> test){
		return jda.getTextChannelCache().stream().filter(test).collect(Collectors.toList());
	}

	private void awaitReadyAnd(Runnable runnable){
		try{
			jda.awaitReady();
			runnable.run();
		}catch(Exception ignored){
		}
	}

	@Override
	public void loadConfig(LoadableConfigType type, Consumer<String> errorHandler){
		switch(type){
			case GENERAL:
				this.generalSettings = Cfgs.readFromConfig("general", GeneralSettingsImpl.class, GeneralSettingsImpl::new,
						e -> errorHandler.accept("Couldn't read general settings due to unexpected exception:\n"+e));
				break;
			case USERS:
				this.userSettings = Cfgs.readFromConfig("users", UserSettingsImpl.class, UserSettingsImpl::new,
						e -> errorHandler.accept("Couldn't read user settings due to unexpected exception:\n"+e));
				break;
			case CHANNELS:
				this.channelSettings = Cfgs.readFromConfig("channels", ChannelSettingsImpl.class, ChannelSettingsImpl::new,
						e -> errorHandler.accept("Couldn't read channel settings due to unexpected exception:\n"+e));
				break;
			case COMMANDS:{
				this.botCommands.clear();
				File f = Cfgs.configDirectory("commands");
				if(!f.exists()) Cfgs.generateDefaultBotCommands(f);
				else if(!f.isDirectory()){
					errorHandler.accept("Couldn't read commands, directory expected.");
					return;
				}
				this.botCommands.read(f, errorHandler);
				this.botCommands.subscribe();
			}
			break;
			case LOCALIZATIONS:
				this.l10n = Cfgs.readFromConfig("localizations", L10n.class, () -> {
					L10n l10n = new L10n();
					l10n.collectDefaults(Cfgs.getLocale());
					return l10n;
				}, e -> errorHandler.accept("Couldn't read localization data due to unexpected exception:\n"+e));
				break;
			case ACTIVITY:
				// TODO figure out why did i put todo here
				this.activity.clear();
				File f = Cfgs.configDirectory("activity");
				if(!f.exists()) Cfgs.generateDefaultActivity(f);
				else if(!f.isDirectory()){
					errorHandler.accept("Couldn't read activity, directory expected.");
					return;
				}
				this.activity.read(f, errorHandler);
				this.activity.subscribe();
				break;
			case ALL:
				for(LoadableConfigType t: LoadableConfigType.values()) if(t!=LoadableConfigType.ALL) loadConfig(t, errorHandler);
				return;
			default:
				errorHandler.accept(String.format("Unknown setting '%s'.", type));
				return;
		}
		LOGGER.debug("Loaded setting '{}'", type);
	}

	@Override
	public void saveConfig(SavableConfigType type, Consumer<String> errorHandler){
		switch(type){
			case GENERAL:
				Cfgs.writeToConfig("general", generalSettings, e -> errorHandler.accept("Couldn't write general settings due to unexpected exception:\n"+e));
				break;
			case USERS:
				Cfgs.writeToConfig("users", userSettings, e -> errorHandler.accept("Couldn't write user settings due to unexpected exception:\n"+e));
				break;
			case CHANNELS:
				Cfgs.writeToConfig("channels", channelSettings, e -> errorHandler.accept("Couldn't write channel settings due to unexpected exception:\n"+e));
				break;
			case LOCALIZATIONS:
				Cfgs.writeToConfig("localizations", l10n, e -> errorHandler.accept("Couldn't write localization data due to unexpected exception:\n"+e));
				break;
			case ALL:
				for(SavableConfigType t: SavableConfigType.values()) if(t!=SavableConfigType.ALL) saveConfig(t, errorHandler);
				return;
			default:
				errorHandler.accept(String.format("Unknown config type '%s'.", type));
				return;
		}
		LOGGER.debug("Loaded setting '{}'", type);
	}

	// endregion

	public void defaultErrorHandler(String errorMessage){
		LOGGER.error(errorMessage);
		errors.add(errorMessage);
	}

	private void setup(){
		TTMPDiscordBot.LOGGER.info("Starting discord bot!");
		this.discordChatSender = Executors.newFixedThreadPool(1);
		this.minecraftChatSender = Executors.newFixedThreadPool(1);
		this.commandExecutor.setup();
		loadConfig(LoadableConfigType.ALL, this::defaultErrorHandler);

		SetupData.read(ex -> defaultErrorHandler("Couldn't write user settings due to unexpected exception:\n"+ex));
		connectWithMessage(new TranslationTextComponent("discord.serverStart"));
		TTMPDiscordBot.LOGGER.info("Started discord bot.");
	}
	private void clear(boolean unexpected){
		if(!isShuttedDown()){
			TTMPDiscordBot.LOGGER.info("Shutting down discord bot!");
			disconnectWithMessage(new TranslationTextComponent(unexpected ? "discord.serverStop.error" : "discord.serverStop"));
		}
		jda = null;
		botCommands.clear();
		errors.clear();

		discordChatSender.shutdown(); // TODO Doesn't terminate? Needs check
		minecraftChatSender.shutdown();
		commandExecutor.invalidate();
		TTMPDiscordBot.LOGGER.info("Shutted down discord bot.");
	}

	private ExecuteResult tryExecute(BotCommand.Scope scope, Matcher matcher, BiFunction<BotCommand, Matcher, BotCommandContext> contextFactory){
		ExecuteResult result = new ExecuteResult(matcher.group(1), matcher.group(2));
		botCommands.tryExecuteCommand(result, scope, contextFactory);
		return result;
	}

	public final class DiscordBotEventHandler extends ListenerAdapter{
		private boolean noError;

		private DiscordBotEventHandler(){}

		@Override
		public void onGuildMessageReceived(GuildMessageReceivedEvent event){
			if(!event.getAuthor().equals(event.getJDA().getSelfUser())){
				Message m = event.getMessage();
				TextChannel ch = event.getChannel();

				ExecuteResult result;
				if(m.getAttachments().isEmpty()&&channelSettings.isDiscordCommandsEnabled(ch)){
					Matcher match = COMMAND_MATCH.get();
					if(match.reset(m.getContentRaw()).matches()){
						result = tryExecute(BotCommand.Scope.DISCORD, match, (botCommand, matcher) -> new TextChannelContext(botCommand, matcher, m));
					}else result = null;
				}else result = null;

				if((result==null||!result.isSilent())&&channelSettings.canReceiveDiscordChat(ch)) chat(m);
				if(result!=null) result.runCommand(r -> commandExecutor.submit(() -> awaitReadyAnd(r)));
			}
		}

		// region Server Lifecycle Events

		@SubscribeEvent(priority = EventPriority.HIGHEST) public void serverAboutToStart(FMLServerAboutToStartEvent event){
			noError = false;
		}
		@SubscribeEvent(priority = EventPriority.HIGHEST) public void onServerStarted(FMLServerStartedEvent event){
			setup();
		}
		@SubscribeEvent(priority = EventPriority.LOWEST) public void onServerStopping(FMLServerStoppingEvent event){
			noError = true;
		}
		@SubscribeEvent(priority = EventPriority.LOWEST) public void onServerStopped(FMLServerStoppedEvent event){
			clear(!noError);
		}

		// endregion

		// region Chat Processing / Message

		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public void onChatEarly(ServerChatEvent event){
			if(!isShuttedDown()){
				// 1. Convert #channel to channel ID, @mention to member ID, etc
				String replaced = MessageFormatUtils.detectAndReplaceMentions(event.getMessage(), null, TTMPDiscordBot.this);
				// 2. Cast magic
				// 3. "Unfold" translated texts starting with 'discord.' so this stupid mod could still be serversided while not displaying some horrible bullshit like discord.fuckyou
				// 4. Set text
				// @see ServerPlayNetHandler#processChatMessage

				event.setComponent(l10n.unfold(new TranslationTextComponent("chat.type.text", event.getPlayer().getDisplayName(),
						chatParser.get().castMagic(TextComponentBuilder.create(), replaced, null)), false));
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void onChatAfter(ServerChatEvent event){
			if(!event.isCanceled()&&event.getComponent()!=null){
				Matcher match = COMMAND_MATCH.get();
				ExecuteResult result = match.reset(event.getMessage()).matches() ?
						tryExecute(BotCommand.Scope.MINECRAFT, match, (botCommand, matcher) -> new IngameChatContext(botCommand, matcher, event.getMessage(), event.getPlayer())) :
						null;
				if(result==null||!result.isSilent()){
					say("**<"+event.getUsername()+">** "+event.getMessage());
					// TTMPDiscordBot.LOGGER.debug(MessageFormatUtils.toDebugString(event.getComponent()));
				}
				if(result!=null) result.runCommand(r -> commandExecutor.submit(() -> awaitReadyAnd(r)));
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST) // TODO Move to JS, if possible
		public void onPlayerDeath(LivingDeathEvent event){
			LivingEntity e = event.getEntityLiving();
			if(e instanceof ServerPlayerEntity) say(e.getCombatTracker().getDeathMessage());
		}

		// endregion
	}
}

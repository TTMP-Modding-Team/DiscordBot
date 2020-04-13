package tictim.ttmpdiscordbot.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import tictim.ttmpdiscordbot.TTMPDiscordBot;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Because adding new ArgumentType is impossible with server-side only
 */
public final class TextChannelArguments{
	private TextChannelArguments(){}

	private static final Dynamic2CommandExceptionType CHANNEL_AMBIGUOUS = new Dynamic2CommandExceptionType((o1, o2) -> new TranslationTextComponent("argument.discord.channel.ambiguous", o1, o2));
	private static final DynamicCommandExceptionType CHANNEL_NOT_FOUND = new DynamicCommandExceptionType(o -> new TranslationTextComponent("argument.discord.channel.notFound", o));
	private static final SimpleCommandExceptionType CHANNEL_INVALID = new SimpleCommandExceptionType(new TranslationTextComponent("argument.discord.channel.invalid"));
	private static final Matcher CHANNEL_SHORT = Pattern.compile("^([^\\s#]+)$").matcher("");
	private static final Matcher CHANNEL_FULL = Pattern.compile("^([^#]+)#([^\\s#]+)$").matcher("");

	public static CompletableFuture<Suggestions> channelSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder){
		String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
		if(!remaining.isEmpty()&&(remaining.indexOf(0)=='\''||remaining.indexOf(0)=='"')) remaining = remaining.substring(1);
		int i = remaining.indexOf('#');
		if(i!=0){
			if(i>0){
				List<TextChannel> channels;
				List<Guild> guilds = TTMPDiscordBot.get().getJDA().getGuildsByName(remaining.substring(0, i), true);
				switch(guilds.size()){
					case 0:
						return Suggestions.empty();
					case 1:
						channels = guilds.get(0).getTextChannelCache().asList();
						break;
					default:
						channels = guilds.stream().flatMap(g -> g.getTextChannelCache().stream()).collect(Collectors.toList());
				}
				for(TextChannel ch: channels){
					String s = ch.getGuild().getName()+"#"+ch.getName();
					String sl = s.toLowerCase();
					if(sl.startsWith(remaining)) builder.suggest('"'+s+'"');
				}
			}else{
				List<TextChannel> channels = TTMPDiscordBot.get().getJDA().getTextChannelCache().asList();
				Map<String, TextChannel> channelNames = new HashMap<>();
				Set<TextChannel> duplicates = new HashSet<>();
				for(TextChannel ch: channels){
					String lowerCasedName = ch.getName().toLowerCase();
					if(!channelNames.containsKey(lowerCasedName)) channelNames.put(lowerCasedName, ch);
					else{
						duplicates.add(ch);
						TextChannel ch2 = channelNames.put(lowerCasedName, null);
						if(ch2!=null) duplicates.add(ch2);
					}
				}
				for(Iterator<Map.Entry<String, TextChannel>> iterator = channelNames.entrySet().iterator(); iterator.hasNext(); ){
					Map.Entry<String, TextChannel> e = iterator.next();
					if(e.getValue()==null||duplicates.contains(e.getValue())||!e.getKey().startsWith(remaining)) continue;
					builder.suggest(e.getValue().getName());
					iterator.remove();
				}
				for(TextChannel ch: channels){
					String s = ch.getGuild().getName()+"#"+ch.getName();
					String sl = s.toLowerCase();
					if(sl.startsWith(remaining)) builder.suggest('"'+s+'"');
				}
			}
			return builder.buildFuture();
		}
		return Suggestions.empty();
	}

	public static TextChannel parseChannel(String input) throws CommandSyntaxException{
		if(CHANNEL_SHORT.reset(input).matches()) return parse(CHANNEL_SHORT.group(1), null, input);
		else if(CHANNEL_FULL.reset(input).matches()) return parse(CHANNEL_FULL.group(2), CHANNEL_FULL.group(1), input);
		else throw CHANNEL_INVALID.create();
	}

	private static TextChannel parse(String channelName, @Nullable String guildName, String originalInput) throws CommandSyntaxException{
		List<TextChannel> channels;
		if(guildName==null) channels = TTMPDiscordBot.get().getJDA().getTextChannelsByName(channelName, true);
		else{
			//TTMPDiscordBot.LOGGER.debug("Available guilds {}", TTMPDiscordBot.get().getJDA().getGuilds().stream().map(Guild::getName).collect(Collectors.joining(" ")));
			List<Guild> guilds = TTMPDiscordBot.get().getJDA().getGuildsByName(guildName, true);
			//TTMPDiscordBot.LOGGER.debug("Found guilds {}", guilds.stream().map(Guild::getName).collect(Collectors.joining(" ")));
			switch(guilds.size()){
				case 0:
					throw CHANNEL_NOT_FOUND.create(originalInput);
				case 1:
					channels = guilds.get(0).getTextChannelsByName(channelName, true);
					break;
				default:
					channels = guilds.stream().flatMap(g -> g.getTextChannelsByName(channelName, true).stream()).collect(Collectors.toList());
			}
		}
		//TTMPDiscordBot.LOGGER.debug("Found chat {}", channels.stream().map(GuildChannel::getName).collect(Collectors.joining(" ")));
		switch(channels.size()){
			case 0:
				throw CHANNEL_NOT_FOUND.create(originalInput);
			case 1:
				return channels.get(0);
			default:
				throw CHANNEL_AMBIGUOUS.create(originalInput, channels.stream().map(ch -> ch.getGuild().getName()+"#"+ch.getName()).collect(Collectors.joining(", ")));
		}
	}
}

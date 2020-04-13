package tictim.ttmpdiscordbot.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import tictim.ttmpdiscordbot.TTMPDiscordBot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Because adding new ArgumentType is impossible with server-side only
 */
public final class UserArguments{
	private UserArguments(){}

	private static final Dynamic2CommandExceptionType USER_AMBIGUOUS = new Dynamic2CommandExceptionType((o1, o2) -> new TranslationTextComponent("argument.discord.user.ambiguous", o1, o2));
	private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(o -> new TranslationTextComponent("argument.discord.user.notFound", o));

	public static CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder){
		return ISuggestionProvider.suggest(TTMPDiscordBot.get().getJDA().getUsers().stream().map(u -> {
			String n = u.getName();
			return n.indexOf(' ') >= 0 ? "\""+n+"\"" : n;
		}), builder);
	}

	public static User parse(String input) throws CommandSyntaxException{
		JDA jda = TTMPDiscordBot.get().getJDA();
		List<User> users = jda.getUsersByName(input, true);
		switch(users.size()){
			case 0:
				try{
					User u = jda.getUserById(input);
					if(u!=null) return u;
				}catch(NumberFormatException ignored){
					// shh
				}
				throw USER_NOT_FOUND.create(input);
			case 1:
				return users.get(0);
			default:
				throw USER_AMBIGUOUS.create(input, users.stream().map(User::getName).collect(Collectors.joining()));
		}
	}
}

package tictim.ttmpdiscordbot.config;

import org.apache.commons.lang3.StringUtils;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.botcommand.BotCommandContext;
import tictim.ttmpdiscordbot.api.config.BotCommands;
import tictim.ttmpdiscordbot.botcommand.ExecuteResult;
import tictim.ttmpdiscordbot.javascript.JSBotCommand;
import tictim.ttmpdiscordbot.javascript.JSReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotCommandsImpl extends JSEventParser implements BotCommands{
	private static final Matcher EMPTY_MATCH = Pattern.compile("").matcher("");

	private final List<BotCommand> commands = new ArrayList<>();

	@Override
	public List<BotCommand> commands(){
		return Collections.unmodifiableList(commands);
	}

	public void tryExecuteCommand(ExecuteResult result, BotCommand.Scope scope, BiFunction<BotCommand, Matcher, BotCommandContext> contextFactory){
		// TTMPDiscordBot.LOGGER.debug("Searching for command matching input '{}' '{}'", result.getSignature(), result.getArguments());
		if(StringUtils.isBlank(result.getSignature())){
			result.setContext(contextFactory.apply(null, null));
			return;
		}
		for(BotCommand c: commands){
			if(c.getScope().containsScope(scope)) for(String s: c.signatures()){
				if(result.getSignature().equals(s)){
					if(c.arguments()==null){
						result.setContext(contextFactory.apply(c, EMPTY_MATCH));
						return;
					}
					Matcher m = c.arguments().matcher(result.getArguments());
					if(m.matches()){
						result.setContext(contextFactory.apply(c, m));
						return;
					}
					result.mismatchedArguments().add(c);
					break;
				}
			}
		}
		result.setContext(contextFactory.apply(result.hasMismatchingArguments() ? result.mismatchedArguments().get(0) : null, null));
	}

	public void read(File root, Consumer<String> errorHandler){
		JSReader reader = new JSReader(root);
		TTMPDiscordBot.LOGGER.info("Read {} commands and {} events", readCommands(reader, errorHandler), readEvents(reader, errorHandler));
		reader.checkForUnusedFiles(unusedFiles ->
				TTMPDiscordBot.LOGGER.info("{} files were ignored since no valid function was found: \n    {}",
						unusedFiles.size(), String.join("\n    ", unusedFiles)));
	}

	protected int readCommands(JSReader reader, Consumer<String> errorHandler){
		List<BotCommand> list = reader.parse("commands", JSBotCommand::new, errorHandler);
		commands.addAll(list);
		return list.size();
	}

	public void clear(){
		commands.clear();
		super.clear();
	}
}

package tictim.ttmpdiscordbot.config;

import net.dv8tion.jda.api.entities.Activity;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.javascript.JSReader;

import java.io.File;
import java.util.function.Consumer;

public final class BotActivity extends JSEventParser{
	public void read(File root, Consumer<String> errorHandler){
		JSReader reader = new JSReader(root);
		TTMPDiscordBot.LOGGER.info("Read {} events from activity file", readEvents(reader, errorHandler));
		reader.checkForUnusedFiles(unusedFiles ->
				TTMPDiscordBot.LOGGER.info("{} files were ignored since no valid function was found: \n    {}",
						unusedFiles.size(), String.join("\n    ", unusedFiles)));
	}

	@Override protected Object[] getArgs(){
		return new Object[]{TTMPDiscordBot.get(), Activity.class};
	}
}

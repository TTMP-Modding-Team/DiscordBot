package tictim.ttmpdiscordbot.api.botcommand;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public interface BotCommand{
	List<String> signatures();
	@Nullable Pattern arguments();
	List<String> usages();
	String description();
	Scope getScope();
	boolean isSilent();

	void execute(BotCommandContext context);

	enum Scope{
		EVERYWHERE,
		MINECRAFT,
		DISCORD;

		public boolean canBeExecutedInMinecraft(){
			return this==EVERYWHERE||this==MINECRAFT;
		}
		public boolean canBeExecutedInDiscord(){
			return this==EVERYWHERE||this==DISCORD;
		}
		public boolean containsScope(Scope scope){
			return this==EVERYWHERE||this==scope;
		}
	}
}

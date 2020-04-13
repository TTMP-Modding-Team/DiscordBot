package tictim.ttmpdiscordbot.api;

import net.minecraftforge.eventbus.api.Event;

/**
 * DiscordBotEvent is forge events fired by bots. Instance of the bot can be fetched by method.
 */
public abstract class DiscordBotEvent extends Event{
	private final Bot bot;

	public DiscordBotEvent(Bot bot){
		this.bot = bot;
	}

	public Bot bot(){
		return bot;
	}

	/**
	 * Fired after successful
	 */
	public static class Connected extends DiscordBotEvent{
		public Connected(Bot bot){
			super(bot);
		}
	}
	public static class Disconnected extends DiscordBotEvent{
		public Disconnected(Bot bot){
			super(bot);
		}
	}
}

package tictim.ttmpdiscordbot.api.config;

public interface GeneralSettings{
	String botToken();
	boolean retryOnTimeout();
	int maxReconnectDelay();
	/**
	 * Message containing this much or more line breaks will displayed as [TL;DR]. Set 0 or less to disable this feature.
	 */
	int tldrLineBreak();
	/**
	 * Message containing this much or more characters will displayed as [TL;DR]. Set 0 or less to disable this feature.
	 */
	int tldrCharacters();
}

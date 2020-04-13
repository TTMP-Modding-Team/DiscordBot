package tictim.ttmpdiscordbot.config;

import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.config.GeneralSettings;
import tictim.ttmpdiscordbot.api.config.SavableConfigType;

@SuppressWarnings("FieldCanBeLocal")
public final class GeneralSettingsImpl implements GeneralSettings{
	private String botToken = "";

	// TODO Activity
	// Discord Settings

	private boolean retryOnTimeout = true;
	private int maxReconnectDelay = 900;

	private int tldrLineBreak = 3;
	private int tldrCharacters = 80;

	@Override
	public String botToken(){
		return botToken;
	}
	@Override
	public boolean retryOnTimeout(){
		return retryOnTimeout;
	}
	@Override
	public int maxReconnectDelay(){
		return maxReconnectDelay;
	}
	@Override
	public int tldrLineBreak(){
		return tldrLineBreak;
	}
	@Override
	public int tldrCharacters(){
		return tldrCharacters;
	}

	public void setBotToken(String botToken){
		this.botToken = botToken!=null ? botToken : "";
		save();
	}
	public void setRetryOnTimeout(boolean retryOnTimeout){
		this.retryOnTimeout = retryOnTimeout;
		save();
	}
	public void setMaxReconnectDelay(int maxReconnectDelay){
		this.maxReconnectDelay = maxReconnectDelay;
		save();
	}
	public void setTldrLineBreak(int tldrLineBreak){
		this.tldrLineBreak = tldrLineBreak;
		save();
	}
	public void setTldrCharacters(int tldrCharacters){
		this.tldrCharacters = tldrCharacters;
		save();
	}

	private void save(){
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		bot.saveConfig(SavableConfigType.GENERAL, bot::defaultErrorHandler);
	}
}

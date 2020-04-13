package tictim.ttmpdiscordbot.api.config;

public enum SavableConfigType{
	GENERAL,
	USERS,
	CHANNELS,
	LOCALIZATIONS,
	ALL;
	
	public boolean contains(SavableConfigType type){
		return this==type||this==ALL;
	}
}

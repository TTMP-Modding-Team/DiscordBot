package tictim.ttmpdiscordbot.api.config;

public enum LoadableConfigType{
	GENERAL,
	USERS,
	CHANNELS,
	COMMANDS,
	LOCALIZATIONS,
	ACTIVITY,
	ALL;

	public boolean contains(LoadableConfigType type){
		return this==type||this==ALL;
	}
}

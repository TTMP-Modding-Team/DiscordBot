/**
 * activity.js can be used for customizing presence(a.k.a. 'activity') of the bot.
 * For handling forge events using JavaScript, see events.js on commands.
 */

var numberOfPlayers = 0;

function updatePlayerActivity(bot, Activity){
	if(bot.isConnected()){
		var utils = Java.type("tictim.ttmpdiscordbot.api.Utils")
		var players = utils.server().players().size();
		if(players!=numberOfPlayers){
			numberOfPlayers = players;
			bot.getJDA().getPresence().setActivity(Java.type(Activity.getName()).playing("with "+players+" players"));
		}
	}
}

function events(){
	return [
		{
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent",
			/**
			 * Event handlers at activity directory receives additional parameter for convenience.
			 */
			"function": function(event, bot, Activity){
				updatePlayerActivity(bot, Activity);
			}
		},
		{
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent",
			"function": function(event, bot, Activity){
				updatePlayerActivity(bot, Activity);
			}
		},
		{
			"class": "tictim.ttmpdiscordbot.api.DiscordBotEvent$Connected",
			"function": function(event, bot, Activity){
				updatePlayerActivity(bot, Activity);
			}
		}
	];
}
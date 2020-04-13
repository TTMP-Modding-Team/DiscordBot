/**
 * activity.js에서는 봇의 presence(a.k.a. 'activity')를 커스터마이징할 수 있습니다.
 * JavaScript를 이용한 포지 이벤트의 핸들링에 대해서 더 알고 싶으시다면, commands 폴더의 events.js를 참조하십시오.
 */

var numberOfPlayers = 0;

function updatePlayerActivity(bot, Activity){
	if(bot.isConnected()){
		var utils = Java.type("tictim.ttmpdiscordbot.api.Utils")
		var players = utils.server().players().size();
		if(players!=numberOfPlayers){
			numberOfPlayers = players;
			bot.getJDA().getPresence().setActivity(Java.type(Activity.getName()).playing(players+"명이 서버에서 게임을"));
		}
	}
}

function events(){
	return [
		{
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent",
			/**
			 * activity 디렉토리의 이벤트 핸들러들은 편의를 위해 인자를 하나 더 받습니다.
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
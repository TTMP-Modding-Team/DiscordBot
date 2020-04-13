/**
 * 곧바로 사용 가능한 쓸모 있는 커맨드입니다.
 * 바꿔야 하는 부분은 쓸모 없는 텍스트로 채워져 있는 !서버 커맨드의 내용뿐입니다.
 */

function help(context, scope){
	var functions = []
	var commands = context.bot().botCommands().commands()
	for(var i = 0;i<commands.size();i++){
		var c = commands.get(i)
		if(c.getScope().containsScope(scope)){
			for(var j = 0;j<c.usages().size();j++) functions.push("**"+c.usages().get(j)+"**")
			functions.push('  '+c.description())
		}
	}
	context.respond(functions.join('\n'))
}

var Scope = Java.type("tictim.ttmpdiscordbot.api.botcommand.BotCommand.Scope")

function commands(){
	return [
		{
			'signatures': [
				'server',
				'서버'
			],
			'isSilent': true,
			'description': '서버의 정보를 출력합니다.',
			'scope': 'discord',
			'function': function(context){
				// !서버 커맨드에 내용을 채우기 전에, 여기서부터...

				function roll(min, max){ return Math.floor(Math.random()*(max-min+1))+min; }

				var year = roll(1000, 6000)
				var hoursLeft = roll(4, 24)
				var hgxwch = roll(0, year)
				
				var amountOfMail = roll(21314213123, 415412342341232143123);
				var luckYear = roll(1, 3);

				// ...여기까지의 내용을 지우세요.

				context.respond("안녕하십니까. TTMP 디스코드 봇의 제작자이자 여러분의 영원한 아바타 Tictim입니다. 이 메시지는 기원전 "+year+"년 영국에서부터 시작되었으며, 이 커맨드가 입력된 서버는 "+hoursLeft+"시간 내에 `config/ttmpdiscordbot/commands/standard.js`에 있는 이 글을 지우고 진짜 서버가 뭘 하고 있는지에 대한 정보를 입력해 놓지 않으면 망하게 됩니다. 영국에서 HGXWCH이라는 사람은 기원후 "+hgxwch+"년에 이 메시지를 받았습니다. 그는 비서에게 복사해서 보내라고 했습니다. 며칠 뒤에 복권이 당첨되어 "+roll(-150, 20)+"억을 받았습니다. 어떤 이는 이 편지를 받았으나 "+roll(5, 128)+"시간 이내 자신의 손에서 떠나야 한다는 사실을 잊었습니다. 그는 곧 사직되었습니다. 나중에야 이 사실을 알고 "+amountOfMail+"통의 편지를 보냈는데 다시 좋은 직장을 얻었습니다. 미국의 케네디 대통령은 이 편지를 받았지만 그냥 버렸습니다. 결국 "+roll(1243214123, 1452347465776543)+"일 후 그는 암살 당했습니다. 기억해 주세요. 이 편지를 보내면 "+luckYear+"년의 행운이 있을 것이고 그렇지 않으면 "+roll(0, 1231211)+"년의 불행이 있을 것입니다. 그리고 이 편지를 버리거나 낙서를 해서는 절대로 안됩니다. "+amountOfMail+"통입니다. 이 편지를 받은 사람은 행운이 깃들 것입니다. 힘들겠지만 좋은게 좋다고 생각하세요. "+luckYear+"년의 행운을 빌면서...")
			}
		},
		{
			'signatures': [
				'help',
				'commands',
				'도움',
				'도움말',
				'명령어'
			],
			'isSilent': true,
			'description': '모든 커맨드와 사용법을 출력합니다.',
			'scope': 'discord',
			'function': function(context){
				help(context, Scope.DISCORD)
			}
		},
		{
			'signatures': [
				'help',
				'commands',
				'도움',
				'도움말',
				'명령어'
			],
			'isSilent': true,
			'description': '모든 커맨드와 사용법을 출력합니다.',
			'scope': 'minecraft',
			'function': function(context){
				help(context, Scope.MINECRAFT)
			}
		},
		{
			'signatures': [
				'rememberme',
				'rememberMe'
			],
			'isSilent': true,
			'arguments': '(\\S(?:.+\\S)?)',
			'usages': [
				'rememberme <디스코드 유저>',
				'rememberMe <디스코드 유저>',
			],
			'description': '입력받은 디스코드 유저와 마인크래프트 유저를 연동시킵니다.',
			'scope': 'minecraft',
			'function': function(context){
				var username = context.argument(0)
				var users = context.jda().getUsersByName(username, true)
				var user;
				
				switch(users.size()){
					case 0:
						context.respond('유저 '+username+'를 찾을 수 없습니다.')
						return;
					case 1:
						user = users.get(0);
						break;
					default:
						var users2 = context.jda().getUsersByName(username, false)
						if(users2.size()==1){
							user = users2.get(0)
							break;
						}
						context.respond('이름 '+username+'은 다른 '+users.size()+'명의 유저 간 사용되어 유저를 확정할 수 없습니다.')
						return;
				}
				context.bot().userSettings().add(user, context.senderAsProfile())
				context.respond('당신은 이제부터 '+user.getName()+"입니다.");
			}
		},
		{
			'signatures': [
				'tellme',
				'tellMe'
			],
			'isSilent': true,
			'description': '당신이 연동된 마인크래프트/디스코드 유저를 출력합니다.',
			'function': function(context){
				if(context.senderAsMember()===null){
					var u = context.senderAsUser()
					context.respond(u===null? '당신이 누군지 알 수 없습니다. !rememberme를 사용하여 알려주세요.' : '당신은 '+u.getName()+"입니다.")
				}else{
					var profile = context.senderAsProfile()
					context.respond(profile===null? '당신이 누군지 알 수 없습니다. !rememberme를 사용하여 알려주세요.' : '당신은 '+profile+"입니다.")
				}
			}
		},
		{
			'signatures': [
				'forgetme',
				'forgetMe'
			],
			'isSilent': true,
			'description': '유저 간 연동을 끊습니다.',
			'function': function(context){
				if(context.senderAsMember()===null){
					if(context.bot().userSettings().removeProfile(context.senderAsProfile())) context.respond('유저 간 연동을 삭제했습니다.')
					else context.respond('당신과 연동된 유저가 없습니다!')
				}else{
					if(context.bot().userSettings().removeUser(context.senderAsUser())) context.respond('유저 간 연동을 삭제했습니다.')
					else context.respond('당신과 연동된 유저가 없습니다!')
				}
			}
		},
		{
			'signatures': 'tps',
			'isSilent': true,
			'description': '서버의 상태를 출력합니다.',
			'function': function(context){
				context.executeMinecraftCommand('forge tps')
			}
		},
		{
			'signatures': [
				'isdaytime',
				'isDaytime',
				'밤낮'
			],
			'isSilent': true,
			'description': '오버월드가 낮인지에 대한 여부를 출력합니다.',
			'function': function(context){
				if(context.server().overworld().isDaytime()) context.respond('낮이에요!')
				else context.respond('밤이에요.')
			}
		},
		{
			'signatures': [
				'online',
				'players',
				'온라인',
				'플레이어'
			],
			'isSilent': true,
			'description': '서버에 있는 플레이어를 출력합니다.',
			'function': function(context){
				var players = context.server().players()
				switch(players.size()){
				case 0:
					context.respond("서버에 아무도 없습니다.")
					break;
				case 1:
					context.respond("한 명의 플레이어가 있습니다.\n```"+players.get(0).name()+"```")
					break;
				default:
					var response = [players.size()+"명의 플레이어가 있습니다.\n```"]
					for(var i = 0;i<players.size();i++) response.push(players.get(i).name()).push('\n')
					context.respond(response.join(''))
				}
			}
		}
	]
}

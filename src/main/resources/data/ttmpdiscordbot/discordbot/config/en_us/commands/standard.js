/*
 * Ready-to-use set of default commands that might be useful to most of the servers.
 * !server command is the part only you need to modify, it is filled with placeholder text now.
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
			'signatures': 'server',
			'isSilent': true,
			'description': 'Displays information about server.',
			'scope': 'discord',
			'function': function(context){
				context.respond('Hey, Vsauce! Michael here. Server mods, did you know you can modify some text in `config/ttmpdiscordbot/commands/standard.js` to describe what this server is actually doing, rather than letting user hear stupid Vsauce meme when they asks for some info about server?')
			}
		},
		{
			'signatures': [
				'help',
				'commands'
			],
			'isSilent': true,
			'description': 'Lists all commands and its usage.',
			'scope': 'discord',
			'function': function(context){
				help(context, Scope.DISCORD)
			}
		},
		{
			'signatures': [
				'help',
				'commands'
			],
			'isSilent': true,
			'description': 'Lists all commands and its usage.',
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
			'arguments': '(\\S+)',
			'usages': [
				'rememberme <Username>',
				'rememberMe <Username>',
			],
			'description': 'Link given minecraft user and discord user.',
			'scope': 'discord',
			'function': function(context){
				var username = context.argument(0)
				var profile = context.server().findProfile(username);
				if(profile===null) context.respond('Couldn\'t find player with username '+username+'.');
				else{
					context.bot().userSettings().add(context.senderAsUser(), profile)
					context.respond('You are now: '+profile);
				}
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
				'rememberme <Discord User>',
				'rememberMe <Discord User>',
			],
			'description': 'Link given discord user and minecraft user.',
			'scope': 'minecraft',
			'function': function(context){
				var username = context.argument(0)
				var users = context.jda().getUsersByName(username, true)
				var user;
				
				switch(users.size()){
					case 0:
						context.respond('Couldn\'t find user '+username+'.')
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
						context.respond('Username '+username+' is ambiguous between '+users.size()+' users.')
						return;
				}
				context.bot().userSettings().add(user, context.senderAsProfile())
				context.respond('You are now: '+user.getName());
			}
		},
		{
			'signatures': [
				'tellme',
				'tellMe'
			],
			'isSilent': true,
			'description': 'Tells your current minecraft/discord user.',
			'function': function(context){
				if(context.senderAsMember()===null){
					var u = context.senderAsUser()
					context.respond(u===null? 'Sorry, couldn\'t find who you are. Use !rememberme to tell me.' : 'You are: '+u.getName())
				}else{
					var profile = context.senderAsProfile()
					context.respond(profile===null? 'Sorry, couldn\'t find who you are. Use /rememberme to tell me.' : 'You are: '+profile)
				}
			}
		},
		{
			'signatures': [
				'forgetme',
				'forgetMe'
			],
			'isSilent': true,
			'description': 'Removes link with user.',
			'function': function(context){
				if(context.senderAsMember()===null){
					if(context.bot().userSettings().removeProfile(context.senderAsProfile())) context.respond('Removed user integration.')
					else context.respond('You don\'t have user integration to remove!')
				}else{
					if(context.bot().userSettings().removeUser(context.senderAsUser())) context.respond('Removed user integration.')
					else context.respond('You don\'t have user integration to remove!')
				}
			}
		},
		{
			'signatures': 'tps',
			'isSilent': true,
			'description': 'Displays current status of the server.',
			'function': function(context){
				context.executeMinecraftCommand('forge tps')
			}
		},
		{
			'signatures': [
				'isdaytime',
				'isDaytime'
			],
			'isSilent': true,
			'description': 'Tells whether or not overworld is currently daytime.',
			'function': function(context){
				if(context.server().overworld().isDaytime()) context.respond('Yes it is!')
				else context.respond('No, it\'s not.')
			}
		},
		{
			'signatures': [
				'online',
				'players'
			],
			'isSilent': true,
			'description': 'Lists currently active players in the server.',
			'function': function(context){
				var players = context.server().players()
				switch(players.size()){
				case 0:
					context.respond("No one is in the server.")
					break;
				case 1:
					context.respond("One player is in the server.\n```"+players.get(0).name()+"```")
					break;
				default:
					var response = [players.size()+" players are in the server.\n```"]
					for(var i = 0;i<players.size();i++) response.push(players.get(i).name()).push('\n')
					context.respond(response.join(''))
				}
			}
		}
	]
}

function commands(){ // Commands are fetched by function commands() in top level.
	return [
		{
			/*
			 * Signatures are header of the command. It cannot have whitespace, and all commands requires at least one signature.
			 * Adding duplicated signatures is okay, except one that read first is always called and the other don't.
			 * Unless you specify different arguments to differentiate commands. To see examples of arguments, see below.
			 */
			'signatures': 'ping',
			/*
			 * Description does basically nothing, but can be fetched by other commands. See !help on standard.js, for example.
			 */
			'description': 'Use this when you have no friend',
			/*
			 * Function is what this command does when executed. Java classes can be used. (see https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/javascript.html)
			 * But note that, since deobfuscated name is not accessible on runtime (since forge on 1.13+), you can't use class/method name forge uses OR name provided by Mojang.
			 * Because coding with obfuscated name is not a fun experience, TTMP Discord Bot provides wrapper for frequently used classes, like Player, World, Minecraft Server, and so on.
			 * See TTMP Discord Bot on GitHub (https://github.com/TTMP-Modding-Team/DiscordBot). You can also request support for another Minecraft classes you want.
			 */
			'function': function(context){
				/*
				 * Context contains information of sender, passed arguments, and some functions for utility.
				 * You can see full reference from GitHub (https://github.com/TTMP-Modding-Team/DiscordBot/blob/master/src/main/java/tictim/ttmpdiscordbot/api/botcommand/BotCommandContext.java).
				 */
				context.respond('Pong!') // Respond to sender
			}
		},
		{
			/*
			 * Double signatures! Either of the following input will work in exact same way:
			 * !pingme
			 * !pingMe
			 */
			'signatures': [
				'pingme',
				'pingMe'
			],
			'description': 'Use this when you feel lonely',
			'function': function(context){
				if(context.channel()===null) context.respond('Hey '+context.senderAsProfile().name()+'.')
				else context.respond('Hey '+context.senderAsMember().getAsMention()+'.')
			}
		},
		{
			'signatures': [
				'rickAstley',
				'rickastley'
			],
			/*
			 * Argument is Regular Expression checked during choosing command. Input doesn't matching arguments will ignored.
			 * You can specify groups for argument and fetch during execution, see below.
			 */
			'arguments': '([12345678])',
			/*
			 * Similar for description, it does nothing, but can be fetched for utility. See !help on standard.js, for example.
			 * When not explicitly defined, usages are equal to list of signatures.
			 * When explicitly defined, it requires at least one usage.
			 */
			'usages': [
				'rickAstley <1~8>',
				'rickastley <1~8>'
			],
			'description': 'Use this when you had a bad day',
			'function': function(context){
				var i = parseInt(context.argument(0), 10) // Fetches group #1 from arguments, then converts to number. The value is used below to select which message will be sent.
				switch(i){
					case 1:
						context.respond('I will never gonna give you up :heart:')
						break;
					case 2:
						context.respond("I'm never gonna let you down :heart:")
						break;
					case 3:
						context.respond("I'm never gonna run around :heart:")
						break;
					case 4:
						context.respond("I'm never gonna desert you :heart:")
						break;
					case 5:
						context.respond("I'm never gonna make you cry :heart:")
						break;
					case 6:
						context.respond("I'm never gonna say goodbye :heart:")
						break;
					case 7:
						context.respond("I'm never gonna tell a lie :heart:")
						break;
					default: // case 8:
						context.respond("I'm never gonna hurt you :heart:")
				}
			}
		},
		{
			/*
			 * More complex-y commands using randomly generated number
			 */
			'signatures': 'roll',
			'arguments': '\\b(d4|d6|d12|d20|d100|d6d6)\\b',
			'usages': [
				'roll d4',
				'roll d6',
				'roll d12',
				'roll d20',
				'roll d100',
				'roll d6d6'
			],
			'description': 'Rolls the dice',
			'function': function(context){
				function roll(min, max){ return Math.floor(Math.random()*(max-min+1))+min; }

				var dice = context.argument(0)
				if(dice=='d4') context.respond(context.senderName()+' rolled d4 and got '+roll(1, 4)+'.')
				else if(dice=='d6') context.respond(context.senderName()+' rolled d6 and got '+roll(1, 6)+'.')
				else if(dice=='d12') context.respond(context.senderName()+' rolled d12 and got '+roll(1, 12)+'.')
				else if(dice=='d20') context.respond(context.senderName()+' rolled d20 and got '+roll(1, 20)+'.')
				else if(dice=='d100') context.respond(context.senderName()+' rolled d100 and got '+roll(1, 100)+'.')
				else if(dice=='d6d6') context.respond(context.senderName()+' rolled two d6s and got '+(roll(1, 6)+roll(1, 6))+'.')
				else context.respond(context.senderName()+' rolled THE FORBIDDEN d-1 and BROKE THE UNIVERSE!!!')
			}
		}
	]
}
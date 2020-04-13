/**
 * TTMP 디스코드 봇을 사용하면 JS로 작성된 커맨드를 손쉽게 추가할 수 있습니다.
 * 커맨드는 commands() 함수를 통해 추가됩니다. commands() 함수는 하나의 커맨드 오브젝트, 혹은 커맨드 오브젝트의 배열을 반환해야 합니다.
 */
function commands(){
	return [
		{
			/**
			 * 시그니쳐는 커맨드의 맨 앞부분입니다. 모든 커맨드는 적어도 하나의 시그니쳐를 필요로 하며, 공백 문자를 포함할 수 없습니다.
			 * 중복된 시그니쳐를 가진 커맨드 또한 추가할 수 있습니다. 하지만 먼저 읽혀들여지는 쪽이 항상 호출된다는 사실을 고려해야 합니다.
			 * 각자 다른 매개변수를 지정하면 같은 시그니쳐의 커맨드를 구분할 수 있습니다. 매개변수에 대한 예제는 아래를 참조하십시오.
			 */
			'signatures': 'ping',
			/**
			 * 설명문은 아무런 역할도 하지 않지만, 다른 커맨드에 의해 읽혀질 수 있습니다. standard.js 파일 안의 !help 커맨드를 참조하십시오.
			 */
			'description': '친구가 없으면 사용하세요',
			/*
			 * 커맨드가 실행되었을 때 작동하는 코드입니다. Java의 클래스 또한 사용될 수 있습니다. (https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/javascript.html 참조)
			 * 하지만 마인크래프트 클래스의 경우 게임 실행 시 복호화된 이름을 사용하지 않기 때문에 (1.13+ 포지), 마인크래프트의 클래스/메소드의 경우 포지가 사용하는 이름도 Mojang이 공식으로 배포한 이름도 사용할 수 없습니다.
			 * 암호화된 이름을 사용하는 것은 매우 즐겁지 않기 때문에, TTMP 디스코드 봇은 자주 사용되는 클래스, 예를 들어 플레이어, 월드, 서버 등의 Wrapper를 제공합니다.
			 * TTMP 디스코드 봇의 GitHub 레포지토리를 참조하십시오 (https://github.com/TTMP-Modding-Team/DiscordBot). 여러분이 원하는 마인크래프트 클래스의 지원을 요청할 수도 있습니다.
			 */
			'function': function(context){
				/*
				 * Context는 발신자의 정보, 매개변수, 그리고 유틸리티 함수를 포함합니다.
				 * GitHub에서 레퍼런스를 확인할 수 있습니다 (https://github.com/TTMP-Modding-Team/DiscordBot/blob/master/src/main/java/tictim/ttmpdiscordbot/api/botcommand/BotCommandContext.java).
				 */
				context.respond('Pong!') // 발신자에게 응답
			}
		},
		{
			/**
			 * 한국어도 가능합니다. !핑  퐁!
			 */
			'signatures': '핑',
			'description': '이것도 친구가 없으면 사용하세요',
			'function': function(context){
				context.respond('퐁!')
			}
		},
		{
			/**
			 * 더블 시그니쳐! 다음 입력은 모두 같은 결과를 출력합니다:
			 * !pingme
			 * !pingMe
			 */
			'signatures': [
				'pingme',
				'pingMe'
			],
			'description': '외로울 때 사용하세요',
			'function': function(context){
				if(context.channel()===null) context.respond('야, '+context.senderAsProfile().name()+'.')
				else context.respond('야, '+context.senderAsMember().getAsMention()+'.')
			}
		},
		{
			'signatures': [
				'rickAstley',
				'rickastley'
			],
			/**
			 * 매개변수는 발동될 커맨드를 선택할 때 사용되는 정규 표현식입니다. 매개변수 조건을 만족하지 않는 인풋은 커맨드를 실행시키지 않습니다.
			 * 여러분은 매개변수에 그룹을 지정하여 커맨드의 실행 중 읽어들여 사용할 수 있습니다. 아래를 참조하세요.
			 */
			'arguments': '([12345678])',
			/**
			 * 사용 예제는 설명문과 비슷하게 아무 일도 하지 않지만, 다른 커맨드에 의해 읽혀질 수 있습니다. standard.js 파일 안의 !help 커맨드를 참조하십시오.
			 * 명시적으로 정의되지 않았을 경우, 사용 예제는 시그니쳐의 리스트와 동일합니다.
			 * 명시적으로 정의할 경우, 적어도 한 개의 예제를 필요로 합니다.
			 */
			'usages': [
				'rickAstley <1~8>',
				'rickastley <1~8>'
			],
			'description': '우울할 때 사용하세요',
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
			'signatures': [
				'roll',
				'주사위'
			],
			'arguments': '\\b(d4|d6|d12|d20|d100|d6d6)\\b',
			'usages': [
				'roll d4',
				'roll d6',
				'roll d12',
				'roll d20',
				'roll d100',
				'roll d6d6',
				'주사위 d4',
				'주사위 d6',
				'주사위 d12',
				'주사위 d20',
				'주사위 d100',
				'주사위 d6d6'
			],
			'description': '주사위를 굴립니다.',
			'function': function(context){
				function roll(min, max){ return Math.floor(Math.random()*(max-min+1))+min; }

				var dice = context.argument(0)
				if(dice=='d4') context.respond(context.senderName()+'가 d4를 굴려 '+roll(1, 4)+'이(가) 나왔습니다.')
				else if(dice=='d6') context.respond(context.senderName()+'가 d6을 굴려 '+roll(1, 6)+'이(가) 나왔습니다.')
				else if(dice=='d12') context.respond(context.senderName()+'가 d12를 굴려 '+roll(1, 12)+'이(가) 나왔습니다.')
				else if(dice=='d20') context.respond(context.senderName()+'가 d20을 굴려 '+roll(1, 20)+'이(가) 나왔습니다.')
				else if(dice=='d100') context.respond(context.senderName()+'가 d100을 굴려 '+roll(1, 100)+'이(가) 나왔습니다.')
				else if(dice=='d6d6') context.respond(context.senderName()+'가 d6 둘을 굴려 '+(roll(1, 6)+roll(1, 6))+'이(가) 나왔습니다.')
				else context.respond(context.senderName()+'가 전설의 d-1을 굴려 우주를 박살냈습니다!!!')
			}
		}
	]
}
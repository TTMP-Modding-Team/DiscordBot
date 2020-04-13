/**
 * TTMP 디스코드 봇이 읽어들이는 모든 종류의 JS 파일은 Forge 이벤트 핸들러를 등록할 수 있습니다.
 * 이벤트는 events() 함수를 통해 추가할 수 있습니다. events() 함수는 하나의 이벤트 핸들러 오브젝트, 혹은 이벤트 핸들러 오브젝트의 배열을 반환해야 합니다.
 * 
 * 이 시스템은 이벤트를 통해 구현될 수 있는 간단한 기능 - 예를 들어, 커스텀 메시지 - 의 구현을 위해 디자인되었으며, 복잡한 기능의 구현이나 자원 소모가 큰 작업에 사용되어서는 안 됩니다.
 */
function events(){
	return [
		{
			/**
			 * 이 이벤트 핸들러 오브젝트가 구독한 이벤트 클래스입니다.
			 * 이 이름은 클래스의 '바이너리명'을 필요로 합니다. 즉 이름에는 클래스의 이름은 물론 클래스가 속한 패키지명까지 기재되어야 합니다. 웹 검색을 통해 자바 클래스의 바이너리명에 대해서 더 찾아볼 수 있을 것입니다.
			 * 마지막으로 그다지 많이 쓰일 것은 아니고, 쓰여서도 아니 되는 것이지만, 구독한 이벤트에 제네릭 타입 필터링 또한 적용시킬 수 있습니다. 바보 같은 예제로 AttachCapabilitiesEvent가 있는데, 여러분은 World를 타겟으로 하는 AttachCapabilitiesEvent를 다음과 같은 방식으로 구독할 수 있습니다:
			 *    net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.World>
			 */
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent",
			/**
			 * 이 이벤트 핸들러 오브젝트의 함수 부분입니다. 디스코드 봇의 인스턴스가 두 번째 패러미터로 지원됩니다.
			 */
			"function": function(event, bot){
				if(bot.isShuttedDown()) return;
				var utils=Java.type("tictim.ttmpdiscordbot.api.Utils")
				bot.say(utils.translation("multiplayer.player.joined", utils.translation("**%s**", utils.wrap(event.getPlayer()).name())));
			}
			/**
			 * 이벤트 핸들러의 우선도 또한 지정할 수 있습니다. 값은 문자열이어야 하며, 아래 목록 중 하나의 값이어야 합니다:
			 *   highest,
			 *   high,
			 *   normal,
			 *   low,
			 *   또는 lowest.
			 * 해당 프로퍼티의 지정은 선택 사항입니다. 만약 프로퍼티의 값이 지정되지 않았다면, 값은 자동적으로 normal이 됩니다.
			 * 
			 * 참고사항으로, JavaScript 이벤트는 FMLServerStartedEvent 도중 로드되며 FMLServerStoppedEvent 도중 제거되기 때문에, 서버 로딩 이벤트와 함께 FMLServerStoppedEvent 이후의 이벤트에는 접근할 수 없습니다. 이러한 점 때문에, 서버 라이프사이클 이벤트는 사용하지 않는 것을 권장합니다.
			 */
			// , "priority": "low"
			/**
			 * 또한 이벤트 핸들러가 이미 취소된 이벤트 또한 수신할지에 대한 여부를 지정할 수 있습니다. 이 값은 true 혹은 false 중 하나여야 합니다.
			 * 해당 프로퍼티의 지정은 선택 사항입니다. 만약 프로퍼티의 값이 지정되지 않았다면, 값은 자동적으로 false가 됩니다.
			 */
			// , "receiveCancelled": true
		},
		{
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent",
			"function": function(event, bot){
				if(bot.isShuttedDown()) return;
				var utils=Java.type("tictim.ttmpdiscordbot.api.Utils")
				bot.say(utils.translation("multiplayer.player.left", utils.translation("**%s**", utils.wrap(event.getPlayer()).name())));
			}
		},
		{
			"class": "net.minecraftforge.event.entity.player.AdvancementEvent",
			"function": function(event, bot){
				if(bot.isShuttedDown()) return;
				var utils=Java.type("tictim.ttmpdiscordbot.api.Utils")
				var a = utils.wrap(event.getAdvancement());
				var d = a.display();
				if(d!=null) bot.say(new TranslationTextComponent("chat.type.advancement."+d.frame().name(), utils.wrap(event.getPlayer()).name(), a.displayText()));
			}
		}
	]
}
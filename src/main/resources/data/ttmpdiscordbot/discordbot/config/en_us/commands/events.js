/**
 * All JavaScript files Discord Bot read can subscribe to Forge EventBus.
 * Events are fetched by function events() in top level. events() function must return one event handler object, or array of event handler objects.
 * 
 * Note that this system is designed for small tasks such as customized message for certain situations that can be implemented using events, and NEVER designed for any complex/heavy operations.
 */
function events(){
	return [
		{
			/**
			 * The Event class this event handler is subscribed for.
			 * Note that the name needed for here is 'binary name' of the class, meaning it should contain all of the packages as well as classname. You can find more about binary name of the java classes on google.
			 * Lastly, although probably wouldn't be used frequently, plus shouldn't, but you can also specify generic type filter as well. One horrible example is AttachCapabilitiesEvent. You can subscribe to AttachCapabilitiesEvent targetting World specifically by this:
			 *    net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.World>
			 */
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent",
			/**
			 * Method part of the event handler object. Instance of the discord bot is passed by second parameter.
			 */
			"function": function(event, bot){
				if(bot.isShuttedDown()) return;
				var utils = Java.type("tictim.ttmpdiscordbot.api.Utils")
				bot.say(utils.translation("multiplayer.player.joined", utils.translation("**%s**", utils.wrap(event.getPlayer()).name())));
			}
			/**
			 * Priority of the event handler also can be specified. The value must be string, and must be one of the values below:
			 *   highest,
			 *   high,
			 *   normal,
			 *   low,
			 *   or lowest.
			 * Assigning this property is optional. If you don't specify this property, then it will be automatically set by normal.
			 * 
			 * Note that, since the JavaScript events are loaded during FMLServerStartedEvent and cleared during FMLServerStoppedEvent, you can't access any server-loading events, as well as events after FMLServerStoppedEvent. And because of that, using server lifecycle event is highly discouraged.
			 */
			// , "priority": "low"
			/**
			 * And you can also decide whether or not receive already cancelled events. The value must be either true or false.
			 * Assigning this property is optional. If you don't specify this property, then it will be automatically set by false.
			 */
			// , "receiveCancelled": true
		},
		{
			"class": "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent",
			"function": function(event, bot){
				if(bot.isShuttedDown()) return;
				var utils = Java.type("tictim.ttmpdiscordbot.api.Utils")
				bot.say(utils.translation("multiplayer.player.left", utils.translation("**%s**", utils.wrap(event.getPlayer()).name())));
			}
		},
		{
			"class": "net.minecraftforge.event.entity.player.AdvancementEvent",
			"function": function(event, bot){
				if(bot.isShuttedDown()) return;
				var utils = Java.type("tictim.ttmpdiscordbot.api.Utils")
				var a = utils.wrap(event.getAdvancement());
				var d = a.display();
				if(d!=null) bot.say(new TranslationTextComponent("chat.type.advancement."+d.frame().name(), utils.wrap(event.getPlayer()).name(), a.displayText()));
			}
		}
	]
}
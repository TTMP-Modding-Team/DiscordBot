package tictim.ttmpdiscordbot.api.botcommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tictim.ttmpdiscordbot.api.Bot;
import tictim.ttmpdiscordbot.api.wrapper.Profile;
import tictim.ttmpdiscordbot.api.wrapper.WPlayer;
import tictim.ttmpdiscordbot.api.wrapper.WServer;

import javax.annotation.Nullable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

public interface BotCommandContext{
	/**
	 * Currently executing command.
	 * @return Currently executing command.
	 */
	BotCommand command();
	/**
	 * Matcher for command arguments.
	 * @return Matcher for command arguments.
	 */
	Matcher arguments();
	/**
	 * Get command argument on specified index.<br/>
	 * <code>context.argument(0)</code> is equal to <code>arguments.group(1)</code>. <code>context.argument(1)</code> for <code>arguments.group(2)</code>, and so on.<br/>
	 * Note that the argument can be null if the group is optional(using ? after group), and cause 'ambiguous call' between <code>respond(String)</code> and <code>respond(ITextComponent)</code> error, if fed directly.
	 * @param index Argument index, or (group index)-1.
	 * @return Command argument on specified index.
	 */
	default String argument(int index){
		return arguments().group(index+1);
	}
	/**
	 * Original message invoked this command.
	 * @return Original message invoked this command.
	 */
	String originalMessage();
	
	/**
	 * Sender as discord member(user on guild). Nonnull if command was executed from discord channel, null otherwise.
	 * @return Sender as discord member.
	 */
	@Nullable
	Member senderAsMember();
	/**
	 * Sender as discord user. Nonnull if command was executed from discord channel, can be null otherwise.<br/>
	 * Note that the user is not always null even from minecraft chat, since you can actually tell the bot which user is you using minecraft command <code>/rememberme &lt;Discord User&gt</code>.
	 * @return Sender as discord user.
	 */
	@Nullable
	User senderAsUser();
	/**
	 * Sender as player. Nonnull if command was executed from minecraft chat, can be null otherwise.<br/>
	 * Note that the user is not always null even from discord channel, but you have to add user and player integration via <code>/rememberme &lt;Discord User&gt</code> and logged in the server.
	 * @return Sender as player entity.
	 */
	@Nullable
	WPlayer senderAsPlayer();
	/**
	 * Sender as game profile(name and UUID). Nonnull if command was executed from minecraft chat, can be null otherwise.<br/>
	 * Note that the user is not always null even from discord channel, since you can actually tell the bot which user is you using minecraft command <code>/rememberme &lt;Discord User&gt</code>.
	 * @return Sender as game profile.
	 */
	@Nullable
	Profile senderAsProfile();
	/**
	 * Discord Channel the message was sent. Nonnull if command was executed from discord channel, null otherwise.
	 * @return Discord Channel the message was sent.
	 */
	@Nullable
	TextChannel channel();

	/**
	 * @return Effective name of the sender (Name of the player if executed in game, username if executed from discord channel)
	 */
	String senderName();
	
	/**
	 * Instance of the TTMP Discord Bot.
	 * @return Instance of TTMP Discord Bot.
	 */
	Bot bot();
	/**
	 * Instance of the JDA.
	 * @return Instance of JDA.
	 */
	default JDA jda(){
		return bot().getJDA();
	}
	/**
	 * Instance of the Minecraft Server.<br/>
	 * The vanilla server instance is wrapped, since any fields/methods can't be accessed due to obfuscation.
	 * @return Instance of Minecraft Server.
	 */
	default WServer server(){
		return new WServer(ServerLifecycleHooks.getCurrentServer());
	}
	/**
	 * Respond to sender.<br/>
	 * If command was executed from discord channel, this method will send message to the channel, and minecraft chat with discord channel identifier.<br/>
	 * If command was executed from minecraft chat, this method will send message to minecraft, and all discord channels that can receive minecraft chat, with username of the bot.<br/>
	 * This operation can be changed to <code>respond(message, true)</code> if the command is silent.
	 * @param message Message to respond.
	 * @see BotCommandContext#respond(String, boolean)
	 */
	default void respond(String message){
		respond(message, command()!=null&&command().isSilent());
	}
	/**
	 * Respond to sender.<br/>
	 * If command was executed from discord channel, this method will send message to the channel, and minecraft chat with discord channel identifier.<br/>
	 * If command was executed from minecraft chat, this method will send message to minecraft, and all discord channels that can receive minecraft chat, with username of the bot.
	 * @param message Message to respond.
	 * @see BotCommandContext#respond(ITextComponent, boolean)
	 */
	default void respond(ITextComponent message){
		respond(message, command()!=null&&command().isSilent());
	}
	
	/**
	 * Respond to sender.<br/>
	 * If command was executed from discord channel, this method will send message to the channel, and minecraft chat with discord channel identifier.<br/>
	 * If command was executed from minecraft chat, this method will send message to minecraft, and all discord channels that can receive minecraft chat, with username of the bot.<br/>
	 * @param message Message to respond.
	 * @param silent  If true, messaging to other side is omitted.<br/>
	 *                For example, it will not send message to minecraft chat when executed from discord channel. And it will not send message to discord channels when executed from minecraft chat.
	 */
	void respond(String message, boolean silent);
	/**
	 * Respond to sender.<br/>
	 * If command was executed from discord channel, this method will send message to the channel, and minecraft chat with discord channel identifier.<br/>
	 * If command was executed from minecraft chat, this method will send message to minecraft, and all discord channels that can receive minecraft chat, with username of the bot.
	 * @param message Message to respond.
	 * @param silent  If true, messaging to other side is omitted.<br/>
	 *                For example, it will not send message to minecraft chat when executed from discord channel. And it will not send message to discord channels when executed from minecraft chat.
	 */
	void respond(ITextComponent message, boolean silent);
	
	/**
	 * Queue for execution of given minecraft command. Any result/error will be sent using <code>respond(message)</code>. Call <code>Future#get()</code> to wait for end of execution.
	 * @param command Minecraft command to execute.
	 * @see BotCommandContext#executeMinecraftCommand(String, boolean)
	 */
	default Future<Integer> executeMinecraftCommand(String command){
		return executeMinecraftCommand(command, command().isSilent());
	}
	/**
	 * Queue for execution of given minecraft command. Any result/error will be sent using <code>respond(message)</code>. Call <code>Future#get()</code> to wait for end of execution.
	 * @param command Minecraft command to execute.
	 * @param silent  If true, result/error will be sent using <code>respond(message, true)</code>.
	 */
	Future<Integer> executeMinecraftCommand(String command, boolean silent);
	/**
	 * Queue for execution of given minecraft command. Any result/error will be ignored. Call <code>Future#get()</code> to wait for end of execution.
	 * @param command Minecraft command to execute.
	 */
	Future<Integer> executeMinecraftCommandWithoutResponse(String command);
}

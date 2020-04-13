package tictim.ttmpdiscordbot.botcommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tictim.ttmpdiscordbot.api.Bot;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.wrapper.Profile;
import tictim.ttmpdiscordbot.api.wrapper.WPlayer;

import javax.annotation.Nullable;
import java.util.regex.Matcher;

public class IngameChatContext extends AbstractContext{
	private final String message;
	private final PlayerEntity player;
	public IngameChatContext(BotCommand command, Matcher args, String message, PlayerEntity player){
		super(command, args);
		this.message = message;
		this.player = player;
	}

	@Override
	public String originalMessage(){
		return message;
	}
	@Nullable
	@Override
	public Member senderAsMember(){
		return null;
	}
	@Nullable
	@Override
	public User senderAsUser(){
		return bot().userSettings().user(player);
	}
	@Nullable
	@Override
	public WPlayer senderAsPlayer(){
		return new WPlayer(player);
	}
	@Nullable
	@Override
	public Profile senderAsProfile(){
		return new Profile(player.getGameProfile());
	}
	@Nullable
	@Override
	public TextChannel channel(){
		return null;
	}
	@Override
	public String senderName(){
		return player.getGameProfile().getName();
	}

	@Override
	public void respond(String message, boolean silent){
		Bot bot = bot();
		User self = bot.getJDA().getSelfUser();
		bot.chat(message, self);
		if(!silent) bot.say("**<"+self.getName()+">** "+message);
	}
	@Override
	public void respond(ITextComponent message, boolean silent){
		Bot bot = bot();
		User self = bot.getJDA().getSelfUser();
		bot.chat(message, self);
		if(!silent) bot.say(new StringTextComponent("**<"+self.getName()+">** ").appendSibling(message));
	}

	// @see Entity#getCommandSource()
	@Override
	protected CommandSource createCommandSource(ICommandSource source){
		return new CommandSource(source,
				player.getPositionVec(),
				player.getPitchYaw(),
				player.world instanceof ServerWorld ? (ServerWorld)player.world : null,
				4,
				player.getGameProfile().getName(),
				player.getDisplayName(),
				ServerLifecycleHooks.getCurrentServer(),
				player);
	}

	@Override
	public String toString(){
		return String.format("IngameChatContext{message='%s', player=%s, command=%s, args=%s}",
				message,
				player,
				command,
				args);
	}
}

package tictim.ttmpdiscordbot.botcommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tictim.ttmpdiscordbot.api.Bot;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.wrapper.Profile;
import tictim.ttmpdiscordbot.api.wrapper.WPlayer;

import javax.annotation.Nullable;
import java.util.regex.Matcher;

public class TextChannelContext extends AbstractContext{
	private final Message message;

	public TextChannelContext(BotCommand command, Matcher args, Message message){
		super(command, args);
		this.message = message;
	}

	@Override
	public String originalMessage(){
		return message.getContentRaw();
	}
	@Override
	public Member senderAsMember(){
		return message.getMember();
	}
	@Override
	public User senderAsUser(){
		return message.getMember().getUser();
	}
	@Nullable
	@Override
	public WPlayer senderAsPlayer(){
		Profile profile = senderAsProfile();
		if(profile!=null){
			PlayerList pl = ServerLifecycleHooks.getCurrentServer().getPlayerList();
			if(profile.id()!=null){
				PlayerEntity p = pl.getPlayerByUUID(profile.id());
				if(p!=null) return new WPlayer(p);
			}
			PlayerEntity p = pl.getPlayerByUsername(profile.name());
			if(p!=null) return new WPlayer(p);
		}
		return null;
	}
	@Nullable
	@Override
	public Profile senderAsProfile(){
		return bot().userSettings().profile(senderAsMember());
	}
	@Override
	public TextChannel channel(){
		return message.getTextChannel();
	}
	@Override
	public String senderName(){
		return message.getMember().getEffectiveName();
	}

	@Override
	public void respond(String message, boolean silent){
		Bot bot = bot();
		bot.say(message, channel());
		if(!silent) bot.chat(message, bot.getJDA().getSelfUser(), channel());
	}
	@Override
	public void respond(ITextComponent message, boolean silent){
		Bot bot = bot();
		bot.say(message, channel());
		if(!silent) bot.chat(message, bot.getJDA().getSelfUser(), channel());
	}

	@Override
	protected CommandSource createCommandSource(ICommandSource source){
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		WPlayer player = senderAsPlayer();
		return new CommandSource(source,
				Vec3d.ZERO,
				Vec2f.ZERO,
				player!=null ? player.world().unwrapped() instanceof ServerWorld ? (ServerWorld)player.world().unwrapped() : null : server.getWorld(DimensionType.OVERWORLD),
				4,
				senderAsMember().getEffectiveName(),
				new StringTextComponent(senderAsMember().getEffectiveName()),
				server,
				null);
	}

	@Override
	public String toString(){
		return String.format("TextChannelContext{message=%s, command=%s, args=%s}",
				message,
				command,
				args);
	}
}

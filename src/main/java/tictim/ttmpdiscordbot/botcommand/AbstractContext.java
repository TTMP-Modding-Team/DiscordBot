package tictim.ttmpdiscordbot.botcommand;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.botcommand.BotCommandContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

public abstract class AbstractContext implements BotCommandContext{
	protected final BotCommand command;
	protected final Matcher args;
	
	public AbstractContext(BotCommand command, Matcher args){
		this.command = command;
		this.args = args;
	}
	
	@Override
	public TTMPDiscordBot bot(){
		return TTMPDiscordBot.get();
	}
	
	@Override
	public BotCommand command(){
		return command;
	}
	@Override
	public Matcher arguments(){
		return args;
	}
	
	@Override
	public Future<Integer> executeMinecraftCommand(String command, boolean silent){
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return CompletableFuture.supplyAsync(() -> server.getCommandManager().handleCommand(createCommandSource(new ICommandSource(){
			@Override
			public void sendMessage(ITextComponent message){
				respond(message, silent);
			}
			@Override
			public boolean shouldReceiveFeedback(){
				return true;
			}
			@Override
			public boolean shouldReceiveErrors(){
				return true;
			}
			@Override
			public boolean allowLogging(){
				return false;
			}
		}), command), server);
	}
	@Override
	public Future<Integer> executeMinecraftCommandWithoutResponse(String command){
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return CompletableFuture.supplyAsync(() -> server.getCommandManager().handleCommand(createCommandSource(new ICommandSource(){
			@Override
			public void sendMessage(ITextComponent message){}
			@Override
			public boolean shouldReceiveFeedback(){
				return false;
			}
			@Override
			public boolean shouldReceiveErrors(){
				return false;
			}
			@Override
			public boolean allowLogging(){
				return false;
			}
		}), command), server);
	}
	
	protected abstract CommandSource createCommandSource(ICommandSource source);
}

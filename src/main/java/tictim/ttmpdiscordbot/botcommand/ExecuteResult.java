package tictim.ttmpdiscordbot.botcommand;

import net.minecraft.util.text.TranslationTextComponent;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.botcommand.BotCommandContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ExecuteResult{
	private final String signature;
	private final String arguments;

	private BotCommandContext context;
	private List<BotCommand> mismatchedArguments;

	public ExecuteResult(String signature, @Nullable String arguments){
		this.signature = signature;
		this.arguments = arguments!=null ? arguments : "";
	}

	public String getSignature(){
		return signature;
	}
	public String getArguments(){
		return arguments;
	}

	public BotCommandContext getContext(){
		return context;
	}
	public boolean isValid(){
		return context!=null&&context.command()!=null&&context.arguments()!=null;
	}
	public boolean isSilent(){
		return context!=null&&context.command()!=null&&context.command().isSilent();
	}

	public void setContext(BotCommandContext context){
		this.context = context;
	}

	public List<BotCommand> mismatchedArguments(){
		if(mismatchedArguments==null) mismatchedArguments = new ArrayList<>();
		return mismatchedArguments;
	}

	public boolean hasMismatchingArguments(){
		return mismatchedArguments!=null&&!mismatchedArguments.isEmpty();
	}

	public void runCommand(Consumer<Runnable> run){
		if(isValid()){
			run.accept(() -> {
				// TTMPDiscordBot.LOGGER.debug("Executing discord command with context {}", context);
				context.command().execute(context);
			});
		}else if(context!=null){
			if(hasMismatchingArguments()){
				getContext().respond(new TranslationTextComponent("discord.command.argumentMismatch",
						"\n  "+mismatchedArguments().stream().flatMap(c -> c.usages().stream()).filter(c -> c.startsWith(signature)).collect(Collectors.joining("\n  "))));
			}else getContext().respond(new TranslationTextComponent("discord.command.unknown"));
		}
	}
}

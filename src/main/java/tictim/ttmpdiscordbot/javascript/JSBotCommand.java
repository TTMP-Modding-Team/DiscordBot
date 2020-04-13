package tictim.ttmpdiscordbot.javascript;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.minecraft.util.text.TranslationTextComponent;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.botcommand.BotCommand;
import tictim.ttmpdiscordbot.api.botcommand.BotCommandContext;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tictim.ttmpdiscordbot.javascript.JSParseUtils.toList;

public final class JSBotCommand implements BotCommand{
	private static final Matcher SIGNATURES = Pattern.compile("^\\S+").matcher("");

	private final List<String> signatures;
	@Nullable private final Pattern arguments;
	private final List<String> usages;
	private final String description;
	private final JSMethod function;
	private final Scope scope;
	private final boolean isSilent;

	public JSBotCommand(ScriptObjectMirror instance){
		Object _signatures = instance.getMember("signatures");
		if(_signatures instanceof ScriptObjectMirror){
			signatures = toList((ScriptObjectMirror)_signatures, o -> (String)o);
		}else signatures = Collections.singletonList((String)_signatures);
		if(instance.hasMember("usages")){
			Object _usages = instance.getMember("usages");
			if(_usages instanceof ScriptObjectMirror){
				usages = toList((ScriptObjectMirror)_usages, o -> (String)o);
			}else usages = Collections.singletonList((String)_usages);
		}else usages = signatures;

		this.arguments = instance.hasMember("arguments") ? Pattern.compile((String)instance.getMember("arguments")) : null;
		this.description = (String)instance.getMember("description");
		this.function = new JSMethod((ScriptObjectMirror)instance.getMember("function"), instance);
		this.scope = instance.hasMember("scope") ? Scope.valueOf(((String)instance.getMember("scope")).toUpperCase()) : Scope.EVERYWHERE;
		this.isSilent = instance.hasMember("isSilent") ? (Boolean)instance.getMember("isSilent") : false;

		if(signatures.isEmpty()) throw new IllegalArgumentException("At least one signature must be provided.");
		for(String s: signatures) if(!SIGNATURES.reset(s).matches()) throw new IllegalArgumentException("Invalid signature '"+s+"'");
		if(usages!=null&&usages.isEmpty()) throw new IllegalArgumentException("At least one example must be provided, if explicitly stated.");
	}

	@Override public List<String> signatures(){
		return signatures;
	}
	@Override @Nullable public Pattern arguments(){
		return arguments;
	}
	@Override public List<String> usages(){
		return usages;
	}
	@Override public String description(){
		return description;
	}
	@Override public Scope getScope(){
		return scope;
	}
	@Override public boolean isSilent(){
		return isSilent;
	}

	@Override public void execute(BotCommandContext context){
		try{
			function.call(context);
		}catch(RuntimeException e){
			TTMPDiscordBot.get().errors.add(String.format("An error occurred during execution of command: %s", e));
			context.respond(new TranslationTextComponent("discord.command.error", e));
		}
	}

	@Override public String toString(){
		return String.format("JSBotCommand{signatures=[%s], arguments=%s, examples=[%s], description='%s'}",
				String.join(", ", signatures),
				arguments,
				String.join(", ", usages),
				description);
	}
}

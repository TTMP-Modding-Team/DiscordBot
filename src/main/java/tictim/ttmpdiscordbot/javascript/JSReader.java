package tictim.ttmpdiscordbot.javascript;

import com.google.common.base.Charsets;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import tictim.ttmpdiscordbot.TTMPDiscordBot;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static tictim.ttmpdiscordbot.javascript.JSParseUtils.toList;

/**
 * Reads .js files and automatically parses JS objects.
 */
public final class JSReader{
	private final List<Code> scripts = new ArrayList<>();

	public JSReader(){}
	public JSReader(File root){
		read(root);
	}

	public void read(File root){
		if(root.exists()){
			if(root.isFile()) readFile(root);
			else if(root.isDirectory()) readDirectory(root);
			else throw new IllegalArgumentException("A File that is not file, nor directory");
		}
	}

	private void readFile(File file){
		if(file.getName().toLowerCase().endsWith(".js")){
			TTMPDiscordBot.LOGGER.debug("Reading JavaScript file '{}'", file.getAbsolutePath());
			try{
				ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
				engine.eval(String.join("\n", Files.readAllLines(file.toPath(), Charsets.UTF_8)));
				scripts.add(new Code((Invocable)engine, file.getAbsolutePath()));
			}catch(ScriptException|IOException|RuntimeException ex){
				TTMPDiscordBot.LOGGER.error("Unable to read JavaScript file {} due to an exception: ", file.getAbsolutePath(), ex);
			}
		}
	}

	private void readDirectory(File directory){
		File[] files = directory.listFiles();
		if(files!=null) for(File f: files) read(f);
	}

	public <T> List<T> parse(String functionName, Function<ScriptObjectMirror, T> parser, Consumer<String> errorHandler){
		List<T> list = new ArrayList<>();
		for(Code code: scripts){
			try{
				Object commands = code.script.invokeFunction(functionName);
				if(commands instanceof ScriptObjectMirror){
					ScriptObjectMirror obj = (ScriptObjectMirror)commands;
					if(obj.isArray()) list.addAll(toList(obj, o -> parser.apply((ScriptObjectMirror)o)));
					else list.add(parser.apply(obj));
				}else throw new ScriptException("Wrong type of result");
				code.unused = false;
			}catch(NoSuchMethodException ignored){
			}catch(ScriptException|RuntimeException ex){
				errorHandler.accept(String.format("Unable to read function named '%s' at %s due to an exception: %s", functionName, code.path, ex));
			}
		}
		return list;
	}

	public void checkForUnusedFiles(Consumer<List<String>> messageHandler){
		List<String> unusedFiles = scripts.stream().filter(it -> it.unused).map(it -> it.path).collect(Collectors.toList());
		if(!unusedFiles.isEmpty()) messageHandler.accept(unusedFiles);
	}

	private static final class Code{
		private final Invocable script;
		private final String path;
		private boolean unused = true;

		private Code(Invocable script, String path){
			this.script = script;
			this.path = path;
		}

		@Override public String toString(){
			return "Code("+path+") : "+script;
		}
	}
}
package tictim.ttmpdiscordbot.config;

import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.javascript.JSEventHandler;
import tictim.ttmpdiscordbot.javascript.JSReader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JSEventParser{
	protected final List<JSEventHandler> eventHandlers = new ArrayList<>();

	protected int readEvents(JSReader reader, Consumer<String> errorHandler){
		List<JSEventHandler> eventHandlerList = reader.parse("events", instance -> {
			try{
				return new JSEventHandler(instance, this::getArgs);
			}catch(ClassNotFoundException e){
				throw new RuntimeException(e);
			}
		}, errorHandler);
		eventHandlers.addAll(eventHandlerList);
		return eventHandlerList.size();
	}

	public void subscribe(){
		for(JSEventHandler e: eventHandlers) e.subscribe();
	}
	public void unsubscribe(){
		for(JSEventHandler e: eventHandlers) e.unsubscribe();
	}

	public void clear(){
		unsubscribe();
		eventHandlers.clear();
	}

	protected Object[] getArgs(){
		return new Object[]{TTMPDiscordBot.get()};
	}
}

package tictim.ttmpdiscordbot.botcommand;

import tictim.ttmpdiscordbot.TTMPDiscordBot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CommandExecutor{
	private ExecutorService service;

	public boolean isValid(){
		return service!=null;
	}

	private void setup0(){
		service = Executors.newFixedThreadPool(3);
	}
	private void invalidate0(){
		service.shutdownNow();
		service = null;
	}

	public void setup(){
		synchronized(this){
			if(isValid()) invalidate0();
			setup0();
		}
	}
	public void reset(){
		synchronized(this){
			invalidate0();
			setup();
		}
	}
	public void invalidate(){
		synchronized(this){
			if(isValid()) invalidate0();
		}
	}
	public void submit(Runnable task){
		if(isValid()) synchronized(this){
			if(isValid()){
				service.submit(task);
				return;
			}
		}
		TTMPDiscordBot.LOGGER.error("CommandExecutor got requested to execute tasks while invalidated!");
	}
}

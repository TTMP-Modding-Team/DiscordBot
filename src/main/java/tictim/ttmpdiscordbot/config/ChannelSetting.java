package tictim.ttmpdiscordbot.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Objects;

public final class ChannelSetting{
	private boolean receiveDiscordChat;
	private boolean sendMinecraftChat;
	private boolean enableDiscordCommands;
	
	public ChannelSetting(boolean receiveDiscordChat, boolean sendMinecraftChat, boolean enableDiscordCommands){
		this.receiveDiscordChat = receiveDiscordChat;
		this.sendMinecraftChat = sendMinecraftChat;
		this.enableDiscordCommands = enableDiscordCommands;
	}
	
	public boolean receiveDiscordChat(){
		return receiveDiscordChat;
	}
	public boolean sendMinecraftChat(){
		return sendMinecraftChat;
	}
	public boolean enableDiscordCommands(){
		return enableDiscordCommands;
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		ChannelSetting that = (ChannelSetting)o;
		return receiveDiscordChat==that.receiveDiscordChat&&
				sendMinecraftChat==that.sendMinecraftChat&&
				enableDiscordCommands==that.enableDiscordCommands;
	}
	@Override
	public int hashCode(){
		return Objects.hash(receiveDiscordChat, sendMinecraftChat, enableDiscordCommands);
	}
	
	@Override
	public String toString(){
		return "ChannelSetting{receiveDiscordChat="+receiveDiscordChat+", sendMinecraftChat="+sendMinecraftChat+", enableDiscordCommands="+enableDiscordCommands+'}';
	}
	
	static final class Serializer implements JsonSerializer<ChannelSetting>, JsonDeserializer<ChannelSetting>{
		@Override
		public JsonElement serialize(ChannelSetting src, Type typeOfSrc, JsonSerializationContext context){
			JsonObject obj = new JsonObject();
			obj.addProperty("receiveDiscordChat", src.receiveDiscordChat);
			obj.addProperty("sendMinecraftChat", src.sendMinecraftChat);
			obj.addProperty("enableDiscordCommands", src.enableDiscordCommands);
			return obj;
		}
		@Override
		public ChannelSetting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
			JsonObject obj = json.getAsJsonObject();
			return new ChannelSetting(obj.get("receiveDiscordChat").getAsBoolean(), obj.get("sendMinecraftChat").getAsBoolean(), obj.get("enableDiscordCommands").getAsBoolean());
		}
	}
}

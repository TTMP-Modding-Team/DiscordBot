package tictim.ttmpdiscordbot.config;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.config.ChannelSettings;
import tictim.ttmpdiscordbot.api.config.SavableConfigType;

import java.lang.reflect.Type;
import java.util.Map;

public final class ChannelSettingsImpl implements ChannelSettings{
	private final LongObjectMap<ChannelSetting> channelSettings = new LongObjectHashMap<>();
	private ChannelSetting defaultChannelSetting = new ChannelSetting(true, false, true);
	
	public ChannelSetting getDefaultChannelSetting(){
		return defaultChannelSetting;
	}
	public void setDefaultChannelSetting(ChannelSetting defaultChannelSetting){
		this.defaultChannelSetting = Preconditions.checkNotNull(defaultChannelSetting);
	}
	
	public ChannelSetting getChannelSetting(TextChannel channel){
		ChannelSetting s = channelSettings.get(channel.getIdLong());
		return s!=null ? s : defaultChannelSetting;
	}
	
	public void addChannelSetting(TextChannel channel, ChannelSetting setting){
		addChannelSetting(channel.getIdLong(), setting);
	}
	public void addChannelSetting(long id, ChannelSetting setting){
		if(!channelSettings.containsKey(id)||!setting.equals(channelSettings.get(id))){
			channelSettings.put(id, setting);
			TTMPDiscordBot bot = TTMPDiscordBot.get();
			bot.saveConfig(SavableConfigType.CHANNELS, bot::defaultErrorHandler);
		}
	}
	
	public boolean canReceiveDiscordChat(TextChannel channel){
		return channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_READ)&&getChannelSetting(channel).receiveDiscordChat();
	}
	public boolean canSendMinecraftChat(TextChannel channel){
		return channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)&&getChannelSetting(channel).sendMinecraftChat();
	}
	public boolean isDiscordCommandsEnabled(TextChannel channel){
		return channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_READ)&&getChannelSetting(channel).enableDiscordCommands();
	}
	
	static final class Serializer implements JsonSerializer<ChannelSettingsImpl>, JsonDeserializer<ChannelSettingsImpl>{
		@Override
		public ChannelSettingsImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
			JsonObject obj = json.getAsJsonObject();
			ChannelSettingsImpl settings = new ChannelSettingsImpl();
			settings.defaultChannelSetting = context.deserialize(obj.get("defaultChannelSetting"), ChannelSetting.class);
			for(Map.Entry<String, JsonElement> e: obj.get("channelSettings").getAsJsonObject().entrySet()){
				settings.channelSettings.put(Long.parseUnsignedLong(e.getKey()), context.deserialize(e.getValue(), ChannelSetting.class));
			}
			return settings;
		}
		@Override
		public JsonElement serialize(ChannelSettingsImpl src, Type typeOfSrc, JsonSerializationContext context){
			JsonObject obj = new JsonObject();
			obj.add("defaultChannelSetting", context.serialize(src.defaultChannelSetting));
			JsonObject o2 = new JsonObject();
			for(LongObjectMap.PrimitiveEntry<ChannelSetting> e: src.channelSettings.entries()) o2.add(Long.toUnsignedString(e.key()), context.serialize(e.value()));
			obj.add("channelSettings", o2);
			return obj;
		}
	}
}

package tictim.ttmpdiscordbot.config;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.config.SavableConfigType;
import tictim.ttmpdiscordbot.api.config.UserSettings;
import tictim.ttmpdiscordbot.api.wrapper.Profile;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class UserSettingsImpl implements UserSettings{
	private final LongObjectMap<Profile> users = new LongObjectHashMap<>();
	
	@Override
	@Nullable
	public Profile profile(User user){
		return profile(user.getIdLong());
	}
	@Override
	@Nullable
	public Profile profile(Member member){
		return profile(member.getUser().getIdLong());
	}
	@Override
	@Nullable
	public Profile profile(long userId){
		return users.get(userId);
	}
	@Override
	@Nullable
	public User user(PlayerEntity player){
		return TTMPDiscordBot.get().getJDA().getUserById(userId(player));
	}
	@Nullable
	@Override
	public User user(GameProfile profile){
		return user(new Profile(profile));
	}
	@Override
	@Nullable
	public User user(Profile profile){
		return TTMPDiscordBot.get().getJDA().getUserById(userId(profile));
	}
	@Override
	@Nullable
	public User userById(UUID uid){
		return TTMPDiscordBot.get().getJDA().getUserById(userIdById(uid));
	}
	@Override
	@Nullable
	public User userByName(String name){
		return TTMPDiscordBot.get().getJDA().getUserById(userIdByName(name));
	}
	@Override
	@Nullable
	public Long userId(PlayerEntity player){
		return player.getGameProfile()!=null ? userId(player.getGameProfile()) : null;
	}
	@Override
	@Nullable
	public Long userId(Profile profile){
		if(profile.id()!=null){
			Long u = userIdById(profile.id());
			if(u!=null) return u;
		}
		return !StringUtils.isNotBlank(profile.name()) ? userIdByName(profile.name()) : null;
	}
	@Nullable
	@Override
	public Long userId(GameProfile profile){
		return userId(new Profile(profile));
	}
	@Override
	@Nullable
	public Long userIdById(UUID uid){
		Preconditions.checkNotNull(uid);
		for(LongObjectMap.PrimitiveEntry<Profile> e: users.entries()) if(Objects.equals(e.value().id(), uid)) return e.key();
		return null;
	}
	@Override
	@Nullable
	public Long userIdByName(String name){
		Preconditions.checkNotNull(name);
		for(LongObjectMap.PrimitiveEntry<Profile> e: users.entries()) if(Objects.equals(e.value().name(), name)) return e.key();
		return null;
	}
	
	@Override
	public boolean removeUser(User user){
		return removeUser(user.getIdLong());
	}
	@Override
	public boolean removeUser(Member member){
		return removeUser(member.getUser().getIdLong());
	}
	@Override
	public boolean removeUser(long userId){
		if(users.remove(userId)!=null){
			save();
			return true;
		}else return false;
	}
	@Override
	public boolean removeProfile(PlayerEntity player){
		return player.getGameProfile()!=null ? removeProfile(player.getGameProfile()) : null;
	}
	@Override
	public boolean removeProfile(GameProfile profile){
		return false;
	}
	@Override
	public boolean removeProfile(Profile profile){
		return (profile.id()!=null&&removeProfileById(profile.id()))||removeProfileByName(profile.name());
	}
	@Override
	public boolean removeProfileById(UUID uid){
		for(Iterator<LongObjectMap.PrimitiveEntry<Profile>> iterator = users.entries().iterator(); iterator.hasNext(); ){
			if(uid.equals(iterator.next().value().id())){
				iterator.remove();
				save();
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean removeProfileByName(String name){
		for(Iterator<LongObjectMap.PrimitiveEntry<Profile>> iterator = users.entries().iterator(); iterator.hasNext(); ){
			if(name.equals(iterator.next().value().name())){
				iterator.remove();
				save();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void add(User user, Profile profile){
		add(Preconditions.checkNotNull(user).getIdLong(), profile);
	}
	@Override
	public void add(long userId, Profile profile){
		profile = tryComplete(profile);
		if(profile.name()==null) TTMPDiscordBot.LOGGER.error("Couldn't fetch nickname for profile of user {}.", userId);
		else{
			Profile p2 = profile(userId);
			if(p2!=null) TTMPDiscordBot.LOGGER.warn("Duplicated user integration {}:{} will overwrite already existing user {}:{}.", userId, profile, userId, p2);
			users.put(userId, profile);
			save();
		}
	}
	
	private void save(){
		TTMPDiscordBot bot = TTMPDiscordBot.get();
		bot.saveConfig(SavableConfigType.USERS, bot::defaultErrorHandler);
	}
	
	private static Profile tryComplete(Profile profile){
		if(profile.name()==null){
			if(profile.id()!=null) return new Profile(ServerLifecycleHooks.getCurrentServer().getMinecraftSessionService().fillProfileProperties(profile.toGameProfile(), false));
		}else if(profile.id()==null){
			GameProfile p2 = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(profile.name());
			if(p2!=null) return new Profile(p2);
		}
		return profile;
	}
	
	static final class Serializer implements JsonSerializer<UserSettingsImpl>, JsonDeserializer<UserSettingsImpl>{
		@Override
		public UserSettingsImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
			UserSettingsImpl settings = new UserSettingsImpl();
			for(Map.Entry<String, JsonElement> e: json.getAsJsonObject().entrySet()){
				JsonObject o2 = e.getValue().getAsJsonObject();
				settings.users.put(Long.parseUnsignedLong(e.getKey()), new Profile(o2.get("name").getAsString(), UUID.fromString(o2.get("uuid").getAsString())));
			}
			return settings;
		}
		@Override
		public JsonElement serialize(UserSettingsImpl src, Type typeOfSrc, JsonSerializationContext context){
			JsonObject obj = new JsonObject();
			for(LongObjectMap.PrimitiveEntry<Profile> u: src.users.entries()){
				JsonObject o2 = new JsonObject();
				if(u.value().id()!=null) o2.addProperty("uuid", u.value().id().toString());
				if(u.value().name()!=null) o2.addProperty("name", u.value().name());
				obj.add(Long.toUnsignedString(u.key()), o2);
			}
			return obj;
		}
	}
}

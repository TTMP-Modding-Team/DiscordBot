package tictim.ttmpdiscordbot.api.config;

import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.entity.player.PlayerEntity;
import tictim.ttmpdiscordbot.api.wrapper.Profile;

import javax.annotation.Nullable;
import java.util.UUID;

public interface UserSettings{
	@Nullable
	Profile profile(User user);
	@Nullable
	Profile profile(Member member);
	@Nullable
	Profile profile(long userId);
	@Nullable
	User user(PlayerEntity player);
	@Nullable
	User user(GameProfile profile);
	@Nullable
	User user(Profile profile);
	@Nullable
	User userById(UUID uid);
	@Nullable
	User userByName(String name);
	@Nullable
	Long userId(PlayerEntity player);
	@Nullable
	Long userId(Profile profile);
	@Nullable
	Long userId(GameProfile profile);
	@Nullable
	Long userIdById(UUID uid);
	@Nullable
	Long userIdByName(String name);
	
	boolean removeUser(User user);
	boolean removeUser(Member member);
	boolean removeUser(long userId);
	boolean removeProfile(PlayerEntity player);
	boolean removeProfile(GameProfile profile);
	boolean removeProfile(Profile profile);
	boolean removeProfileById(UUID uid);
	boolean removeProfileByName(String name);
	
	void add(long userId, Profile profile);
	void add(User user, Profile profile);
}

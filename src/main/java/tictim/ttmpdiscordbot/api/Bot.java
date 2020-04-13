package tictim.ttmpdiscordbot.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import tictim.ttmpdiscordbot.api.config.*;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface Bot{
	@Nullable
	JDA getJDA();
	GeneralSettings generalSettings();
	UserSettings userSettings();
	ChannelSettings channelSettings();
	BotCommands botCommands();

	boolean isConnected();
	boolean isShuttedDown();

	boolean connect();
	boolean connectWithMessage(ITextComponent message);
	void disconnect();
	void disconnectWithMessage(ITextComponent message);

	default void say(ITextComponent message){
		say(message, null);
	}
	default void say(String message){
		say(message, null);
	}

	void say(ITextComponent message, @Nullable TextChannel channel);
	void say(String message, @Nullable TextChannel channel);

	void chat(Message message);

	default void chat(String message, Member member){
		chat(message, member, null);
	}
	default void chat(ITextComponent message, Member member){
		chat(message, member, null);
	}

	void chat(String message, Member member, @Nullable TextChannel channel);
	void chat(ITextComponent message, Member member, @Nullable TextChannel channel);

	default void chat(String message, User user){
		chat(message, user, null);
	}
	default void chat(ITextComponent message, User user){
		chat(message, user, null);
	}

	void chat(String message, User user, @Nullable TextChannel channel);
	void chat(ITextComponent message, User user, @Nullable TextChannel channel);

	default void chat(String message, String name){
		chat(message, name, null);
	}
	default void chat(ITextComponent message, String name){
		chat(message, name, null);
	}

	void chat(String message, String name, @Nullable TextChannel channel);
	void chat(ITextComponent message, String name, @Nullable TextChannel channel);

	default void chat(String message, Entity entity){
		chat(message, entity, null);
	}
	default void chat(ITextComponent message, Entity entity){
		chat(message, entity, null);
	}

	void chat(String message, Entity entity, @Nullable TextChannel channel);
	void chat(ITextComponent message, Entity entity, @Nullable TextChannel channel);

	default void chat(String message, ITextComponent name){
		chat(message, name, null);
	}
	default void chat(ITextComponent message, ITextComponent name){
		chat(message, name, null);
	}

	void chat(String message, ITextComponent name, @Nullable TextChannel channel);
	void chat(ITextComponent message, ITextComponent name, @Nullable TextChannel channel);

	void loadConfig(LoadableConfigType type, Consumer<String> errorHandler);
	void saveConfig(SavableConfigType type, Consumer<String> errorHandler);
}

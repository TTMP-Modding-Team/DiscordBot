package tictim.ttmpdiscordbot.api.config;

import net.dv8tion.jda.api.entities.TextChannel;

public interface ChannelSettings{
	boolean canReceiveDiscordChat(TextChannel channel);
	boolean canSendMinecraftChat(TextChannel channel);
	boolean isDiscordCommandsEnabled(TextChannel channel);
}

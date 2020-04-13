package tictim.ttmpdiscordbot.ast.node;

import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import java.util.Objects;

import static net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;

public class ChannelMentionNode extends Node<TextChannel>{
	private final String id;
	public ChannelMentionNode(String id){
		this.id = id;
	}

	@Override
	public void render(TextComponentBuilder builder, TextChannel renderContext){
		TextChannel ch = renderContext!=null ? renderContext.getGuild().getTextChannelById(id) : TTMPDiscordBot.get().getJDA().getTextChannelById(id);
		if(ch!=null){
			ITextComponent text = new StringTextComponent("#"+ch.getName());
			setEvent(text.getStyle().setBold(true).setColor(TextFormatting.BLUE), ch);
			builder.append(text);
		}else builder.append("#deleted-channel");
	}

	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		ChannelMentionNode nodes = (ChannelMentionNode)o;
		return id.equals(nodes.id);
	}
	@Override
	public int hashCode(){
		return Objects.hash(id);
	}

	public static void setEvent(Style style, TextChannel channel){
		style.setHoverEvent(TextComponentBuilder.hoverTextEvent(new TranslationTextComponent("discord.chat.channel"), new TranslationTextComponent("discord.chat.useShift")))
				.setClickEvent(new ClickEvent(SUGGEST_COMMAND, getChatToSuggestion(channel)))
				.setInsertion("#"+channel.getName());
	}

	private static String getChatToSuggestion(TextChannel channel){
		String fullName = channel.getGuild().getName()+"#"+channel.getName();
		return String.format("/chatTo \"%s\" ", fullName.indexOf(' ') >= 0 ? fullName : fullName);
	}
}

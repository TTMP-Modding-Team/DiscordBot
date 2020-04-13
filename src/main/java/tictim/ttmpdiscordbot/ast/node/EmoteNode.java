package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.util.text.*;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import java.util.Objects;

public class EmoteNode extends Node<TextChannel>{
	private final String name;
	private final String id;

	public EmoteNode(String name, String id){
		this.name = Preconditions.checkNotNull(name);
		this.id = Preconditions.checkNotNull(id);
	}

	@Override
	public void render(TextComponentBuilder builder, TextChannel renderContext){
		Emote emote = (renderContext!=null ? renderContext.getGuild().getJDA() : TTMPDiscordBot.get().getJDA()).getEmoteById(id);
		if(emote!=null){
			ITextComponent text = new StringTextComponent(":"+emote.getName()+":");
			setEvent(text.getStyle().setBold(true).setColor(TextFormatting.BLUE), emote);
			builder.append(text);
		}else builder.append(":"+name+":");
	}

	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		EmoteNode nodes = (EmoteNode)o;
		return name.equals(nodes.name)&&id.equals(nodes.id);
	}
	@Override
	public int hashCode(){
		return Objects.hash(name, id);
	}

	public static void setEvent(Style style, Emote emote){
		style.setHoverEvent(TextComponentBuilder.hoverTextEvent(new TranslationTextComponent("discord.chat.useShift"))).setInsertion(":"+emote.getName()+":");
	}
}

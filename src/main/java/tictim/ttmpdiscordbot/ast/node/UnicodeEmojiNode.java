package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import net.minecraft.util.text.*;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import java.util.Objects;

public class UnicodeEmojiNode<R> extends Node<R>{ // TODO Incomplete (e.g. Doesn't work well with emoji with multiple unicode characters)
	private final String emoji;
	public UnicodeEmojiNode(String emoji){
		this.emoji = Preconditions.checkNotNull(emoji);
	}
	
	@Override
	public void render(TextComponentBuilder builder, R renderContext){
		ITextComponent text = new StringTextComponent(":"+emoji+":");
		setEvent(text.getStyle().setBold(true).setColor(TextFormatting.BLUE), emoji);
		builder.append(text);
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		UnicodeEmojiNode<?> that = (UnicodeEmojiNode<?>)o;
		return emoji.equals(that.emoji);
	}
	@Override
	public int hashCode(){
		return Objects.hash(emoji);
	}
	
	public static void setEvent(Style style, String emoji){
		style.setHoverEvent(TextComponentBuilder.hoverTextEvent(new TranslationTextComponent("discord.chat.useShift"))).setInsertion(":"+emoji+":");
	}
	
}

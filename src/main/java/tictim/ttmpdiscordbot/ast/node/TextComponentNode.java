package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import net.minecraft.util.text.ITextComponent;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import java.util.Objects;

public class TextComponentNode<R> extends Node<R>{
	private final ITextComponent textComponent;
	public TextComponentNode(ITextComponent textComponent){
		this.textComponent = Preconditions.checkNotNull(textComponent);
	}
	
	@Override
	public void render(TextComponentBuilder builder, R renderContext){
		builder.append(textComponent.deepCopy());
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		TextComponentNode<?> that = (TextComponentNode<?>)o;
		return textComponent.equals(that.textComponent);
	}
	@Override
	public int hashCode(){
		return Objects.hash(textComponent);
	}
}

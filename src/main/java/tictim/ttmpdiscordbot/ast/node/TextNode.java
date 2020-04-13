package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import java.util.Objects;

/**
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public class TextNode<R> extends Node<R>{
	public final String content;
	
	public TextNode(String content){
		this.content = Preconditions.checkNotNull(content);
	}
	@Override
	public void render(TextComponentBuilder builder, R renderContext){
		builder.append(content);
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		TextNode<?> textNode = (TextNode<?>)o;
		return content.equals(textNode.content);
	}
	@Override
	public int hashCode(){
		return Objects.hash(content);
	}
}
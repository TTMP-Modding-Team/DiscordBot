package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;
import tictim.ttmpdiscordbot.ast.textcomponent.TextStyle;

import java.util.Objects;

/**
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public class StyleNode<R> extends Node<R>{
	private final TextStyle style;
	
	public StyleNode(TextStyle style){
		this.style = Preconditions.checkNotNull(style);
	}
	
	@Override
	public void render(TextComponentBuilder builder, R renderContext){
		if(getChildren()!=null){
			builder.addStyle(style);
			for(Node<R> node: getChildren()) node.render(builder, renderContext);
			builder.removeStyle(style);
		}
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		StyleNode<?> styleNode = (StyleNode<?>)o;
		return style.equals(styleNode.style);
	}
	@Override
	public int hashCode(){
		return Objects.hash(style);
	}
	
	/**
	 * Convenience method for creating a [StyleNode] when we already know what
	 * the text content will be.
	 */
	public static <R> StyleNode<R> createWithText(String content, TextStyle style){
		StyleNode<R> styleNode = new StyleNode<>(style);
		styleNode.addChild(new TextNode<>(content));
		return styleNode;
	}
}

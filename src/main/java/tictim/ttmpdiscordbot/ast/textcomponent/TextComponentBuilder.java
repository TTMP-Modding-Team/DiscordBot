package tictim.ttmpdiscordbot.ast.textcomponent;

import com.google.common.base.Preconditions;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

public final class TextComponentBuilder{
	public static TextComponentBuilder create(){
		return new TextComponentBuilder();
	}
	public static TextComponentBuilder create(ITextComponent root){
		return new TextComponentBuilder(root);
	}
	public static TextComponentBuilder createHoverTextBuilder(ITextComponent rootText){
		TextComponentBuilder builder = new TextComponentBuilder(rootText);
		rootText.getStyle().setHoverEvent(new HoverEvent(SHOW_TEXT, builder.currentTarget = new StringTextComponent("")));
		return builder;
	}
	
	private final ITextComponent root;
	private final List<TextStyle> activeStyles = new ArrayList<>();
	@Nullable
	private ITextComponent currentTarget;
	@Nullable
	private TextStyle currentClosureStyle;
	
	private TextComponentBuilder(){
		this(new StringTextComponent(""));
	}
	private TextComponentBuilder(ITextComponent root){
		this.root = root;
	}
	
	public void append(String string){
		Preconditions.checkNotNull(string);
		appendInternal(new StringTextComponent(string));
	}
	
	public void append(ITextComponent textComponent){
		Preconditions.checkNotNull(textComponent);
		appendInternal(textComponent.deepCopy());
	}
	
	private void appendInternal(ITextComponent textComponent){
		Style style = textComponent.getStyle();
		for(TextStyle ts: activeStyles) ts.applyStyle(style);
		getCurrentTarget().appendSibling(textComponent);
	}
	
	public void addStyle(TextStyle style){
		Preconditions.checkNotNull(style);
		if(!activeStyles.contains(style)){
			if(currentTarget==null){
				ITextComponent component = style.getEllipsis();
				if(component!=null){
					appendInternal(component);
					component.getStyle().setHoverEvent(new HoverEvent(SHOW_TEXT, currentTarget = new StringTextComponent("")));
					currentClosureStyle = style;
				}
			}
			activeStyles.add(style);
		}
	}
	
	public void removeStyle(TextStyle style){
		Preconditions.checkNotNull(style);
		if(activeStyles.remove(style)){
			if(currentClosureStyle==style){
				currentTarget = null;
				currentClosureStyle = null;
			}
		}
	}
	
	private ITextComponent getCurrentTarget(){
		return currentTarget==null ? root : currentTarget;
	}
	
	public ITextComponent build(){
		return root.deepCopy();
	}
	
	public static HoverEvent hoverTextEvent(ITextComponent... texts){
		switch(texts.length){
			case 0:
				return null;
			case 1:
				return new HoverEvent(SHOW_TEXT, texts[0]);
			default:{
				ITextComponent root = new StringTextComponent("");
				for(int i = 0; i<texts.length; i++){
					if(i>0) root.appendText("\n");
					root.appendSibling(texts[i]);
				}
				return new HoverEvent(SHOW_TEXT, root);
			}
		}
	}
}

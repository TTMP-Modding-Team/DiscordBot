package tictim.ttmpdiscordbot.ast.textcomponent;

import net.minecraft.util.text.Style;

import java.util.function.Consumer;

public class SimpleTextStyle implements TextStyle{
	private final Consumer<Style> styleConsumer;
	public SimpleTextStyle(Consumer<Style> styleConsumer){
		this.styleConsumer = styleConsumer;
	}
	
	@Override
	public void applyStyle(Style s){
		styleConsumer.accept(s);
	}
}

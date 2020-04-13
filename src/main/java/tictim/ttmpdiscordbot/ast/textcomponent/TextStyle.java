package tictim.ttmpdiscordbot.ast.textcomponent;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import javax.annotation.Nullable;

public interface TextStyle{
	void applyStyle(Style s);
	@Nullable
	default ITextComponent getEllipsis(){ return null; }
}

package tictim.ttmpdiscordbot.ast.textcomponent;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import javax.annotation.Nullable;

public class EllipsisStyle implements TextStyle{
	private final ITextComponent ellipsis;
	public EllipsisStyle(ITextComponent ellipsis){
		this.ellipsis = ellipsis;
	}
	
	@Override
	public void applyStyle(Style s){} // Do nothing
	@Nullable
	@Override
	public ITextComponent getEllipsis(){
		return ellipsis.deepCopy();
	}
}

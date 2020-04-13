package tictim.ttmpdiscordbot.api.wrapper;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

@WrapperClass(WAdvancement.class)
public final class WAdvancement extends Wrapper<Advancement>{
	public WAdvancement(Advancement advancement){
		super(advancement);
	}

	@Nullable public WAdvancement parent(){
		return t.getParent()==null ? null : new WAdvancement(t.getParent());
	}
	@Nullable public WDisplayInfo display(){
		return t.getDisplay()==null ? null : new WDisplayInfo(t.getDisplay());
	}
	public ITextComponent displayText(){
		return t.getDisplayText();
	}

	public static final class WDisplayInfo extends Wrapper<DisplayInfo>{
		public WDisplayInfo(DisplayInfo displayInfo){
			super(displayInfo);
		}

		public WFrameType frame(){
			return new WFrameType(t.getFrame());
		}

		public static final class WFrameType extends Wrapper<FrameType>{
			public WFrameType(FrameType frameType){
				super(frameType);
			}

			public String name(){
				return t.getName();
			}
			public TextFormatting getFormat(){
				return t.getFormat();
			}
		}
	}
}

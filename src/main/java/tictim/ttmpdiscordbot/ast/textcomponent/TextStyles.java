package tictim.ttmpdiscordbot.ast.textcomponent;

import net.minecraft.util.text.TranslationTextComponent;

public final class TextStyles{
	private TextStyles(){}
	
	public static final TextStyle BOLD = new SimpleTextStyle(s -> s.setBold(true));
	public static final TextStyle ITALIC = new SimpleTextStyle(s -> s.setItalic(true));
	public static final TextStyle UNDERLINED = new SimpleTextStyle(s -> s.setUnderlined(true));
	public static final TextStyle STRIKETHROUGH = new SimpleTextStyle(s -> s.setStrikethrough(true));
	public static final TextStyle SPOILER = new EllipsisStyle(new TranslationTextComponent("discord.chat.spoiler"));
	public static final TextStyle CODE_BLOCK = new EllipsisStyle(new TranslationTextComponent("discord.chat.codeBlock"));
}

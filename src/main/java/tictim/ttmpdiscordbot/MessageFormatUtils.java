package tictim.ttmpdiscordbot;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import tictim.ttmpdiscordbot.api.Bot;
import tictim.ttmpdiscordbot.config.L10n.L10nTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MessageFormatUtils{
	private MessageFormatUtils(){}

	private static final Matcher EMOJI_MATCH = Pattern.compile("^:([a-zA-Z0-9-_]+)(_tone[12345])?:").matcher("");

	public static String detectAndReplaceMentions(String message, @Nullable TextChannel channel, Bot bot){
		StringBuilder stb = new StringBuilder(message);
		L:
		for(int i = 0; i<stb.length(); i++){
			char c = stb.charAt(i);
			switch(c){
				case '@':
					if(!has(stb, i, "@everyone")&&!has(stb, i, "@here")){
						for(Member m: channel!=null ?
								channel.getMembers() :
								bot.getJDA().getTextChannelCache().stream().filter(bot.channelSettings()::canSendMinecraftChat).flatMap(g -> g.getMembers().stream()).collect(Collectors.toList())){
							String name = m.getEffectiveName();
							if(has(stb, i+1, name)){
								i = replace(stb, i, i+1+name.length(), m.getAsMention());
								continue L;
							}
						}// TODO possible 'misping' when the name is too easy to find like '@b'?
						for(Role r: channel!=null ? channel.getGuild().getRoleCache() : bot.getJDA().getRoleCache()){
							String name = r.getName();
							if(has(stb, i+1, name)){
								i = replace(stb, i, i+1+name.length(), r.getAsMention());
								continue L;
							}
						}
					}
					break;
				case '#':  // TODO channel names doesn't have any whitespace characters, any optimization idea?
					for(TextChannel ch: channel!=null ?
							channel.getGuild().getTextChannelCache() :
							bot.getJDA().getTextChannelCache()){
						String name = ch.getName();
						if(has(stb, i+1, name)) i = replace(stb, i, i+1+name.length(), ch.getAsMention());
					}
					break;
				case ':':
					if(EMOJI_MATCH.reset(stb).region(i, stb.length()).find()){
						String emojiName = EMOJI_MATCH.group(1);
						// Try search unicode emoji.
						for(Emoji e: EmojiManager.getAll()) if(e.getAliases().contains(emojiName)) continue L;

						String fullEmoteName = EMOJI_MATCH.groupCount() >= 3 ? emojiName+EMOJI_MATCH.group(2) : emojiName;
						if(channel!=null){
							Guild g = channel.getGuild();
							for(Emote e: g.getEmoteCache()){
								if(e.getName().equals(fullEmoteName)){
									i = replace(stb, EMOJI_MATCH.start(), EMOJI_MATCH.end(), e.getAsMention());
									continue L;
								}
							}
							for(Emote e: bot.getJDA().getEmoteCache()){
								if(e.getGuild()!=g&&e.getName().equals(fullEmoteName)){
									i = replace(stb, EMOJI_MATCH.start(), EMOJI_MATCH.end(), e.getAsMention());
									continue L;
								}
							}
						}else for(Emote e: bot.getJDA().getEmoteCache()){
							if(e.getName().equals(fullEmoteName)){
								i = replace(stb, EMOJI_MATCH.start(), EMOJI_MATCH.end(), e.getAsMention());
								continue L;
							}
						}
						// If not found, insert \ so it doesn't confuse the fuck out of parser
						stb.insert(i, '\\');
						i++;
					}
					break;
				case '\\':
					i++;
					break;
			}
		}
		return stb.toString();
	}

	private static boolean has(CharSequence string, int start, String match){
		if(string.length()<start+match.length()) return false;
		for(int i = 0; i<match.length(); i++) if(string.charAt(start+i)!=match.charAt(i)) return false;
		return true;
	}
	/**
	 * Replace and return next read point
	 * @return next read point
	 */
	private static int replace(StringBuilder stb, int start, int end, String string){
		stb.replace(start, end, string);
		return start+string.length()-1;
	}

	public static boolean isNormalText(ITextComponent text){
		Style style = text.getStyle();
		return (style.getClickEvent()==null||style.getClickEvent().getAction()==null)&&
				(style.getHoverEvent()==null||style.getHoverEvent().getAction()==null)&&
				style.getInsertion()==null;
	}

	public static String toDebugString(ITextComponent text){
		if(text instanceof TranslationTextComponent){
			return toDebugStringInternal((TranslationTextComponent)text);
		}else if(text instanceof L10nTextComponent){
			return toDebugStringInternal((L10nTextComponent)text);
		}else if(text instanceof StringTextComponent){
			return toDebugStringInternal((StringTextComponent)text);
		}else return text.getClass().getSimpleName()+defaultDebugString(text);
	}

	private static String toDebugStringInternal(TranslationTextComponent t){
		return "Translation("+t.getKey()+")"+defaultDebugString(t);
	}
	private static String toDebugStringInternal(L10nTextComponent t){
		return "L10n("+t.getKey()+")"+defaultDebugString(t);
	}
	private static String toDebugStringInternal(StringTextComponent t){
		return "Text("+t.getText()+")"+defaultDebugString(t);
	}

	private static String defaultDebugString(ITextComponent text){
		List<String> list = new ArrayList<>();
		if(!text.getStyle().isEmpty()) list.add(toDebugString(text.getStyle()));
		List<ITextComponent> sib = text.getSiblings();
		if(!sib.isEmpty()){
			if(!list.isEmpty()) list.add("");
			for(ITextComponent t: sib){
				list.add(toDebugString(t));
			}
		}
		if(list.isEmpty()) return "";
		return "{\n  "+tab(String.join("\n", list))+"\n}";
	}

	private static String toDebugString(Style s){
		String str = s.toString();
		List<String> list = new ArrayList<>();
		if(s.getColor()!=null) list.add("Color: "+s.getColor().name());

		for(StyleProperty p : StyleProperty.values()) if(p.m.reset(str).find()) list.add(StringUtils.capitalize(p.name())+": "+p.m.group(1));
		if(s.getClickEvent()!=null) list.add("ClickEvent: "+tab(toDebugString(s.getClickEvent())));
		if(s.getHoverEvent()!=null) list.add("HoverEvent: "+tab(toDebugString(s.getHoverEvent())));
		list.add("Insertion: "+s.getInsertion());
		return String.join("\n", list);
	}

	private static String toDebugString(ClickEvent event){
		return event.getAction()+"("+event.getValue()+")";
	}
	private static String toDebugString(HoverEvent event){
		return event.getAction()+"("+toDebugString(event.getValue())+")";
	}

	private static String tab(String s){
		return s.replace("\n", "\n  ");
	}

	@SuppressWarnings("unused")
	private enum StyleProperty{
		bold,
		italic,
		underlined,
		obfuscated;

		Matcher m;

		StyleProperty(){
			m = Pattern.compile(this.name()+"=\\b(true|false)\\b").matcher("");
		}
	}
}

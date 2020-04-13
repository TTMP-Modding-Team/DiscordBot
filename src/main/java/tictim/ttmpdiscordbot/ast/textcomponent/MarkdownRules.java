package tictim.ttmpdiscordbot.ast.textcomponent;

import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import tictim.ttmpdiscordbot.ast.node.*;
import tictim.ttmpdiscordbot.ast.parser.ParseSpec;
import tictim.ttmpdiscordbot.ast.parser.Parser;
import tictim.ttmpdiscordbot.ast.parser.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public final class MarkdownRules{
	private MarkdownRules(){}

	public static final Pattern PATTERN_BOLD = Pattern.compile("^\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)");
	public static final Pattern PATTERN_UNDERLINE = Pattern.compile("^__([\\s\\S]+?)__(?!_)");
	public static final Pattern PATTERN_STRIKETHROUGH = Pattern.compile("^~~(?=\\S)([\\s\\S]*?\\S)~~");
	public static final Pattern PATTERN_TEXT = Pattern.compile("^[\\s\\S]+?(?=[^0-9A-Za-z\\s\\u00c0-\\uffff]|\\n\\n| {2,}\\n|\\w+:\\S|$)");
	public static final Pattern PATTERN_ESCAPE = Pattern.compile("^\\\\([^0-9A-Za-z\\s])");
	public static final Pattern PATTERN_SPOILER = Pattern.compile("^\\|\\|([\\s\\S]+?)\\|\\|(?!\\|)");
	public static final Pattern PATTERN_CODE_BLOCK = Pattern.compile("^```([\\s\\S]+?)```(?!`)");
	public static final Pattern PATTERN_ITALICS = Pattern.compile(
			// only match _s surrounding words.
			"^\\b_"+"((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_"+"\\b"+
					"|"+
					// Or match *s that are followed by a non-space:
					"^\\*(?=\\S)("+
					// Match any of:
					//  - `**`: so that bolds inside italics don't close the
					// italics
					//  - whitespace
					//  - non-whitespace, non-* characters
					"(?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?"+
					// followed by a non-space, non-* then *
					")\\*(?!\\*)"
	);
	public static final Pattern PATTERN_MEMBER_MENTIONS = Pattern.compile("^<@!?(\\d+)>");
	public static final Pattern PATTERN_ROLE_MENTIONS = Pattern.compile("^<@&(\\d+)>");
	public static final Pattern PATTERN_CHANNEL_MENTIONS = Pattern.compile("^<#(\\d+)>");
	public static final Pattern PATTERN_EMOTES = Pattern.compile("^<a?:([a-zA-Z0-9_]+):([0-9]+)>");
	public static final Pattern PATTERN_UNICODE_EMOJIS = Pattern.compile("^:([a-zA-Z0-9-_]+):");
	public static final Pattern PATTERN_EVERYONE = Pattern.compile("^@everyone");
	public static final Pattern PATTERN_HERE = Pattern.compile("^@here");

	public static <R> Rule<R, Node<R>> createEscapeRule(){
		return new Rule<R, Node<R>>(PATTERN_ESCAPE, false){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				return ParseSpec.createTerminal(new TextNode<>(matcher.group(1)));
			}
		};
	}
	public static <R> Rule<R, Node<R>> createBoldRule(){
		return createSimpleStyleRule(PATTERN_BOLD, TextStyles.BOLD);
	}
	public static <R> Rule<R, Node<R>> createUnderlineRule(){
		return createSimpleStyleRule(PATTERN_UNDERLINE, TextStyles.UNDERLINED);
	}
	public static <R> Rule<R, Node<R>> createItalicsRule(){
		return new Rule<R, Node<R>>(PATTERN_ITALICS, false){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				int startIndex;
				int endIndex;
				String asteriskMatch = matcher.group(2);
				if(asteriskMatch!=null&&asteriskMatch.length()>0){
					startIndex = matcher.start(2);
					endIndex = matcher.end(2);
				}else{
					startIndex = matcher.start(1);
					endIndex = matcher.end(1);
				}
				return ParseSpec.createNonterminal(new StyleNode<>(TextStyles.ITALIC), startIndex, endIndex);
			}
		};
	}
	public static <R> Rule<R, Node<R>> createStrikethroughRule(){
		return createSimpleStyleRule(PATTERN_STRIKETHROUGH, TextStyles.STRIKETHROUGH);
	}
	public static <R> Rule<R, Node<R>> createSpoilerRule(){
		return createSimpleStyleRule(PATTERN_SPOILER, TextStyles.SPOILER);
	}
	public static <R> Rule<R, Node<R>> createCodeBlockRule(){
		return createSimpleStyleRule(PATTERN_CODE_BLOCK, TextStyles.CODE_BLOCK);
	}
	public static <R> Rule<R, Node<R>> createTextRule(){
		return new Rule<R, Node<R>>(PATTERN_TEXT, true){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				return ParseSpec.createTerminal(new TextNode<>(matcher.group()));
			}
		};
	}
	public static Rule<TextChannel, Node<TextChannel>> createMemberMentionRule(){
		return new Rule<TextChannel, Node<TextChannel>>(PATTERN_MEMBER_MENTIONS, true){
			@Override
			public ParseSpec<TextChannel, Node<TextChannel>> parse(Matcher matcher, Parser<TextChannel, ? super Node<TextChannel>> parser, boolean isNested){
				return ParseSpec.createTerminal(new MemberMentionNode(matcher.group(1)));
			}
		};
	}
	public static Rule<TextChannel, Node<TextChannel>> createRoleMentionRule(){
		return new Rule<TextChannel, Node<TextChannel>>(PATTERN_ROLE_MENTIONS, true){
			@Override
			public ParseSpec<TextChannel, Node<TextChannel>> parse(Matcher matcher, Parser<TextChannel, ? super Node<TextChannel>> parser, boolean isNested){
				return ParseSpec.createTerminal(new RoleMentionNode(matcher.group(1)));
			}
		};
	}
	public static Rule<TextChannel, Node<TextChannel>> createChannelMentionRule(){
		return new Rule<TextChannel, Node<TextChannel>>(PATTERN_CHANNEL_MENTIONS, true){
			@Override
			public ParseSpec<TextChannel, Node<TextChannel>> parse(Matcher matcher, Parser<TextChannel, ? super Node<TextChannel>> parser, boolean isNested){
				return ParseSpec.createTerminal(new ChannelMentionNode(matcher.group(1)));
			}
		};
	}
	public static Rule<TextChannel, Node<TextChannel>> createEmoteRule(){
		return new Rule<TextChannel, Node<TextChannel>>(PATTERN_EMOTES, true){
			@Override
			public ParseSpec<TextChannel, Node<TextChannel>> parse(Matcher matcher, Parser<TextChannel, ? super Node<TextChannel>> parser, boolean isNested){
				return ParseSpec.createTerminal(new EmoteNode(matcher.group(1), matcher.group(2)));
			}
		};
	}
	public static <R> Rule<R, Node<R>> createUnicodeEmojiRule(){
		return new Rule<R, Node<R>>(PATTERN_UNICODE_EMOJIS, true){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				return ParseSpec.createTerminal(new UnicodeEmojiNode<>(matcher.group(1)));
			}
		};
	}
	public static <R> Rule<R, Node<R>> createEveryoneRule(){
		ITextComponent text = new StringTextComponent("@everyone");
		text.getStyle().setBold(true).setColor(TextFormatting.BLUE).setInsertion("@everyone");
		return new Rule<R, Node<R>>(PATTERN_EVERYONE, true){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				return ParseSpec.createTerminal(new TextComponentNode<>(text));
			}
		};
	}
	public static <R> Rule<R, Node<R>> createHereRule(){
		ITextComponent text = new StringTextComponent("@here");
		text.getStyle().setBold(true).setColor(TextFormatting.BLUE).setInsertion("@everyone");
		return new Rule<R, Node<R>>(PATTERN_HERE, true){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				return ParseSpec.createTerminal(new TextComponentNode<>(text));
			}
		};
	}

	public static List<Rule<TextChannel, Node<TextChannel>>> createRules(){
		return createRules(true);
	}
	public static List<Rule<TextChannel, Node<TextChannel>>> createRules(boolean includeTextRule){
		ArrayList<Rule<TextChannel, Node<TextChannel>>> rules = new ArrayList<>();
		rules.add(createEscapeRule());
		rules.add(createBoldRule());
		rules.add(createUnderlineRule());
		rules.add(createItalicsRule());
		rules.add(createStrikethroughRule());
		rules.add(createSpoilerRule());
		rules.add(createCodeBlockRule());
		rules.add(createMemberMentionRule());
		rules.add(createRoleMentionRule());
		rules.add(createChannelMentionRule());
		rules.add(createEmoteRule());
		rules.add(createUnicodeEmojiRule());
		rules.add(createEveryoneRule());
		rules.add(createHereRule());
		if(includeTextRule) rules.add(createTextRule());
		return rules;
	}

	private static <R> Rule<R, Node<R>> createSimpleStyleRule(Pattern pattern, TextStyle style){
		return new Rule<R, Node<R>>(pattern, false){
			@Override
			public ParseSpec<R, Node<R>> parse(Matcher matcher, Parser<R, ? super Node<R>> parser, boolean isNested){
				return ParseSpec.createNonterminal(new StyleNode<>(style), matcher.start(1), matcher.end(1));
			}
		};
	}
}
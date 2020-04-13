package tictim.ttmpdiscordbot.ast.parser;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.ast.node.Node;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;

import static tictim.ttmpdiscordbot.MessageFormatUtils.isNormalText;

/**
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public class Parser<R, T extends Node<R>>{
	private final ArrayList<Rule<R, ? extends T>> rules = new ArrayList<>();
	private final boolean enableDebugging;

	public Parser(){
		this(false);
	}
	public Parser(boolean enableDebugging){
		this.enableDebugging = enableDebugging;
	}

	public <C extends T> Parser<R, T> addRule(Rule<R, C> rule){
		this.rules.add(rule);
		return this;
	}

	public <C extends T> Parser<R, T> addRules(Collection<Rule<R, C>> rules){
		this.rules.addAll(rules);
		return this;
	}

	public List<T> parse(@Nullable CharSequence source){
		return parse(source, false);
	}
	public List<T> parse(@Nullable CharSequence source, boolean isNested){
		Deque<ParseSpec<R, ? extends T>> remainingParses = new ArrayDeque<>();
		List<T> topLevelNodes = new ArrayList<>();

		if(source!=null&&source.length()>0) remainingParses.add(new ParseSpec<>(null, 0, source.length()));

		while(!remainingParses.isEmpty()){
			ParseSpec<R, ? extends T> builder = remainingParses.pop();
			if(builder.startIndex >= builder.endIndex) break;

			CharSequence inspectionSource = source==null ? null : source.subSequence(builder.startIndex, builder.endIndex);
			if(inspectionSource==null) continue;
			int offset = builder.startIndex;

			boolean foundRule = false;
			for(Rule<R, ? extends T> rule: rules){
				if(isNested&&!rule.applyOnNestedParse) continue;

				Matcher matcher = rule.matcher.reset(inspectionSource);

				if(matcher.find()){
					logMatch(rule, inspectionSource);
					int matcherSourceEnd = matcher.end()+offset;
					foundRule = true;

					ParseSpec<R, ? extends T> newBuilder = rule.parse(matcher, this, isNested);
					T parent = builder.root;

					if(newBuilder.root!=null){
						if(parent!=null) parent.addChild(newBuilder.root);
						else topLevelNodes.add(newBuilder.root);
					}

					// In case the last match didn't consume the rest of the source for this subtree,
					// make sure the rest of the source is consumed.
					if(matcherSourceEnd!=builder.endIndex) remainingParses.push(ParseSpec.createNonterminal(parent, matcherSourceEnd, builder.endIndex));

					// We want to speak in terms of indices within the source string,
					// but the Rules only see the matchers in the context of the substring
					// being examined. Adding this offset addresses that issue.
					if(!newBuilder.isTerminal){
						newBuilder.applyOffset(offset);
						remainingParses.push(newBuilder);
					}

					// println("source: $inspectionSource -- depth: ${remainingParses.size}")

					break;
				}else logMiss(rule, inspectionSource);
			}

			if(!foundRule){
				throw new RuntimeException("failed to find rule to match source: \"$inspectionSource\"");
			}
		}

		return topLevelNodes;
	}

	private void logMatch(Rule<R, ? extends T> rule, CharSequence source){
		if(enableDebugging) TTMPDiscordBot.LOGGER.debug("MATCH: with rule with pattern: {} to source: {}", rule.matcher.pattern().toString(), source);
	}
	private void logMiss(Rule<R, ? extends T> rule, CharSequence source){
		if(enableDebugging) TTMPDiscordBot.LOGGER.debug("MISS: with rule with pattern: {} to source: {}", rule.matcher.pattern().toString(), source);
	}

	public ITextComponent castMagic(TextComponentBuilder textBuilder, String message, @Nullable R context){ // TODO Name the fucking thing lol
		for(ITextComponent c: ForgeHooks.newChatWithLinks(message)){ // TODO It could be possible to define chat link as markdown rules
			if(isNormalText(c)) for(T node: parse(c.getUnformattedComponentText())) node.render(textBuilder, context);
			else textBuilder.append(c);
		}
		return textBuilder.build();
	}
}
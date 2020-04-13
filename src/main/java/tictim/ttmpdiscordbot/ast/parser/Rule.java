package tictim.ttmpdiscordbot.ast.parser;

import com.google.common.base.Preconditions;
import tictim.ttmpdiscordbot.ast.node.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public abstract class Rule<R, T extends Node<R>>{
	public final Matcher matcher;
	public final boolean applyOnNestedParse;
	
	public Rule(Pattern pattern){
		this(pattern, false);
	}
	public Rule(Pattern pattern, boolean applyOnNestedParse){
		Preconditions.checkNotNull(pattern);
		this.applyOnNestedParse = applyOnNestedParse;
		matcher = pattern.matcher("");
	}
	
	public abstract ParseSpec<R, T> parse(Matcher matcher, Parser<R, ? super T> parser, boolean isNested);
}
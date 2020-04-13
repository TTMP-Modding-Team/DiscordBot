package tictim.ttmpdiscordbot.ast.parser;

import tictim.ttmpdiscordbot.ast.node.Node;

import javax.annotation.Nullable;

/**
 * Facilitates fast parsing of the source text.
 * <p>
 * <p>
 * For nonterminal subtrees, the provided root will be added to the main, and text between
 * startIndex (inclusive) and endIndex (exclusive) will continue to be parsed into Nodes and
 * added as children under this root.
 * <p>
 * <p>
 * For terminal subtrees, the root will simply be added to the tree and no additional parsing will
 * take place on the text.
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public class ParseSpec<R, T extends Node<R>>{
	@Nullable
	public final T root;
	public final boolean isTerminal;
	public int startIndex;
	public int endIndex;
	
	public ParseSpec(@Nullable T root, int startIndex, int endIndex){
		this.root = root;
		this.isTerminal = false;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	public ParseSpec(@Nullable T root){
		this.root = root;
		this.isTerminal = true;
	}
	
	public void applyOffset(int offset){
		startIndex += offset;
		endIndex += offset;
	}
	
	public static <R, T extends Node<R>> ParseSpec<R, T> createNonterminal(@Nullable T node, int startIndex, int endIndex){
		return new ParseSpec<>(node, startIndex, endIndex);
	}
	public static <R, T extends Node<R>> ParseSpec<R, T> createTerminal(@Nullable T node){
		return new ParseSpec<>(node);
	}
}
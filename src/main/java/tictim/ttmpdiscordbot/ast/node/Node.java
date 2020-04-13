package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * \@stolenfrom https://github.com/AndyG/SimpleAST
 */
public abstract class Node<R> implements Iterable<Node<R>>{
	@Nullable
	private List<Node<R>> children;
	
	@Nullable
	public final List<Node<R>> getChildren(){
		return children;
	}
	
	public final boolean hasChildren(){
		return children!=null&&!children.isEmpty();
	}
	
	public final void addChild(Node<R> child){
		Preconditions.checkNotNull(child);
		if(children==null) children = new ArrayList<>();
		children.add(child);
	}
	
	public abstract void render(TextComponentBuilder builder, R renderContext);
	
	@Override
	public abstract boolean equals(Object obj);
	@Override
	public abstract int hashCode();
	
	@NotNull
	@Override
	public Iterator<Node<R>> iterator(){
		return new Iterator<Node<R>>(){
			private Node<R> nextNode = Node.this;
			@Nullable
			private Iterator<Node<R>> currentNodeIterator = children==null ? null : Iterators.concat(children.stream().map(Node::iterator).iterator());
			
			@Override
			public boolean hasNext(){
				return nextNode!=null;
			}
			@Override
			public Node<R> next(){
				if(nextNode!=null){
					Node<R> cache = nextNode;
					nextNode = currentNodeIterator!=null ? (currentNodeIterator.hasNext() ? currentNodeIterator.next() : null) : null;
					return cache;
				}else throw new NoSuchElementException();
			}
		};
	}
}

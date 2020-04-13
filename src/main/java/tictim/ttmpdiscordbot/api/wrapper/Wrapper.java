package tictim.ttmpdiscordbot.api.wrapper;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class Wrapper<T>{
	protected final T t;

	public Wrapper(T t){
		this.t = Preconditions.checkNotNull(t);
	}

	public T unwrapped(){
		return t;
	}

	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Wrapper<?> wrapper = (Wrapper<?>)o;
		return t.equals(wrapper.t);
	}
	@Override
	public int hashCode(){
		return Objects.hash(t);
	}

	@Override
	public String toString(){
		return t.toString();
	}
}

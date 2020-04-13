package tictim.ttmpdiscordbot.javascript;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.annotation.Nullable;
import java.util.Objects;

public final class JSMethod{
	private final ScriptObjectMirror function;
	@Nullable private final Object thiz;

	public JSMethod(ScriptObjectMirror function, @Nullable Object thiz){
		if(!Objects.requireNonNull(function).isFunction()) throw new IllegalArgumentException("Invalid function.");
		this.function = function;
		this.thiz = thiz;
	}

	public Object call(Object... args){
		return function.call(thiz, args);
	}
}

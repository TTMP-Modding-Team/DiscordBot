package tictim.ttmpdiscordbot.javascript;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class JSParseUtils{
	private JSParseUtils(){}

	public static <T> List<T> toList(ScriptObjectMirror array, Function<Object, T> parser){
		if(!array.isArray()) throw new IllegalArgumentException("Couldn't parse object to list, array expected.");
		List<T> list = new ArrayList<>();
		for(int i = 0; array.hasSlot(i); i++){
			T t = parser.apply(array.getSlot(i));
			list.add(t);
		}
		return list;
	}
}

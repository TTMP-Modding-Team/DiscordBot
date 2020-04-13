package tictim.ttmpdiscordbot.api;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tictim.ttmpdiscordbot.api.wrapper.WServer;
import tictim.ttmpdiscordbot.api.wrapper.Wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * All-in-one util class for JavaScript usage
 */
public final class Utils{
	private Utils(){}

	public static final Map<Class<?>, WrapperFactory<Object>> wrapperClasses = new HashMap<>();

	public static WServer server(){
		return new WServer(ServerLifecycleHooks.getCurrentServer());
	}

	// region TextComponent

	public static StringTextComponent string(String message){
		return new StringTextComponent(message);
	}
	public static TranslationTextComponent translation(String key, Object... args){
		return new TranslationTextComponent(key, args);
	}

	// endregion

	// region Wrapper

	@SuppressWarnings({"unchecked", "unused"})
	public static <T> Wrapper<T> wrap(T t){
		for(Map.Entry<Class<?>, WrapperFactory<Object>> e: wrapperClasses.entrySet()){
			Class<?> c = e.getKey();
			if(c.isInstance(t)){
				Wrapper<T> w = (Wrapper<T>)e.getValue().apply(t);
				if(w!=null) return w;
			}
		}
		return new Wrapper<>(t);
	}

	// endregion

	@FunctionalInterface
	public interface WrapperFactory<T> extends Function<T, Wrapper<T>>{}
}

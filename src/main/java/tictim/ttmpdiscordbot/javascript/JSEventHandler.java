package tictim.ttmpdiscordbot.javascript;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.GenericEvent;
import tictim.ttmpdiscordbot.TTMPDiscordBot;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JSEventHandler{ // TODO does "class": "net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.World>" work? I'm not sure.
	// Is equal to: ((?:[\p{L}_$][\p{L}\p{N}_$]*\.)*[\p{L}_$][\p{L}\p{N}_$]*)(?:<((?:[\p{L}_$][\p{L}\p{N}_$]*\.)*[\p{L}_$][\p{L}\p{N}_$]*)>)?
	private static final Pattern CLASSNAME_REGEX = Pattern.compile(
			"((?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"+
					"(?:<((?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)>)?");

	private final Class<?> eventClass;
	@Nullable private final Class<?> genericClass;
	private final JSMethod function;
	private final EventPriority priority;
	private final boolean receiveCancelled;

	private final Consumer<? extends Event> consumer = this::fire;
	private final Supplier<Object[]> argsSupplier;

	public JSEventHandler(ScriptObjectMirror instance, Supplier<Object[]> argsSupplier) throws ClassNotFoundException{
		this.argsSupplier = Objects.requireNonNull(argsSupplier);

		String classname = (String)instance.get("class");
		Matcher m = CLASSNAME_REGEX.matcher(classname);
		if(m.matches()){
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			eventClass = cl.loadClass(m.group(1));
			String generic = m.group(2);
			if(generic!=null&&!generic.isEmpty()) genericClass = cl.loadClass(generic);
			else genericClass = null;
		}else throw new IllegalArgumentException("Name of the class '"+classname+"' is invalid");

		function = new JSMethod((ScriptObjectMirror)instance.get("function"), instance);
		if(instance.hasMember("priority")){
			String priority = ((String)instance.getMember("priority")).toUpperCase();
			try{
				this.priority = EventPriority.valueOf(priority);
			}catch(RuntimeException ex){
				throw new IllegalArgumentException("Invalid event priority '"+priority+"'", ex);
			}
		}else this.priority = EventPriority.NORMAL;

		receiveCancelled = instance.hasMember("receiveCancelled") ? (Boolean)instance.getMember("receiveCancelled") : false;
	}

	@SuppressWarnings("unchecked")
	public void subscribe(){
		if(genericClass!=null){
			MinecraftForge.EVENT_BUS.addGenericListener((Class<Object>)genericClass, priority, receiveCancelled, (Class<GenericEvent<Object>>)eventClass, (Consumer<GenericEvent<Object>>)consumer);
		}else MinecraftForge.EVENT_BUS.addListener(priority, receiveCancelled, (Class<Event>)eventClass, (Consumer<Event>)consumer);
	}

	public void unsubscribe(){
		MinecraftForge.EVENT_BUS.unregister(this.consumer);
	}

	private void fire(Event e){
		try{
			Object[] args = argsSupplier.get();
			Object[] args2 = new Object[args.length+1];
			args2[0] = e;
			System.arraycopy(args, 0, args2, 1, args.length);
			function.call(args2);
		}catch(RuntimeException ex){
			TTMPDiscordBot.get().errors.add(String.format("Unexpected exception during handling event %s: %s", ex.getClass().getSimpleName(), ex));
			TTMPDiscordBot.LOGGER.error("Unexpected exception during handling event {}: ", ex.getClass().getSimpleName(), ex);
		}
	}
}

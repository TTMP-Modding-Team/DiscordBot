package tictim.ttmpdiscordbot;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;
import tictim.ttmpdiscordbot.api.Utils;
import tictim.ttmpdiscordbot.api.wrapper.Wrapper;
import tictim.ttmpdiscordbot.api.wrapper.WrapperClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "ttmpdiscordbot", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WrapperClassLoader{
	private WrapperClassLoader(){}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event){
		int detectedCount = 0, injectedCount = 0;
		Type annotationType = Type.getType(WrapperClass.class);
		for(ModFileScanData scanData: ModList.get().getAllScanData()){
			for(AnnotationData a: scanData.getAnnotations()){
				if(Objects.equals(a.getAnnotationType(), annotationType)){
					detectedCount++;
					String className = a.getMemberName();
					try{
						Class<?> clazz = Class.forName(className).asSubclass(Wrapper.class);
						if(!Utils.wrapperClasses.containsKey(clazz)){
							List<Constructor<?>> constructors = Arrays.stream(clazz.getConstructors())
									.filter(it ->
											it.getParameterCount()==1&&
													!it.isVarArgs()&&
													!it.getParameterTypes()[0].isArray()&&
													!it.getParameterTypes()[0].isPrimitive())
									.collect(Collectors.toList());
							if(!constructors.isEmpty()){
								Constructor<?> c = constructors.get(0);
								TTMPDiscordBot.LOGGER.info("@WrapperClass {} : {}", className, a.getAnnotationData().entrySet().stream().map(e -> e.toString()+"("+e.getValue().getClass()+")").collect(Collectors.joining()));
								Type t = (Type)a.getAnnotationData().get("value");
								Utils.wrapperClasses.put(Class.forName(t.getClassName()), o -> {
									try{
										//noinspection unchecked
										return (Wrapper<Object>)c.newInstance(o);
									}catch(IllegalAccessException|InstantiationException|InvocationTargetException e){
										TTMPDiscordBot.LOGGER.error("Unable to create wrapper class for {} ", className, e);
										return null;
									}
								});
								injectedCount++;
							}else TTMPDiscordBot.LOGGER.error("Unable to load class {}, couldn't find appropriate constructor", className);
						}
					}catch(ClassNotFoundException|LinkageError|RuntimeException e){
						TTMPDiscordBot.LOGGER.error("Unable to load class {} due to an exception: ", className, e);
					}
				}
			}
		}
		TTMPDiscordBot.LOGGER.info("{} wrapper types detected, {} injected", detectedCount, injectedCount);
	}
}

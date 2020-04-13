package tictim.ttmpdiscordbot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Cfgs{
	private Cfgs(){}

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(UserSettingsImpl.class, new UserSettingsImpl.Serializer())
			.registerTypeAdapter(L10n.class, new L10n.Serializer())
			.registerTypeAdapter(ChannelSettingsImpl.class, new ChannelSettingsImpl.Serializer())
			.registerTypeAdapter(ChannelSetting.class, new ChannelSetting.Serializer())
			.setPrettyPrinting().create();

	public static <T> T readFromConfig(String fileName, Class<T> clazz, Supplier<T> fallback, @Nullable Consumer<Throwable> exceptionHandler){
		return read(configJson(fileName), clazz, fallback, exceptionHandler);
	}
	public static void writeToConfig(String fileName, Object object, @Nullable Consumer<Throwable> exceptionHandler){
		write(configJson(fileName), object, exceptionHandler);
	}
	public static <T> T read(File file, Class<T> clazz, Supplier<T> fallback, @Nullable Consumer<Throwable> exceptionHandler){
		try(Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
			T t = GSON.fromJson(r, clazz);
			if(t!=null) return t;
		}catch(FileNotFoundException ignored){
		}catch(Exception e){
			if(exceptionHandler!=null) exceptionHandler.accept(e);
			return fallback.get();
		}
		T t = fallback.get();
		write(file, t, exceptionHandler);
		return t;
	}
	public static void write(File file, Object object, @Nullable Consumer<Throwable> exceptionHandler){
		try(Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)){
			//noinspection ResultOfMethodCallIgnored
			file.getParentFile().mkdir();
			GSON.toJson(object, w);
		}catch(Exception e){
			if(exceptionHandler!=null) exceptionHandler.accept(e);
		}
	}

	public static File configJson(String name){
		return configFile(name, "json");
	}
	public static File configFile(String name, String extension){
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "ttmpdiscordbot/"+name+"."+extension);
	}
	public static File configDirectory(String name){
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "ttmpdiscordbot/"+name);
	}
	public static File configRoot(){
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "ttmpdiscordbot");
	}

	public static void generateDefaultBotCommands(File dest){
		AssetFetcher af = new AssetFetcher(getLocale(), locale -> "discordbot/config/"+locale+"/commands");
		af.copyAll(dest);
	}
	public static void generateDefaultActivity(File dest){
		AssetFetcher af = new AssetFetcher(getLocale(), locale -> "discordbot/config/"+locale+"/activity");
		af.copyAll(dest);
	}

	@Nullable private static String locale;
	public static String getLocale(){
		return locale!=null ? locale : getDefaultLocale();
	}
	public static void setLocale(@Nullable String locale){
		Cfgs.locale = locale!=null ? locale.toLowerCase() : locale;
	}
	private static String getDefaultLocale(){
		return DistExecutor.runForDist(() -> () -> Minecraft.getInstance().gameSettings.language, () -> () -> "en_us");
	}
}

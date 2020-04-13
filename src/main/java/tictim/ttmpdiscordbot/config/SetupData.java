package tictim.ttmpdiscordbot.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Consumer;

public final class SetupData{
	private final CommentedFileConfig cfg;

	private SetupData(CommentedFileConfig cfg){ this.cfg = cfg; }

	public String getBotToken(){
		return cfg.get("botToken");
	}
	public String getLocale(){
		return cfg.get("locale");
	}

	@Nullable
	public static SetupData read(@Nullable Consumer<Throwable> exceptionHandler){
		try{
			CommentedFileConfig cfg = CommentedFileConfig.builder(new File(FMLPaths.CONFIGDIR.get().toFile(), "ttmpdiscordbot/setup.toml")).build();
			cfg.load();
			boolean save = false;
			if(!cfg.contains("botToken")){
				cfg.set("botToken", "");
				cfg.setComment("botToken", "Your Discord bot token goes here.");
				save = true;
			}
			if(!cfg.contains("locale")){
				cfg.set("locale", "en_us");
				cfg.setComment("locale", "Locale(aka language) for discord bot goes here. The localization data for target locale will be fetched.\n" +
						"Note that if the localization data for target locale wasn't downloaded before, fetching wouldn't work...");
				save = true;
			}
			if(save) cfg.save();
			return new SetupData(cfg);
		}catch(Exception ex){
			if(exceptionHandler!=null) exceptionHandler.accept(ex);
			return null;
		}
	}
}

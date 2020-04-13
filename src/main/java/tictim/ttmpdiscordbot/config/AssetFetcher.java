package tictim.ttmpdiscordbot.config;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.io.FileUtils;
import tictim.ttmpdiscordbot.TTMPDiscordBot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AssetFetcher{
	private final Map<String, ResourceLocation> destToLocation = new HashMap<>();

	public AssetFetcher(String locale, Function<String, String> localeToAsset){
		Objects.requireNonNull(localeToAsset);
		Objects.requireNonNull(locale);

		String l = localeToAsset.apply(locale), l2;
		searchForIndex(l);
		if(!"en_us".equals(locale)) searchForIndex(l2 = localeToAsset.apply("en_us"));
		else l2 = l;

		if(destToLocation.isEmpty()){
			if(l.equals(l2)) TTMPDiscordBot.LOGGER.error("Couldn't fetch asset, index for asset {} doesn't exist.", l);
			else TTMPDiscordBot.LOGGER.error("Couldn't fetch asset, neither of index {} or {} doesn't exist.", l, l2);
		}
	}

	private void searchForIndex(String asset){
		IReloadableResourceManager m = ServerLifecycleHooks.getCurrentServer().getResourceManager();
		Collection<ResourceLocation> resources = m.getAllResourceLocations(asset, s -> true);
		for(ResourceLocation r: resources){
			if(!r.getPath().equals(asset)) destToLocation.putIfAbsent(r.getPath().substring(asset.length()+1), r);
		}
	}

	public int copyAll(File root){
		if(root.isFile()){
			TTMPDiscordBot.LOGGER.error("Couldn't copy using file as root!");
			return 0;
		}
		root.mkdir();

		int succeed = 0;
		IReloadableResourceManager m = ServerLifecycleHooks.getCurrentServer().getResourceManager();

		for(Map.Entry<String, ResourceLocation> e: destToLocation.entrySet()){
			ResourceLocation rl = e.getValue();
			try(InputStream stream = m.getResource(rl).getInputStream()){
				File dest = new File(root, e.getKey());
				if(dest.exists()){
					if(dest.isDirectory()) FileUtils.deleteDirectory(dest);
					else //noinspection ResultOfMethodCallIgnored
						dest.delete();
				}
				dest.getParentFile().mkdirs();
				TTMPDiscordBot.LOGGER.info("{} -> {}", stream, dest);
				Files.copy(stream, dest.toPath());
				succeed++;
			}catch(IOException|RuntimeException ex){
				TTMPDiscordBot.LOGGER.error("Unexpected exception while reading resource {}: ", rl, ex);
			}
		}
		return succeed;
	}
}

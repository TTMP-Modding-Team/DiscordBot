package tictim.ttmpdiscordbot.config;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tictim.ttmpdiscordbot.TTMPDiscordBot;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

public class L10n{
	private static final class FallbackLanguage{
		private static final LanguageMap FALLBACK_LANGUAGE = new LanguageMap();
	}

	private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

	private static LanguageMap getFallbackLanguage(){
		return FallbackLanguage.FALLBACK_LANGUAGE;
	}

	private static final String[] DEFAULT_L10N_COLLECT = {
			"discord", "chat.type.advancement", "multiplayer.player", "commands.discord", "argument.discord"
	};

	private static boolean isDefaultL10nCollectedKey(String key){
		for(String s: DEFAULT_L10N_COLLECT) if(key.startsWith(s)) return true;
		return false;
	}

	public void collectDefaults(String locale){
		collect(locale, L10n::isDefaultL10nCollectedKey);
	}
	public void collect(String locale){
		collect(locale, s -> true);
	}
	public void collect(String locale, Predicate<String> keyFilter){ // TODO needs testing on real environment
		long time = System.currentTimeMillis();
		try{
			int initial = size();
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			loadLocaleData(keyFilter, cl.getResourceAsStream("assets/minecraft/lang/"+locale+".json"));

			DistExecutor.runWhenOn(Dist.CLIENT,
					() -> () -> findLocaleData(Minecraft.getInstance().getResourceManager(), locale, keyFilter));
			DistExecutor.runWhenOn(Dist.DEDICATED_SERVER,
					() -> () -> {
						// Construct new fucking resource manager because thanks mojang
						SimpleReloadableResourceManager rm = new SimpleReloadableResourceManager(ResourcePackType.CLIENT_RESOURCES, Thread.currentThread());
						rm.addResourcePack(new VanillaPack("minecraft", "realms"));
						findLocaleData(rm, locale, keyFilter);
						findLocaleData(ServerLifecycleHooks.getCurrentServer().getResourceManager(), locale, keyFilter);
					});

			TTMPDiscordBot.LOGGER.debug("Collected {} localization data, took {} ms.", size()-initial, System.currentTimeMillis()-time);
		}catch(Exception e){
			TTMPDiscordBot.LOGGER.error("Couldn't read localization data for locale {}, unexpected exception occurred.", locale, e);
		}
	}

	private void findLocaleData(IResourceManager resources, String locale, Predicate<String> keyFilter){
		String langFile = "lang/"+locale+".json";
		for(String namespace: resources.getResourceNamespaces()){
			TTMPDiscordBot.LOGGER.debug("Collecting localization data from namespace: {}", namespace);
			try{
				List<IResource> resourceList = resources.getAllResources(new ResourceLocation(namespace, langFile));
				if(resourceList!=null){
					TTMPDiscordBot.LOGGER.debug("Fetching localization data from {} lang files in locale {}", resourceList.size(), namespace);
					for(IResource resource: resourceList) loadLocaleData(keyFilter, resource.getInputStream());
				}else TTMPDiscordBot.LOGGER.debug("No localization data was found from {}, skipping.", namespace);
			}catch(FileNotFoundException ignored){
			}catch(IOException e){
				TTMPDiscordBot.LOGGER.error("Skipping localization data on {}:{} due to unexpected exception occurred. ", namespace, locale, e);
			}
		}
	}

	// @see LanguageHook#loadLocaleData
	private void loadLocaleData(Predicate<String> keyFilter, @Nullable InputStream stream){
		if(stream==null) return;
		try(Reader r = new InputStreamReader(stream, StandardCharsets.UTF_8)){
			for(Map.Entry<String, JsonElement> e: JSONUtils.getJsonObject(new Gson().fromJson(r, JsonElement.class), "strings").entrySet()){
				String key = e.getKey();
				if(keyFilter.test(key)){
					localizations.put(key, NUMERIC_VARIABLE_PATTERN.matcher(JSONUtils.getString(e.getValue(), key)).replaceAll("%$1s"));
				}
			}
		}catch(Exception e){
			TTMPDiscordBot.LOGGER.error("Skipping {} due to unexpected exception occurred: ", stream, e);
		}
	}

	private final Map<String, String> localizations = new HashMap<>();

	public L10n(){}
	public L10n(Map<String, String> localizations){
		this.localizations.putAll(localizations);
	}

	@Nullable
	public String get(String key){
		return localizations.get(key);
	}
	public String getOrKey(String key){
		return localizations.getOrDefault(key, key);
	}
	public String getOrDefault(String key, String defaultValue){
		return localizations.getOrDefault(key, defaultValue);
	}
	@Nullable
	public String getOrTranslate(String key){
		String result = localizations.get(key);
		if(result!=null) return result;
		else synchronized(this){
			return LanguageMap.getInstance().exists(key) ? LanguageMap.getInstance().translateKey(key) : getFallbackLanguage().translateKey(key);
		}
	}

	public boolean contains(String key){
		return localizations.containsKey(key);
	}
	public boolean isEmpty(){
		return localizations.isEmpty();
	}
	public int size(){
		return this.localizations.size();
	}

	/**
	 * 'unfolds' the text component, meaning it replaces all TranslationTextComponent that can be translated by this instance into simple text.<br>
	 * For preventing from breaking user locale settings, L10n will replace TranslationTextComponent only if the key starts with certain string. 'Bold Unfold' ignores this restriction and will replace as many as they can do.<br>
	 * Note that it may modify the text component. Returning TextComponent might be same instance as input, or completely new instance.
	 */
	public ITextComponent unfold(ITextComponent text, boolean boldUnfold){
		// If it's TranslationTextComponent and can be translated, change the instance (create empty string component, apply style and append its children.)
		if(text instanceof TranslationTextComponent){
			TranslationTextComponent t = (TranslationTextComponent)text;
			Object[] f = t.getFormatArgs();
			for(int i = 0; i<f.length; i++){
				Object o = f[i];
				if(o instanceof ITextComponent) f[i] = unfold((ITextComponent)o, boldUnfold);
			}
			if(contains(t.getKey())&&(boldUnfold||isDefaultL10nCollectedKey(t.getKey()))){
				text = new StringTextComponent("").setStyle(text.getStyle().createShallowCopy().setParentStyle(null));
				for(ITextComponent c: new L10nTextComponent(t.getKey(), t.getFormatArgs()).getChildren()) text.appendSibling(unfold(c, boldUnfold));
			}
		}
		Style s = text.getStyle();
		HoverEvent e = s.getHoverEvent();
		if(e!=null&&e.getAction()==SHOW_TEXT) s.setHoverEvent(new HoverEvent(SHOW_TEXT, unfold(e.getValue(), boldUnfold)));

		List<ITextComponent> list = text.getSiblings();
		for(int i = 0; i<list.size(); i++){
			ITextComponent c = unfold(list.get(i), boldUnfold);
			c.getStyle().setParentStyle(s);
			list.set(i, c);
		}
		return text;
	}

	public static final class Serializer implements JsonSerializer<L10n>, JsonDeserializer<L10n>{
		@Override
		public L10n deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
			return new L10n(json.getAsJsonObject()
					.entrySet().stream()
					.map(e -> new SimpleEntry<>(e.getKey(), e.getValue().getAsString()))
					.collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll));
		}
		@Override
		public JsonElement serialize(L10n src, Type typeOfSrc, JsonSerializationContext context){
			return src.localizations.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(JsonObject::new, (o, e) -> o.addProperty(e.getKey(), e.getValue()), (o1, o2) -> {
				for(Map.Entry<String, JsonElement> e: o2.entrySet()) o1.add(e.getKey(), e.getValue());
			});
		}
	}

	/**
	 * Yeah, more dirty hacks!
	 */
	public final class L10nTextComponent extends TranslationTextComponent{
		private boolean initialized;

		private L10nTextComponent(String key, Object... format){
			super(key, format.length>0 ? Arrays.copyOf(format, format.length) : format);
		}

		// @see TranslationTextComponent#ensureInitialized()
		private void ensureInitialized2(){
			synchronized(this){
				if(!initialized){
					initialized = true;
					try{
						String s = get(getKey());
						if(s!=null){
							this.initializeFromFormat(s);
							return;
						}
					}catch(TranslationTextComponentFormatException ignored){
					}
					try{
						if(LanguageMap.getInstance().exists(getKey())){
							this.initializeFromFormat(LanguageMap.getInstance().translateKey(getKey()));
							return;
						}
					}catch(TranslationTextComponentFormatException ignored){
					}
					try{
						if(getFallbackLanguage().exists(getKey())){
							this.initializeFromFormat(getFallbackLanguage().translateKey(getKey()));
							return;
						}
					}catch(TranslationTextComponentFormatException ignored){
					}
					this.children.add(new StringTextComponent(getKey()));
				}
			}
		}

		@Override
		public Stream<ITextComponent> stream(){
			this.ensureInitialized2();
			return Stream.concat(this.children.stream(), this.siblings.stream()).flatMap(ITextComponent::stream);
		}

		@Override
		public String getUnformattedComponentText(){
			this.ensureInitialized2();
			StringBuilder stb = new StringBuilder();
			for(ITextComponent t: this.children) stb.append(t.getUnformattedComponentText());
			return stb.toString();
		}

		private List<ITextComponent> getChildren(){
			this.ensureInitialized2();
			return this.children;
		}
	}
}

package tictim.ttmpdiscordbot.api.wrapper;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@WrapperClass(MinecraftServer.class)
public final class WServer extends Wrapper<MinecraftServer>{
	public WServer(MinecraftServer minecraftServer){
		super(minecraftServer);
	}

	public List<WPlayer> players(){
		return t.getPlayerList().getPlayers().stream().map(WPlayer::new).collect(Collectors.toList());
	}

	@Nullable public Profile findProfile(String name){
		GameProfile gameProfile = t.getPlayerProfileCache().getGameProfileForUsername(name);
		return gameProfile!=null ? new Profile(gameProfile) : null;
	}
	public WWorld overworld(){
		return new WWorld(t.getWorld(DimensionType.OVERWORLD));
	}

	@Nullable public WWorld world(WNamespace id){
		return world(id.t);
	}
	@Nullable public WWorld world(String id){
		return world(new ResourceLocation(id));
	}
	@Nullable public WWorld world(ResourceLocation rl){
		World w = DimensionManager.getWorld(t, DimensionType.byName(rl), false, false);
		return w==null ? null : new WWorld(w);
	}
}

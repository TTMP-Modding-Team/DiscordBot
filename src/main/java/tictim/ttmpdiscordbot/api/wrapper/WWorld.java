package tictim.ttmpdiscordbot.api.wrapper;

import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

@WrapperClass(World.class)
public final class WWorld extends Wrapper<World>{
	public WWorld(World world){
		super(world);
	}

	public List<WPlayer> players(){
		return t.getPlayers().stream().map(WPlayer::new).collect(Collectors.toList());
	}

	public long dayTime(){
		return t.getDayTime();
	}
	public long gameTime(){
		return t.getGameTime();
	}
	public boolean isDaytime(){
		return t.isDaytime();
	}
	public boolean isRaining(){
		return t.isRaining();
	}
	public boolean isThundering(){
		return t.isThundering();
	}
}

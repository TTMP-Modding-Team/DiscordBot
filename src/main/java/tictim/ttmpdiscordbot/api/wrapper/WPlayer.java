package tictim.ttmpdiscordbot.api.wrapper;

import net.minecraft.entity.player.PlayerEntity;

@WrapperClass(PlayerEntity.class)
public final class WPlayer extends Wrapper<PlayerEntity>{
	public WPlayer(PlayerEntity player){
		super(player);
	}
	
	public WWorld world(){
		return new WWorld(t.world);
	}
	public Profile profile(){
		return new Profile(t.getGameProfile());
	}
	public String name(){
		return t.getGameProfile().getName();
	}
}

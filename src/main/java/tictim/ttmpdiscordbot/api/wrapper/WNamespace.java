package tictim.ttmpdiscordbot.api.wrapper;

import net.minecraft.util.ResourceLocation;

@WrapperClass(ResourceLocation.class)
public final class WNamespace extends Wrapper<ResourceLocation>{
	public WNamespace(ResourceLocation rl){
		super(rl);
	}

	public String namespace(){
		return t.getNamespace();
	}
	public String path(){
		return t.getPath();
	}
}

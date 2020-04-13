package tictim.ttmpdiscordbot.api.wrapper;

import net.minecraft.item.Item;

@WrapperClass(Item.class)
public final class WItem extends Wrapper<Item>{
	public WItem(Item item){
		super(item);
	}

	public WNamespace registryName(){
		return new WNamespace(t.getRegistryName());
	}
}

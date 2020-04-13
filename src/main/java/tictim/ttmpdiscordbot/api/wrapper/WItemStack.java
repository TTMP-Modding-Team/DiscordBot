package tictim.ttmpdiscordbot.api.wrapper;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@WrapperClass(ItemStack.class)
public final class WItemStack extends Wrapper<ItemStack>{
	public WItemStack(ItemStack itemStack){
		super(itemStack);
	}

	@Nullable public WItem item(){
		return t.isEmpty() ? null : new WItem(t.getItem());
	}
	public int count(){
		return t.getCount();
	}
	public void setCount(int count){
		t.setCount(count);
	}
	public int maxCount(){
		return t.getMaxStackSize();
	}
	public int damage(){
		return t.getDamage();
	}
	public void setDamage(int damage){
		t.setDamage(damage);
	}
	public int maxDamage(){
		return t.getMaxDamage();
	}

	public boolean isEmpty(){
		return t.isEmpty();
	}
}

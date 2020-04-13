package tictim.ttmpdiscordbot.api.wrapper;

import com.mojang.authlib.GameProfile;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public final class Profile{
	private final String name;
	@Nullable
	private final UUID id;
	
	public Profile(String name, @Nullable UUID id){
		this.name = name;
		this.id = id;
	}
	public Profile(GameProfile gameProfile){
		this.name = gameProfile.getName();
		this.id = gameProfile.getId();
	}
	
	public String name(){
		return name;
	}
	@Nullable
	public UUID id(){
		return id;
	}
	
	public GameProfile toGameProfile(){
		return new GameProfile(id, name);
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Profile profile = (Profile)o;
		return Objects.equals(name, profile.name)&&Objects.equals(id, profile.id);
	}
	@Override
	public int hashCode(){
		return Objects.hash(name, id);
	}
	
	@Override
	public String toString(){
		return id==null ? name : String.format("%s (%s)", name, id);
	}
}

package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.util.text.*;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Objects;

import static java.lang.Math.*;

public class RoleMentionNode extends Node<TextChannel>{
	private final String id;
	public RoleMentionNode(String id){
		this.id = Preconditions.checkNotNull(id);
	}
	
	@Override
	public void render(TextComponentBuilder builder, TextChannel renderContext){
		Role r = renderContext!=null ? renderContext.getGuild().getRoleById(id) : TTMPDiscordBot.get().getJDA().getRoleById(id);
		if(r!=null){
			ITextComponent text = new StringTextComponent("@"+r.getName());
			setEvent(text.getStyle().setBold(true).setColor(getClosestColorToRoleColor(r, TextFormatting.BLUE)), r);
			builder.append(text);
		}else builder.append("@deleted-role");
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		RoleMentionNode nodes = (RoleMentionNode)o;
		return id.equals(nodes.id);
	}
	@Override
	public int hashCode(){
		return Objects.hash(id);
	}
	
	public static void setEvent(Style style, Role role){
		style.setHoverEvent(TextComponentBuilder.hoverTextEvent(new TranslationTextComponent("discord.chat.useShift"))).setInsertion("@"+role.getName());
	}
	
	@Nullable
	public static TextFormatting getClosestColorToRoleColor(Role role){
		return getClosestColorToRoleColor(role, null);
	}
	@Nullable
	public static TextFormatting getClosestColorToRoleColor(Role role, @Nullable TextFormatting defaultValue){
		return role.getColor()==null ? defaultValue : getClosestColor(role.getColor());
	}
	public static TextFormatting getClosestColor(Color color){
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		
		ColorFormatting closest = null;
		double difference = Double.POSITIVE_INFINITY;
		
		for(ColorFormatting cf: ColorFormatting.values()){
			float dh1 = abs(hsb[0]-cf.hsb[0]);
			float dh = min(dh1, 1-dh1);
			float ds = (float)abs(sqrt(hsb[1])-sqrt(cf.hsb[1]));
			float db = abs(sq(hsb[2])-sq(cf.hsb[2]));
			double diff = dh*dh+ds*ds+db*db;
			
			if(diff<difference){
				difference = diff;
				closest = cf;
			}
		}
		return TextFormatting.values()[closest.ordinal()];
	}
	
	private static float sq(float d){
		return d*d;
	}
	
	// Need to use these bc TextFormatting#getColor is fucking clientsided
	private enum ColorFormatting{
		BLACK(0),
		DARK_BLUE(170),
		DARK_GREEN(43520),
		DARK_AQUA(43690),
		DARK_RED(11141120),
		DARK_PURPLE(11141290),
		GOLD(16755200),
		GRAY(11184810),
		DARK_GRAY(5592405),
		BLUE(5592575),
		GREEN(5635925),
		AQUA(5636095),
		RED(16733525),
		LIGHT_PURPLE(16733695),
		YELLOW(16777045),
		WHITE(16777215);
		
		private final float[] hsb;
		
		ColorFormatting(int rgb){
			Color color = new Color(rgb);
			this.hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		}
	}
}

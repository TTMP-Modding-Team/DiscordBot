package tictim.ttmpdiscordbot.ast.node;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.util.text.*;
import tictim.ttmpdiscordbot.TTMPDiscordBot;
import tictim.ttmpdiscordbot.api.wrapper.Profile;
import tictim.ttmpdiscordbot.ast.textcomponent.TextComponentBuilder;

import java.util.Objects;

public class MemberMentionNode extends Node<TextChannel>{
	private final String id;
	public MemberMentionNode(String id){
		this.id = Preconditions.checkNotNull(id);
	}
	
	@Override
	public void render(TextComponentBuilder builder, TextChannel renderContext){
		Member m = renderContext!=null ?
				renderContext.getGuild().getMemberById(id) :
				TTMPDiscordBot.get().getJDA().getGuildCache().stream().map(g -> g.getMemberById(id)).filter(Objects::nonNull).findFirst().orElse(null);
		ITextComponent textComponent = new StringTextComponent(m!=null ? "@"+m.getEffectiveName() : "<@"+id+">");
		Style style = textComponent.getStyle().setBold(true).setColor(TextFormatting.BLUE);
		if(m!=null) setEvent(style, m);
		builder.append(textComponent);
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MemberMentionNode nodes = (MemberMentionNode)o;
		return id.equals(nodes.id);
	}
	@Override
	public int hashCode(){
		return Objects.hash(id);
	}
	
	public static void setEvent(Style style, Member member){
		style.setHoverEvent(TextComponentBuilder.hoverTextEvent(getKnownAs(member), new TranslationTextComponent("discord.chat.useShift"))).setInsertion("@"+member.getEffectiveName());
	}
	public static void setEvent(Style style, User user){
		style.setHoverEvent(TextComponentBuilder.hoverTextEvent(getKnownAs(user), new TranslationTextComponent("discord.chat.useShift"))).setInsertion("@"+user.getName());
	}
	
	public static ITextComponent getKnownAs(Member member){
		return getKnownAs(member.getUser());
	}
	public static ITextComponent getKnownAs(User user){
		if(user.isBot()) return new TranslationTextComponent("discord.user.bot");
		Profile profile = TTMPDiscordBot.get().userSettings().profile(user.getIdLong());
		if(profile==null) return new TranslationTextComponent("discord.user.unknown");
		return profile.id()!=null ?
				new TranslationTextComponent("discord.user.knownAs", TextFormatting.BOLD+profile.name(), profile.id()) :
				new TranslationTextComponent("discord.user.knownAs.noId", TextFormatting.BOLD+profile.name());
	}
}

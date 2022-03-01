import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter{
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		//String[] args = event.getMessage().getContentRaw().split("\\s+");
		EmbedBuilder info = new EmbedBuilder();
		Message msg;
		
		MessageChannel channel = event.getChannel();
		Category category = event.getGuild().getTextChannelsByName("log-helper", true);
		//category.createTextChannel("Log Helper").queue();
		
	//	TextChannel textChannel = event.getGuild().getTextChannelsByName("TSJ",true).get(0);
//		System.out.println("textChannel : " + event.getGuild().getTextChannelsByName("副本报名",true).get(0).getId());
		
		try {
			info.setTitle("Leader : " + event.getMember().getEffectiveName());
//			info.setDescription("困难5连");
			info.setDescription("description here");

			
			info.addField("<:time:888631032759353345>" + "time here", "<:tank:888631032713199626> 0/2", true);
			info.addField("<:calendar:888631032767725598>" + "calendar here", "<:heal:888631032474132491> 0/2", true);
			info.addField("", "<:dps:888631032729989200> 0/2", true);
			
			info.setColor(0x8c0000);
			System.out.println("event.getMember().getUser().getAvatarUrl() : " + event.getMember().getUser().getAvatarUrl());

			info.setFooter("Report bug to Lozy#9999", "https://cdn.discordapp.com/avatars/180635615790891008/165df9084c1c3aacadf36a1e81761627.png");

//			textChannel.sendMessage(info.build()).queue(embed->{
//				embed.addReaction(Emotes.TANK).queue();
//				embed.addReaction(Emotes.HEAL).queue();
//				embed.addReaction(Emotes.DPS).queue();
//			});
			
		} catch(Exception e) {
			if(e.toString().startsWith("java.lang.ArrayIndexOutOfBoundsException")) {
				EmbedBuilder error = new EmbedBuilder();
				error.setColor(0xff3923);
				error.setTitle("Invalid parameters");
				event.getChannel().sendMessage(error.build()).queue();
			}
		}
	}
}

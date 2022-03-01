import java.awt.Color;

import model.LogModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LogHelperCommand extends ListenerAdapter {
	public static LogModel logModel = new LogModel();

	public static EmbedBuilder embedVerbose = new EmbedBuilder();
	public static EmbedBuilder embedDebug = new EmbedBuilder();
	public static EmbedBuilder embedError = new EmbedBuilder();

	public static MessageEmbed verboseMessage;
	public static MessageEmbed debugMessage;
	public static MessageEmbed errorMessage;

	public static long embedVerboseId;

	public static TextChannel textChannel;
	long msgid;

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) { // so that it wont check for bot's reply
			new LogReader();
			msgid = event.getMessageIdLong();

			System.out.println("msgid : " + msgid);
			fileReadThread.start();

			textChannel = event.getGuild().getTextChannelsByName("log-helper", true).get(0);

			// create new embed
			embedVerbose.setTitle("FFXIV Log");
			embedDebug.setTitle("FFXIV Log");
			embedError.setTitle("FFXIV Log");

			embedVerbose.addField("Verbose", "", true);
			embedDebug.addField("Debug", "", true);
			embedError.addField("Error", "", true);

			embedVerbose.setColor(Color.YELLOW);
			embedDebug.setColor(Color.GRAY);
			embedError.setColor(Color.RED);

			verboseMessage = embedVerbose.build();
			debugMessage = embedDebug.build();
			errorMessage = embedError.build();

			sendVerbose();
			// textChannel.sendMessage(debugMessage).queue();
			// textChannel.sendMessage(errorMessage).queue();

			updateEmbedMessageThread.start();
			return;
		}
		// updateLogListener();
	}

	private void sendVerbose() {
		System.out.println("sendVerbose()");
		// TODO Auto-generated method stub

		textChannel.sendMessage(verboseMessage).queue((message) -> {
			embedVerboseId = message.getIdLong();
			//textChannel.sendMessage(verboseMessage).queue();
		});
	}

	Thread fileReadThread = new Thread() {
		public void run() {
			System.out.println("fileReadThread running");
			LogReader.readFile();
		}
	};

	Thread updateEmbedMessageThread = new Thread() {
		public void run() {
			int i = 0;
			while (true) {
				try {
					System.out.println("updateEmbedMessageThread running");
					Thread.sleep(1000);
					embedVerbose.setFooter(convertArrayListToOneLine());
					textChannel.editMessageById(String.valueOf(embedVerboseId), embedVerbose.build()).queue();
					i++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	String convertArrayListToOneLine(){

        String delim = "\n";
 
        StringBuilder sb = new StringBuilder();
 
        int i = 0;
        while (i < logModel.verbose.size() - 1)
        {
            sb.append(logModel.verbose.get(i));
            sb.append(delim);
            i++;
        }
        sb.append(logModel.verbose.get(i));
 
        String res = sb.toString();
        System.out.println(res);        // A-B-C
        
        return res.substring(res.length() - 2048);
	}
}

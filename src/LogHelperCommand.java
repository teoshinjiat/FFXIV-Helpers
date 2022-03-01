import java.awt.Color;
import java.util.ArrayList;

import model.LogModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LogHelperCommand extends ListenerAdapter {
	public static LogModel logModel = new LogModel();

	public static EmbedBuilder embedDebug = new EmbedBuilder();
	public static EmbedBuilder embedError = new EmbedBuilder();
	public static EmbedBuilder embedVerbose = new EmbedBuilder();

	public static MessageEmbed debugMessage;
	public static MessageEmbed errorMessage;
	public static MessageEmbed verboseMessage;

	public static long embedDebugMessageId;
	public static long embedErrorMessageId;
	public static long embedVerboseMessageId;

	public static TextChannel textChannel;
	long msgid;
	int previousDebugLineNumber = 0;
	int previousErrorLineNumber = 0;
	int previousVerboseLineNumber = 0;

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) { // so that it wont check for bot's reply
			new LogReader();
			msgid = event.getMessageIdLong();

			fileReadThread.start();

			textChannel = event.getGuild().getTextChannelsByName("log-helper", true).get(0);

			// create new embed
			embedDebug.setTitle("FFXIV Log");
			embedError.setTitle("FFXIV Log");
			embedVerbose.setTitle("FFXIV Log");

			embedDebug.addField("Debug", "", true);
			embedError.addField("Error", "", true);
			embedVerbose.addField("Verbose", "", true);

			embedDebug.setColor(Color.GRAY);
			embedError.setColor(Color.RED);
			embedVerbose.setColor(Color.YELLOW);

			debugMessage = embedDebug.build();
			errorMessage = embedError.build();
			verboseMessage = embedVerbose.build();

			createBaseEmbed();

			updateEmbedMessageThread.start(); // thread for periodically update the embed message in discord
			return;
		}
		// updateLogListener();
	}

	Thread fileReadThread = new Thread() {
		public void run() {
			System.out.println("fileReadThread running");
			LogReader.readFile();
		}
	};

	Thread updateEmbedMessageThread = new Thread() {
		public void run() {
			try {
				Thread.sleep(2000); // waiting for embed creation 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				detectNewLines(); // if there is new line, then only update embed message
			}
		}
	};

	private void detectNewLines() {
		lineNumbersOutOfSync();
	}

	private void lineNumbersOutOfSync() {
		System.out.println("lineNumbersOutOfSync()");
		System.out.println("verbose : " + logModel.previousVerboseLineNumber + " != " + logModel.currentVerboseLineNumber);
		System.out.println("debug : " + logModel.previousDebugLineNumber + " != " + logModel.currentDebugLineNumber);
		System.out.println("error : " + logModel.previousErrorLineNumber + " != " + logModel.currentErrorLineNumber);

		if (logModel.previousVerboseLineNumber != logModel.currentVerboseLineNumber) {
			System.out.println("new lines detected for verbose");
			System.out.println(logModel.previousVerboseLineNumber + " != " + logModel.currentVerboseLineNumber);
			System.out.println("logModel.currentVerboseLineNumber : " + logModel.currentVerboseLineNumber);
			System.out.println("LogHelperCommand.embedVerboseMessageId : " + LogHelperCommand.embedVerboseMessageId);

			updateEmbedFn(LogHelperCommand.embedVerboseMessageId, embedVerbose, logModel.verboseLogsList);
			LogHelperCommand.logModel.savePreviousLineNumber("verbose");

		} else if (logModel.previousDebugLineNumber != logModel.currentDebugLineNumber) {
			System.out.println("new lines detected for debug");
			System.out.println(logModel.previousDebugLineNumber + " != " + logModel.currentDebugLineNumber);

			updateEmbedFn(LogHelperCommand.embedDebugMessageId, embedDebug, logModel.debugLogsList);
			LogHelperCommand.logModel.savePreviousLineNumber("debug");

		} else if (logModel.previousErrorLineNumber != logModel.currentErrorLineNumber) {
			System.out.println("new lines detected for error");
			System.out.println(logModel.previousErrorLineNumber + " != " + logModel.currentErrorLineNumber);

			updateEmbedFn(LogHelperCommand.embedErrorMessageId, embedError, logModel.errorLogsList);
			LogHelperCommand.logModel.savePreviousLineNumber("error");
		} else {
			// System.out.println("All logs are up to date.");
		}
	}

	private void updateEmbedFn(long messageId, EmbedBuilder embed, ArrayList<LogModel> logModel) {
		System.out.println("updateEmbed()");
		try {

			String message = convertArrayListToOneLine((logModel));
			System.out.println("will print this on discord...");
			System.out.println("message size : " + message.length());
			//

			embedVerbose.setTitle("updated title");
			embedVerbose.setFooter(message);
			System.out.println("messageId : " + messageId);

			textChannel.editMessageById(String.valueOf(messageId), embed.build()).queue();
			// textChannel.editMessageById(String.valueOf(embedErrorMessageId), embed.build()).queue();

			Thread.sleep(5000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// convert last 20(array element) lines into one line
	String convertArrayListToOneLine(ArrayList<LogModel> logModel) {
		System.out.println("reached here");
		String delim = "\n";
		StringBuilder sb = new StringBuilder();
		int maxRowsToDisplayInDiscord = 20;

		int i;
		if (logModel.size() > maxRowsToDisplayInDiscord) {
			i = logModel.size() - 20;
		} else {
			i = 0;
		}
		// System.out.println(logModel.get(i).getLogTimestamp() +
		// logModel.get(i).getLogType() + logModel.get(i).getLogMessage());
		while (i <= logModel.size() - 2) {
			sb.append(
					logModel.get(i).getLogTimestamp() + logModel.get(i).getLogType() + logModel.get(i).getLogMessage());
			sb.append(delim);
			System.out.println("i : " + i);
			i++;
		}
		sb.append(logModel.get(i).getLogTimestamp() + logModel.get(i).getLogType() + logModel.get(i).getLogMessage());

		String res = sb.toString();
		System.out.println(res);

		String log = res; // reverse the string to split by timestamp
//		log = splitByTimestamp(log); // to ensure that there is no message cut into half due to 2048 maxlength for a
//										// string
//		log = reverseString(log); // revert the reverse
		return log;
	}

	private void createBaseEmbed() {
		System.out.println("sendVerbose()");
		// TODO Auto-generated method stub

		textChannel.sendMessage(debugMessage).queue((message) -> {
			LogHelperCommand.embedDebugMessageId = message.getIdLong();
			System.out.println("LogHelperCommand.embedDebugMessageId : " + LogHelperCommand.embedDebugMessageId);
		});

		textChannel.sendMessage(errorMessage).queue((message) -> {
			LogHelperCommand.embedErrorMessageId = message.getIdLong();
			System.out.println("LogHelperCommand.embedErrorMessageId : " + LogHelperCommand.embedErrorMessageId);

		});

		textChannel.sendMessage(verboseMessage).queue((message) -> {
			LogHelperCommand.embedVerboseMessageId = message.getIdLong();
			System.out.println("LogHelperCommand.embedVerboseMessageId : " + LogHelperCommand.embedVerboseMessageId);

		});
	}

	private String reverseString(String input) {
		return new StringBuilder(input).reverse().toString();
	}

	private String splitByTimestamp(String log) {
		// System.out.println("log : " + log);
		String[] parts = log.split("\\[");
		for (int i = 0; i < parts.length; i++) {
			// System.out.println("part : [" + reverseString(parts[i]));
		}

		return mergePartsIntoOneLine(parts);
	}

	private String mergePartsIntoOneLine(String[] parts) {
		String oneLine = "";
		for (int i = 0; i < parts.length; i++) {
			oneLine += "[" + parts[i];
		}
		// System.out.println("oneLine : " + oneLine);
		return oneLine;
	}
}

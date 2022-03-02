import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

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

	public Object lock = this;
	public boolean pause = false;

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) { // so that it wont check for bot's reply
			new LogReader();
			msgid = event.getMessageIdLong();

			fileReadThread.start();

			textChannel = event.getGuild().getTextChannelsByName("log-helper", true).get(0);

			// create new embed
			embedDebug.setAuthor("Debug Log");
			embedError.setAuthor("Error Log");
			embedVerbose.setAuthor("Verbose Log");

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

	// run initially
	Thread fileReadThread = new Thread() {
		public void run() {
			System.out.println("fileReadThread running");
			LogReader.readFile();
		}
	};

	// run periodically
	Thread updateEmbedMessageThread = new Thread() {
		public void run() {
			while (true) {
				lineNumbersOutOfSync(); // if there is new line, then only update embed message

				try {
					Thread.sleep(2000); // 2 seconds interval to check for new lines in log files
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private void lineNumbersOutOfSync() {
		System.out.println("lineNumbersOutOfSync(), if not same then should update");
		System.out.println(
				"verbose : " + logModel.previousVerboseLineNumber + " != " + logModel.currentVerboseLineNumber);
		System.out.println("debug : " + logModel.previousDebugLineNumber + " != " + logModel.currentDebugLineNumber);
		System.out.println("error : " + logModel.previousErrorLineNumber + " != " + logModel.currentErrorLineNumber);
		pauseThread();
		if (logModel.previousVerboseLineNumber != logModel.currentVerboseLineNumber) {
			updateVerboseEmbed();
		} else if (logModel.previousDebugLineNumber != logModel.currentDebugLineNumber) {
			updateDebugEmbed();
		} else if (logModel.previousErrorLineNumber != logModel.currentErrorLineNumber) {
			updateErrorEmbed();
		} else {
			updateDebugElapsedTime();
			updateErrorElapsedTime();
			updateVerboseElapsedTime();
			updateAllEmbed(); // update elapsed time
		}
		pauseThread();
	}

	private void updateDebugElapsedTime() {
		embedDebug.setTitle("Elapsed time since last log " + getLastLogTimestamp(logModel.debugLastLogTimestamp));
	}
	
	private void updateErrorElapsedTime() {
		embedError.setTitle("Elapsed time since last log " + getLastLogTimestamp(logModel.errorLastLogTimestamp));
	}
	
	private void updateVerboseElapsedTime() {
		embedVerbose.setTitle("Elapsed time since last log " + getLastLogTimestamp(logModel.verboseLastLogTimestamp));
	}

	private void updateAllEmbed() {
		updateVerboseEmbed();
		updateErrorEmbed();
		updateDebugEmbed();
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
			sb.append(logModel.get(i).getLogTimestamp() + "     " + logModel.get(i).getLogType() + "     "
					+ logModel.get(i).getLogMessage());
			sb.append(delim);
			i++;
		}
		sb.append(logModel.get(i).getLogTimestamp() + "     " + logModel.get(i).getLogType() + "     "
				+ logModel.get(i).getLogMessage());
		saveLastLogTimeStamp(logModel.get(i).getLogTimestamp(), logModel.get(i).getLogType());

		String res = "";
		try {
			res = sb.toString();

		} catch (Exception e) {
			throw e;
		}

		String log = res;
		return log;
	}

	private void saveLastLogTimeStamp(String logTimestamp, String logType) {
		System.out.println("logType : " + logType);
		switch (logType) {
		case "(Verbose)": // TODO: constants
			System.out.println("(verbose) : " + logTimestamp);
			logModel.verboseLastLogTimestamp = logTimestamp;
			updateVerboseElapsedTime();
			break;
		case "(Debug)":
			System.out.println("(debug) : " + logTimestamp);
			logModel.debugLastLogTimestamp = logTimestamp;
			updateDebugElapsedTime();
			break;
		case "(Error)":
			System.out.println("(error) : " + logTimestamp);
			logModel.errorLastLogTimestamp = logTimestamp;
			updateErrorElapsedTime();
			break;
		}
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

	private void updateVerboseEmbed() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateEmbed(LogHelperCommand.embedVerboseMessageId, embedVerbose, logModel.verboseLogsList);
		textChannel.editMessageById(String.valueOf(embedVerboseMessageId), embedVerbose.build()).queue();
		LogHelperCommand.logModel.savePreviousLineNumber("verbose");
	};

	private void updateDebugEmbed() {
		updateEmbed(LogHelperCommand.embedDebugMessageId, embedDebug, logModel.debugLogsList);
		textChannel.editMessageById(String.valueOf(embedDebugMessageId), embedDebug.build()).queue();
		LogHelperCommand.logModel.savePreviousLineNumber("debug");
	};

	private void updateErrorEmbed() {
		updateEmbed(LogHelperCommand.embedErrorMessageId, embedError, logModel.errorLogsList);
		textChannel.editMessageById(String.valueOf(embedErrorMessageId), embedError.build()).queue();
		LogHelperCommand.logModel.savePreviousLineNumber("error");
	};

	private void updateEmbed(long messageId, EmbedBuilder embed, ArrayList<LogModel> logModel) {
		System.out.println("updating embed in discord()");
		try {
			// String elapsedTimeSinceLastLog =
			// getLastLogTimestamp(logModel.get(logModel.size() - 1).getLogTimestamp());
			String message = convertArrayListToOneLine((logModel));

			// embed.setTitle("Elapsed time since last log " + elapsedTimeSinceLastLog);
			embed.setFooter(message);
			// textChannel.editMessageById(String.valueOf(embedErrorMessageId),
			// embed.build()).queue();
			Thread.sleep(545);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String getLastLogTimestamp(String lastLogTimestamp) {
		// 02/03/2022 02:05:41
		System.out.println("lastLogTimestamp : " + lastLogTimestamp);
		String lastLogTimeStamp = lastLogTimestamp.replaceAll("\\[", "").replaceAll("\\]", "");
		String currentTimeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		LocalDateTime arrival = LocalDateTime.parse(lastLogTimeStamp, fmt);
		LocalDateTime scheduled = LocalDateTime.parse(currentTimeStamp, fmt);
		long seconds = ChronoUnit.SECONDS.between(arrival, scheduled);
		System.out.println("last log time is : " + seconds);
		return beautifySeconds(seconds);
	};

	private String beautifySeconds(long seconds) {
		if (seconds <= 60) {
			return String.valueOf(seconds + " seconds.");
		} else {
			int minute = (int) (seconds / 60);
			int remainingSeconds = (int) (seconds % 60);
			return String.valueOf(minute + (minute == 1 ? " minute " : " minutes ") + remainingSeconds + " seconds.");
		}
	}

	private void threadSleep(int timer) {
		try {
			Thread.sleep(timer);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // waiting for embed creation
	}

	public void pause() {
		pause = true;
	}

	public void continueThread() {
		pause = false;
	}

	private void synchronizedThread(Object lock) {
		lock.notifyAll();
	}

	private void pauseThread() {
		synchronized (lock) {
			if (pause)
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Note that this can cause an InterruptedException
		}
	}
}

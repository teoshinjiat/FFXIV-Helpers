import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

import model.LogContents;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import service.LogHelperService;

public class LogHelperCommand extends ListenerAdapter {
	// public static LogModel logModel = new LogModel();
	public static LogHelperService logHelperService = new LogHelperService();

	public static EmbedBuilder embedDebug = new EmbedBuilder();
	public static EmbedBuilder embedError = new EmbedBuilder();
	public static EmbedBuilder embedVerbose = new EmbedBuilder();
	public static EmbedBuilder embedRetainer = new EmbedBuilder();
	public static EmbedBuilder embedRetainerUndercut = new EmbedBuilder();

	public static MessageEmbed debugMessage;
	public static MessageEmbed errorMessage;
	public static MessageEmbed verboseMessage;
	public static MessageEmbed retainerMessage;
	public static MessageEmbed retainerUndercutMessage;

	public static long embedDebugMessageId;
	public static long embedErrorMessageId;
	public static long embedVerboseMessageId;
	public static long embedRetainerMessageId;
	public static long embedRetainerUndercutMessageId;

	public static TextChannel logHelperChannel;
	public static TextChannel retainerNotificationChannel;

	public Object lock = this;
	public boolean pause = false;

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) { // so that it wont check for bot's reply
			new LogReader();
			fileReadThread.start();

			logHelperChannel = event.getGuild().getTextChannelsByName("log-helper", true).get(0);
			retainerNotificationChannel = event.getGuild().getTextChannelsByName("retainer-notification", true).get(0);

			// create new embed
			embedDebug.setAuthor("Debug Log");
			embedError.setAuthor("Error Log");
			embedVerbose.setAuthor("Verbose Log");
			embedRetainer.setAuthor("Retainer Log");
			embedRetainerUndercut.setAuthor("Retainer Undercut Alert Log");

			embedDebug.setColor(Color.GRAY);
			embedError.setColor(Color.RED);
			embedVerbose.setColor(Color.YELLOW);
			embedRetainer.setColor(Color.CYAN);
			embedRetainerUndercut.setColor(Color.CYAN);

			debugMessage = embedDebug.build();
			errorMessage = embedError.build();
			verboseMessage = embedVerbose.build();
			retainerMessage = embedRetainer.build();
			retainerUndercutMessage = embedRetainerUndercut.build();

			createBaseEmbed();

			updateEmbedMessageThread.start(); // thread for periodically update the embed message in discord
			return;
		}
		// updateLogListener();
	}

	private void createBaseEmbed() {
		// TODO Auto-generated method stub
		logHelperChannel.sendMessage(debugMessage).queue((message) -> {
			LogHelperCommand.embedDebugMessageId = message.getIdLong();
		});

		logHelperChannel.sendMessage(errorMessage).queue((message) -> {
			LogHelperCommand.embedErrorMessageId = message.getIdLong();
		});

		logHelperChannel.sendMessage(verboseMessage).queue((message) -> {
			LogHelperCommand.embedVerboseMessageId = message.getIdLong();
		});

		logHelperChannel.sendMessage(retainerMessage).queue((message) -> {
			LogHelperCommand.embedRetainerMessageId = message.getIdLong();
		});

		logHelperChannel.sendMessage(retainerUndercutMessage).queue((message) -> {
			LogHelperCommand.embedRetainerUndercutMessageId = message.getIdLong();
		});
	}

	// run initially
	Thread fileReadThread = new Thread() {
		public void run() {
			while (true) {
				// rerun thread every 5s because file might be renamed for archieving function
				System.out.println("fileReadThread()");
				new LogReader();
				// LogReader.readFile();
				LogReader.readFile();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // 2 seconds interval to check for new lines in log files
			}
		}
	};

	// run periodically
	Thread updateEmbedMessageThread = new Thread() {
		public void run() {
			System.out.println("updateEmbedMessageThread()");
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
		pauseThread();
		if (logHelperService.verboseModel.previousLineNumber < logHelperService.verboseModel.currentLineNumber) {
			System.out.println("Updating verboseModel");
			updateVerboseEmbed(false);
		} else if (logHelperService.debugModel.previousLineNumber < logHelperService.debugModel.currentLineNumber) {
			System.out.println("Updating debugModel");
			updateDebugEmbed(false);
		} else if (logHelperService.errorModel.previousLineNumber < logHelperService.errorModel.currentLineNumber) {
			System.out.println("Updating errorModel");
			updateErrorEmbed(false);
			alertMe("<@" + Constants.userId + "> An error occurred in AHK script.");
		} else if (logHelperService.retainerModel.previousLineNumber < logHelperService.retainerModel.currentLineNumber) {
			System.out.println("Updating retainerModel");
			updateRetainerEmbed(false);
		} else if (logHelperService.retainerUndercutModel.previousLineNumber < logHelperService.retainerUndercutModel.currentLineNumber) {
			System.out.println("Updating retainerUndercutModel");
			updateRetainerUndercutEmbed(false);
			alertMe("<@" + Constants.userId + "> One item have been undercut from AHK script.");
		} else { // if all lines are the same, then only update elapsed time
			System.out.println("Updating all elapsed timestamp");
			updateAllElapsedTimeEmbed(); // update elapsed time
		}
		pauseThread();
	}

	private void updateAllElapsedTimeEmbed() { // used for updating elapsed time
		if (!logHelperService.verboseModel.logList.isEmpty()
				&& !logHelperService.verboseModel.lastLogTimestamp.isBlank()) {
			updateVerboseElapsedTime();
			updateVerboseEmbed(true);
		}
		if (!logHelperService.errorModel.logList.isEmpty() && !logHelperService.errorModel.lastLogTimestamp.isBlank()) {
			updateErrorElapsedTime();
			updateErrorEmbed(true);
		}
		if (!logHelperService.debugModel.logList.isEmpty() && !logHelperService.debugModel.lastLogTimestamp.isBlank()) {
			updateDebugElapsedTime();
			updateDebugEmbed(true);
		}
		if (!logHelperService.retainerModel.logList.isEmpty()
				&& !logHelperService.retainerModel.lastLogTimestamp.isBlank()) {
			updateRetainerElapsedTime();
			updateRetainerEmbed(true);
		}

		if (!logHelperService.retainerUndercutModel.logList.isEmpty()
				&& !logHelperService.retainerUndercutModel.lastLogTimestamp.isBlank()) {
			updateRetainerUndercutElapsedTime();
			updateRetainerUndercutEmbed(true);
		}
	}

	private void updateDebugElapsedTime() {
		if (!logHelperService.debugModel.lastLogTimestamp.isBlank()) {
			embedDebug.setTitle(
					"Elapsed time since last log " + getLastLogTimestamp(logHelperService.debugModel.lastLogTimestamp));
		}
	}

	private void updateErrorElapsedTime() {
		if (!logHelperService.errorModel.lastLogTimestamp.isBlank()) {
			embedError.setTitle(
					"Elapsed time since last log " + getLastLogTimestamp(logHelperService.errorModel.lastLogTimestamp));
		}
	}

	private void updateVerboseElapsedTime() {
		if (!logHelperService.verboseModel.lastLogTimestamp.isBlank()) {
			embedVerbose.setTitle("Elapsed time since last log "
					+ getLastLogTimestamp(logHelperService.verboseModel.lastLogTimestamp));
		}
	}

	private void updateRetainerElapsedTime() {
		if (!logHelperService.retainerModel.lastLogTimestamp.isBlank()) {
			embedRetainer.setTitle("Elapsed time since last log "
					+ getLastLogTimestamp(logHelperService.retainerModel.lastLogTimestamp));
		}
	}

	private void updateRetainerUndercutElapsedTime() {
		if (!logHelperService.retainerUndercutModel.lastLogTimestamp.isBlank()) {
			embedRetainerUndercut.setTitle("Elapsed time since last log "
					+ getLastLogTimestamp(logHelperService.retainerUndercutModel.lastLogTimestamp));
		}
	}

	// convert last 20(array element) lines into one line
	String convertArrayListToOneLine(ArrayList<LogContents> logList) {
		String delim = "\n";
		StringBuilder sb = new StringBuilder();
		int maxRowsToDisplayInDiscord = 20;

		int i;
		if (logList.size() > maxRowsToDisplayInDiscord) {
			i = logList.size() - 20;
		} else {
			i = 0;
		}
		while (i <= logList.size() - 2) {
			sb.append(logList.get(i).getLogTimestamp() + "     " + logList.get(i).getLogType() + "     "
					+ logList.get(i).getLogMessage());
			sb.append(delim);
			i++;
		}
		sb.append(logList.get(i).getLogTimestamp() + "     " + logList.get(i).getLogType() + "     "
				+ logList.get(i).getLogMessage());

		saveLastLogTimeStamp(logList.get(i).getLogTimestamp(), logList.get(i).getLogType());

		String log = "";
		try {
			log = sb.toString();

		} catch (Exception e) {
			throw e;
		}

		if (log.length() > 2048) {
			log = log.substring(log.length() - 2048); // string max limit TODO: make it to array of strings when its
														// more than 2048
		}

		return log;
	}

	private void saveLastLogTimeStamp(String logTimestamp, String logType) {
		switch (logType) {
		case "(Verbose)": // TODO: constants
			logHelperService.verboseModel.lastLogTimestamp = logTimestamp;
			updateVerboseElapsedTime();
			break;
		case "(Debug)":
			logHelperService.debugModel.lastLogTimestamp = logTimestamp;
			updateDebugElapsedTime();
			break;
		case "(Error)":
			logHelperService.errorModel.lastLogTimestamp = logTimestamp;
			updateErrorElapsedTime();
			break;
		case "(Retainer)":
			logHelperService.retainerModel.lastLogTimestamp = logTimestamp;
			updateRetainerElapsedTime();
			break;
		case "(Retainer Undercut)":
			logHelperService.retainerUndercutModel.lastLogTimestamp = logTimestamp;
			updateRetainerUndercutElapsedTime();
			break;
		default:
			break;
		}
	}

	private void updateVerboseEmbed(boolean updateElapsedOnly) {
		updateEmbed(LogHelperCommand.embedVerboseMessageId, embedVerbose, logHelperService.verboseModel.logList);
		logHelperChannel.editMessageById(String.valueOf(embedVerboseMessageId), embedVerbose.build()).queue();
		// LogHelperCommand.logModel.savePreviousLineNumber("verbose");
		if (!updateElapsedOnly) {
			logHelperService.verboseModel.savePreviousLineNumber();
		}
	};

	private void updateDebugEmbed(boolean updateElapsed) {
		updateEmbed(LogHelperCommand.embedDebugMessageId, embedDebug, logHelperService.debugModel.logList);
		logHelperChannel.editMessageById(String.valueOf(embedDebugMessageId), embedDebug.build()).queue();
		if (!updateElapsed) {
			logHelperService.debugModel.savePreviousLineNumber();
		}
	};

	private void updateErrorEmbed(boolean updateElapsed) {
		updateEmbed(LogHelperCommand.embedErrorMessageId, embedError, logHelperService.errorModel.logList);
		logHelperChannel.editMessageById(String.valueOf(embedErrorMessageId), embedError.build()).queue();
		if (!updateElapsed) {
			logHelperService.errorModel.savePreviousLineNumber();

		}
	};

	private void updateRetainerEmbed(boolean updateElapsed) {
		updateEmbed(LogHelperCommand.embedRetainerMessageId, embedRetainer, logHelperService.retainerModel.logList);
		logHelperChannel.editMessageById(String.valueOf(embedRetainerMessageId), embedRetainer.build()).queue();
		if (!updateElapsed) {
			logHelperService.retainerModel.savePreviousLineNumber();
		}
	};

	private void updateRetainerUndercutEmbed(boolean updateElapsed) {
		updateEmbed(LogHelperCommand.embedRetainerUndercutMessageId, embedRetainerUndercut,
				logHelperService.retainerUndercutModel.logList);
		logHelperChannel.editMessageById(String.valueOf(embedRetainerUndercutMessageId), embedRetainerUndercut.build())
				.queue();
		if (!updateElapsed) {
			logHelperService.retainerUndercutModel.savePreviousLineNumber();
		}
	};

	private void updateEmbed(long messageId, EmbedBuilder embed, ArrayList<LogContents> logList) {
		try {
			// String elapsedTimeSinceLastLog =
			// getLastLogTimestamp(logModel.get(logModel.size() - 1).getLogTimestamp());
			String message = convertArrayListToOneLine((logList));

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
		String lastLogTimeStamp = lastLogTimestamp.replace("[", "").replace("]", "");
		String currentTimeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		LocalDateTime arrival = LocalDateTime.parse(lastLogTimeStamp, fmt);
		LocalDateTime scheduled = LocalDateTime.parse(currentTimeStamp, fmt);
		long seconds = ChronoUnit.SECONDS.between(arrival, scheduled);

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

	private void alertMe(String text) {
		System.out.println("alertMe()");
		retainerNotificationChannel.sendMessage(text).queue(); // async
	}
}

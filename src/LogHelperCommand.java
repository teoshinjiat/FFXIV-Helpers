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

	public static TextChannel textChannel;

	public Object lock = this;
	public boolean pause = false;

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) { // so that it wont check for bot's reply
			new LogReader();
			fileReadThread.start();

			textChannel = event.getGuild().getTextChannelsByName("log-helper", true).get(0);

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
		textChannel.sendMessage(debugMessage).queue((message) -> {
			LogHelperCommand.embedDebugMessageId = message.getIdLong();
		});

		textChannel.sendMessage(errorMessage).queue((message) -> {
			LogHelperCommand.embedErrorMessageId = message.getIdLong();
		});

		textChannel.sendMessage(verboseMessage).queue((message) -> {
			LogHelperCommand.embedVerboseMessageId = message.getIdLong();
		});

		textChannel.sendMessage(retainerMessage).queue((message) -> {
			LogHelperCommand.embedRetainerMessageId = message.getIdLong();
		});

		textChannel.sendMessage(retainerUndercutMessage).queue((message) -> {
			LogHelperCommand.embedRetainerUndercutMessageId = message.getIdLong();
		});
	}

	// run initially
	Thread fileReadThread = new Thread() {
		public void run() {
			System.out.println("fileReadThread()");
			LogReader.readFile();
		}
	};

	// run periodically
	Thread updateEmbedMessageThread = new Thread() {
		public void run() {
			System.out.println("updateEmbedMessageThread()");
			while (true) {
				lineNumbersOutOfSync(); // if there is new line, then only update embed message
				try {
					Thread.sleep(500); // 2 seconds interval to check for new lines in log files
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private void lineNumbersOutOfSync() {
		System.out.println("lineNumbersOutOfSync(), if not same then should update the embed");
		pauseThread();
		if (logHelperService.verboseModel.previousLineNumber != logHelperService.verboseModel.currentLineNumber) {
			updateVerboseEmbed();
		} else if (logHelperService.debugModel.previousLineNumber != logHelperService.debugModel.currentLineNumber) {
			updateDebugEmbed();
		} else if (logHelperService.errorModel.previousLineNumber != logHelperService.errorModel.currentLineNumber) {
			updateErrorEmbed();
			alertMe("<@" + Constants.userId + "> An error occurred in AHK script.");
		} else if (logHelperService.retainerModel.previousLineNumber != logHelperService.retainerModel.currentLineNumber) {
			updateRetainerEmbed();
		} else if (logHelperService.retainerUndercutModel.previousLineNumber != logHelperService.retainerUndercutModel.currentLineNumber) {
			updateRetainerUndercutEmbed();
			alertMe("<@" + Constants.userId + "> One item have been undercut from AHK script.");
		} else {
			updateAllElapsedTimeEmbed(); // update elapsed time
		}
		pauseThread();
	}

	private void updateAllElapsedTimeEmbed() { // used for updating elapsed time
		if (!logHelperService.verboseModel.logList.isEmpty()
				&& !logHelperService.verboseModel.lastLogTimestamp.isBlank()) {
			updateVerboseElapsedTime();
			updateVerboseEmbed();
		}
		if (!logHelperService.errorModel.logList.isEmpty() && !logHelperService.errorModel.lastLogTimestamp.isBlank()) {
			updateErrorElapsedTime();
			updateErrorEmbed();
		}
		if (!logHelperService.debugModel.logList.isEmpty() && !logHelperService.debugModel.lastLogTimestamp.isBlank()) {
			updateDebugElapsedTime();
			updateDebugEmbed();
		}
		if (!logHelperService.retainerModel.logList.isEmpty()
				&& !logHelperService.retainerModel.lastLogTimestamp.isBlank()) {
			updateRetainerElapsedTime();
			updateRetainerEmbed();
		}

		System.out.println("logHelperService.retainerUndercutModel.logList.isEmpty() : "
				+ logHelperService.retainerUndercutModel.logList.isEmpty());
		System.out.println("logHelperService.retainerUndercutModel.lastLogTimestamp.isBlank() : "
				+ logHelperService.retainerUndercutModel.lastLogTimestamp.isBlank());
		if (!logHelperService.retainerUndercutModel.logList.isEmpty()
				&& !logHelperService.retainerUndercutModel.lastLogTimestamp.isBlank()) {
			updateRetainerUndercutElapsedTime();
			updateRetainerUndercutEmbed();
		}
	}

	private void updateDebugElapsedTime() {
		System.out.println("updateDebugElapsedTime()");
		if (!logHelperService.debugModel.lastLogTimestamp.isBlank()) {
			embedDebug.setTitle(
					"Elapsed time since last log " + getLastLogTimestamp(logHelperService.debugModel.lastLogTimestamp));
		}
	}

	private void updateErrorElapsedTime() {
		System.out.println("updateErrorElapsedTime()");
		if (!logHelperService.errorModel.lastLogTimestamp.isBlank()) {
			embedError.setTitle(
					"Elapsed time since last log " + getLastLogTimestamp(logHelperService.errorModel.lastLogTimestamp));
		}
	}

	private void updateVerboseElapsedTime() {
		System.out.println("updateVerboseElapsedTime()");
		if (!logHelperService.verboseModel.lastLogTimestamp.isBlank()) {
			embedVerbose.setTitle("Elapsed time since last log "
					+ getLastLogTimestamp(logHelperService.verboseModel.lastLogTimestamp));
		}
	}

	private void updateRetainerElapsedTime() {
		System.out.println("updateRetainerElapsedTime()");
		if (!logHelperService.retainerModel.lastLogTimestamp.isBlank()) {
			embedRetainer.setTitle("Elapsed time since last log "
					+ getLastLogTimestamp(logHelperService.retainerModel.lastLogTimestamp));
		}
	}

	private void updateRetainerUndercutElapsedTime() {
		System.out.println("updateRetainerUndercutElapsedTime()");
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

		System.out.println("logList.get(i).getLogTimestamp() : " + logList.get(i).getLogTimestamp());
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
		System.out.println("logType : " + logType);
		System.out.println("String logTimestamp : " + logTimestamp);

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

	private void updateVerboseEmbed() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateEmbed(LogHelperCommand.embedVerboseMessageId, embedVerbose, logHelperService.verboseModel.logList);
		textChannel.editMessageById(String.valueOf(embedVerboseMessageId), embedVerbose.build()).queue();
		// LogHelperCommand.logModel.savePreviousLineNumber("verbose");
		logHelperService.verboseModel.savePreviousLineNumber();
	};

	private void updateDebugEmbed() {
		updateEmbed(LogHelperCommand.embedDebugMessageId, embedDebug, logHelperService.debugModel.logList);
		textChannel.editMessageById(String.valueOf(embedDebugMessageId), embedDebug.build()).queue();
		logHelperService.debugModel.savePreviousLineNumber();

	};

	private void updateErrorEmbed() {
		updateEmbed(LogHelperCommand.embedErrorMessageId, embedError, logHelperService.errorModel.logList);
		textChannel.editMessageById(String.valueOf(embedErrorMessageId), embedError.build()).queue();
		logHelperService.errorModel.savePreviousLineNumber();
	};

	private void updateRetainerEmbed() {
		updateEmbed(LogHelperCommand.embedRetainerMessageId, embedRetainer, logHelperService.retainerModel.logList);
		textChannel.editMessageById(String.valueOf(embedRetainerMessageId), embedRetainer.build()).queue();
		logHelperService.retainerModel.savePreviousLineNumber();
	};

	private void updateRetainerUndercutEmbed() {
		updateEmbed(LogHelperCommand.embedRetainerUndercutMessageId, embedRetainerUndercut,
				logHelperService.retainerUndercutModel.logList);
		textChannel.editMessageById(String.valueOf(embedRetainerUndercutMessageId), embedRetainerUndercut.build())
				.queue();
		logHelperService.retainerUndercutModel.savePreviousLineNumber();
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
		System.out.println("lastLogTimeStamp : " + lastLogTimeStamp);
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
		textChannel.sendMessage(text).queue();
	}
}

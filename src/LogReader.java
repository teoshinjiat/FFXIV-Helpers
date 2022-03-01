
import java.io.IOException;
import java.io.RandomAccessFile;

import model.LogModel;

public class LogReader {
	public static LogModel logModel = LogHelperCommand.logModel;

	public static void main(String[] args) {
		System.out.println("LogReader() initialized");
		readFile();
	}

	public static void readFile() {
		try {
			// from https://stackoverflow.com/a/53013944
			RandomAccessFile bufferedReader = new RandomAccessFile(Constants.logFilePath, "r");
			long filePointer;
			LogHelperCommand.logModel.savePreviousLineNumber(""); // initialize line numbers
			while (true) {
				final String string = bufferedReader.readLine();

				if (string != null) { // if eof, go to else and watch for new lines
					// System.out.println("eof reached");
					String line = new String(string.getBytes("ISO-8859-1"), "UTF-8");
					// System.out.println("line : " + line);
					processLine(line);
				} else {
					// System.out.println("else watch for lines pointer every 5s");
					filePointer = bufferedReader.getFilePointer();
					bufferedReader.close();
					Thread.sleep(5000);
					bufferedReader = new RandomAccessFile(Constants.logFilePath, "r");
					bufferedReader.seek(filePointer);
				}

			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void processLine(String line) {
		String[] parts = line.split("\\|", 3);

		if (line.contains(Constants.verboseTag)) {
			logModel.verboseLogsList.add(setter(parts));
			logModel.currentVerboseLineNumber = logModel.verboseLogsList.size();
		} else if (line.contains(Constants.debugTag)) {
			logModel.debugLogsList.add(setter(parts));
			logModel.currentDebugLineNumber = logModel.debugLogsList.size();
		} else if (line.contains(Constants.errorTag)) {
			System.out.println("error log detected");

			logModel.errorLogsList.add(setter(parts));
			logModel.currentErrorLineNumber = logModel.errorLogsList.size();
		}
	}

	private static LogModel setter(String[] parts) {
		LogModel logModel = new LogModel();
		logModel.setLogTimestamp(parts[0]);
		logModel.setLogType(parts[1]);
		logModel.setLogMessage(parts[2]);
		// System.out.println("here : " + parts[0] + " " + parts[1] + " " + parts[2]);
		return logModel;
	}
}

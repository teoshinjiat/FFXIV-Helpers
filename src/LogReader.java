
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import model.LogContents;
import service.LogHelperService;

public class LogReader {
	public static LogHelperService logHelperService = LogHelperCommand.logHelperService;

	public static void main(String[] args) {
		System.out.println("LogReader() initialized");
		readFile();
	}

	public static void readFile() {
		try {
			// LogHelperCommand.logModel.savePreviousLineNumber(""); // initialize line
			// numbers
			while (true) {
				File file1 = new File(Constants.logFilePath);
				if (file1.exists() && !file1.isDirectory()) {
					// from https://stackoverflow.com/a/53013944
					RandomAccessFile bufferedReader = new RandomAccessFile(Constants.logFilePath, "r");
					long filePointer;

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

				} else {
					// System.out.println(file1 + " Does not exists");
				}
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void processLine(String line) {
		String[] parts = line.split("\\|", 3);

		if (line.contains(Constants.verboseTag)) {
			logHelperService.verboseModel.logList.add(setter(parts));
			logHelperService.verboseModel.currentLineNumber = logHelperService.verboseModel.logList.size();
		} else if (line.contains(Constants.debugTag)) {
			logHelperService.debugModel.logList.add(setter(parts));
			logHelperService.debugModel.currentLineNumber = logHelperService.debugModel.logList.size();
		} else if (line.contains(Constants.errorTag)) {
			logHelperService.errorModel.logList.add(setter(parts));
			logHelperService.errorModel.currentLineNumber = logHelperService.errorModel.logList.size();
		} else if (line.contains(Constants.retainerTag)) {
			logHelperService.retainerModel.logList.add(setter(parts));
			logHelperService.retainerModel.currentLineNumber = logHelperService.retainerModel.logList.size();
		} else if (line.contains(Constants.retainerUndercutTag)) {
			logHelperService.retainerUndercutModel.logList.add(setter(parts));
			logHelperService.retainerUndercutModel.currentLineNumber = logHelperService.retainerUndercutModel.logList
					.size();
		}
	}

	private static LogContents setter(String[] parts) {
		LogContents logModel = new LogContents();
	//	System.out.println("parts[0].replace(\"\\\\?\", \"\") : " + parts[0].replaceFirst("\\?", ""));
		logModel.setLogTimestamp(parts[0].replace("?", ""));
		logModel.setLogType(parts[1]);
		logModel.setLogMessage(parts[2]);
		// System.out.println("LogContents : " + parts[0] + " " + parts[1] + " " + parts[2]);
		return logModel;
	}
}

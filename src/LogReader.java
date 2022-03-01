
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import model.LogModel;
import net.dv8tion.jda.api.entities.MessageEmbed;


public class LogReader {
	private static LogModel logModel = LogHelperCommand.logModel;

	public static void main(String[] args) {
		System.out.println("LogReader() initialized");
		readFile();
	}

	public static void readFile() {
		try {
			// from https://stackoverflow.com/a/53013944
			RandomAccessFile bufferedReader = new RandomAccessFile(Constants.logFilePath, "r");
			long filePointer;
			while (true) {
				final String string = bufferedReader.readLine();

				if (string != null) { // if eof, go to else and watch for new lines
					//System.out.println("eof reached");
					String line = new String(string.getBytes("ISO-8859-1"), "UTF-8");
					// System.out.println("line : " + line);
					processLine(line);
				} else {
					//System.out.println("else watch for lines pointer every 5s");
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

	public static void printLastLine() {
		System.out.println("logModel.previousVerboseSize :" + logModel.previousVerboseSize);
		System.out.println("logModel.previousDebugSize :" + logModel.previousDebugSize);
		System.out.println("logModel.previousErrorSize :" + logModel.previousErrorSize);
	}
	
	public static void processLine(String line) {
		if(line.contains(Constants.verboseTag)) {
			logModel.verbose.add(line);
			logModel.previousVerboseSize++;
		} else if(line.contains(Constants.debugTag)) {
			logModel.debug.add(line);
			logModel.previousDebugSize++;
		} else {
			logModel.error.add(line);
			logModel.previousErrorSize++;
		}
		//printLastLine();
	}
}

package model;

import java.util.ArrayList;

public class LogModel {
	public ArrayList<LogContents> logList = new ArrayList<LogContents>();
	public int previousLineNumber = 0;
	public int currentLineNumber = 0;
	public String lastLogTimestamp = "";

	public ArrayList<LogContents> getLogList() {
		return logList;
	}

	public void setLogList(ArrayList<LogContents> logList) {
		this.logList = logList;
	}

	public int getPreviousLineNumber() {
		return previousLineNumber;
	}

	public void setPreviousLineNumber(int previousLineNumber) {
		this.previousLineNumber = previousLineNumber;
	}

	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	public void setCurrentLineNumber(int currentLineNumber) {
		this.currentLineNumber = currentLineNumber;
	}

	public String getLastLogTimestamp() {
		return lastLogTimestamp;
	}

	public void setLastLogTimestamp(String lastLogTimestamp) {
		this.lastLogTimestamp = lastLogTimestamp;
	}

	public void savePreviousLineNumber() {
		this.previousLineNumber = this.logList.size();
	}
}

package model;

import java.util.ArrayList;
import java.util.List;

public class LogModel extends LogContents {
	public ArrayList<LogModel> debugLogsList = new ArrayList<LogModel>();
	public ArrayList<LogModel> errorLogsList = new ArrayList<LogModel>();
	public ArrayList<LogModel> verboseLogsList = new ArrayList<LogModel>();

	public int previousDebugLineNumber = 0;
	public int previousErrorLineNumber = 0;
	public int previousVerboseLineNumber = 0;

	public int currentDebugLineNumber = 0;
	public int currentErrorLineNumber = 0;
	public int currentVerboseLineNumber = 0;


	public int fileSize = 0;
	public List<String> lines = new ArrayList<String>();

	public List<String> verbose = new ArrayList<String>();
	public int currentVerboseSize = 0;

	public List<String> debug = new ArrayList<String>();
	public int previousDebugSize = 0;
	public int DebugSize = 0;

	public List<String> error = new ArrayList<String>();
	public int previousErrorSize = 0;
	public int currentErrorSize = 0;
	public String previousVerboseSize;
	
	public void savePreviousLineNumber(String flag) {
		switch(flag) {
		case "verbose":
			this.previousVerboseLineNumber = this.verboseLogsList.size();
		case "debug":
			this.previousDebugLineNumber = this.debugLogsList.size();
		case "error":
			this.previousErrorLineNumber = this.errorLogsList.size();
		default:
			this.previousVerboseLineNumber = this.verboseLogsList.size();
			this.previousDebugLineNumber = this.debugLogsList.size();
			this.previousErrorLineNumber = this.errorLogsList.size();
		}
	}

}

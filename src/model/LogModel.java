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
	
	public String debugLastLogTimestamp = "";
	public String errorLastLogTimestamp = "";
	public String verboseLastLogTimestamp = "";



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
		System.out.println("flagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflagflag : " + flag);
		switch(flag) {
		case "verbose":
			System.out.println("verbose only");
			this.previousVerboseLineNumber = this.verboseLogsList.size();
			break;
		case "debug":
			System.out.println("debug only");
			this.previousDebugLineNumber = this.debugLogsList.size();
			break;
		case "error":
			System.out.println("error only");
			this.previousErrorLineNumber = this.errorLogsList.size();
			break;
		default:
			System.out.println("DEFAULT FOR ALL");
			this.previousVerboseLineNumber = this.verboseLogsList.size();
			this.previousDebugLineNumber = this.debugLogsList.size();
			this.previousErrorLineNumber = this.errorLogsList.size();
			break;
		}
	}

}

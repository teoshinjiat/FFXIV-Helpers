package model;

import java.util.ArrayList;
import java.util.List;

public class LogModel {
	public int fileSize = 0;
	public List<String> lines = new ArrayList<String>();
	
	public List<String> verbose = new ArrayList<String>();
	public int previousVerboseSize = 0;
	public int currentVerboseSize = 0;

	public List<String> debug = new ArrayList<String>();
	public int previousDebugSize = 0;
	public int DebugSize = 0;

	public List<String> error = new ArrayList<String>();
	public int previousErrorSize = 0;
	public int currentErrorSize = 0;
	
}

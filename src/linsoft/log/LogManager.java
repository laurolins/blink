package linsoft.log;

import java.io.IOException;
import java.util.Vector;


public class LogManager {
	private Vector _logs;


	public LogManager() {
		_logs = new Vector();
	}


	/**
	 * Retorna o índice do log dentro do array _logs.
	 */
	public int addLog(String name, String filename, boolean persistent) throws IOException {
		Log log = new Log(name, filename, persistent);
		int index = _logs.size();
		_logs.add(log);
		return index;
	}


	private Log getLogByName(String logName) {
		Log log = null;
		boolean achou = false;
		for(int i = 0; !achou && i < _logs.size(); i++) {
			log = (Log) _logs.get(i);
			if(log.getName().equals(logName)) achou = true;
		}
		if(!achou)
			log = null;
		return log;
	}


	public void write(int logIndex, String msg) throws IOException {
		Log log = (Log) _logs.get(logIndex);
		log.write(msg);
	}


	public void write(String logName, String msg) throws IOException {
		Log log = getLogByName(logName);
		if(log != null)
			log.write(msg);
		else throw new RuntimeException("Log " + logName + " não existe.");
	}


	public void writeWithTimestamp(int logIndex, String msg) throws IOException {
		Log log = (Log) _logs.get(logIndex);
		log.writeWithTimestamp(msg);
	}


	public void writeWithTimestamp(String logName, String msg) throws IOException {
		Log log = getLogByName(logName);
		if(log != null)
			log.writeWithTimestamp(msg);
		else throw new RuntimeException("Log " + logName + " não existe.");
	}


	public void setEnabled(int logIndex, boolean enabled) {
		Log log = (Log) _logs.get(logIndex);
		log.setEnabled(enabled);
	}


	public void setEnabled(String logName, boolean enabled) {
		Log log = getLogByName(logName);
		if(log != null)
			log.setEnabled(enabled);
		else throw new RuntimeException("Log " + logName + " não existe.");
	}


	public static void main(String[] args) {
		LogManager logMan = new LogManager();
		try {
			// não persistente
			int l1 = logMan.addLog("log1", "arq1.log", false);

			// persistente
			int l2 = logMan.addLog("log2", "arq2.log", true);

			String msg = "mensagem 1.";
			logMan.write(l1, msg);
			logMan.write("log2", msg);

			msg = "mensagem 2.";
			logMan.writeWithTimestamp("log1", msg);
			logMan.writeWithTimestamp(l2, msg);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
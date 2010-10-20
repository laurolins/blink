package linsoft.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Log {
	private String _name;
	// private FileWriter _writer;
    private PrintStream _writer;
	private boolean _enabled;
    private boolean _blackHoleLog;

    /**
     * Creates an empty log.
     */
    public Log() {
        // this is a black hole log
        _blackHoleLog = true;
    }

    /**
     * Creates a log
     */
	public Log(String name, String filename, boolean persistent) throws IOException {
		_name = name;
		File file = new File(filename);
		if (!file.exists()) {
		    file.getParentFile().mkdirs();
			file.createNewFile();
		}
        else if (persistent) {
            file.delete();
            file.createNewFile();
        }
		_writer = new PrintStream(new FileOutputStream(file));
		_enabled = true;

        // this is not a black hole log
        _blackHoleLog = false;
	}

    /**
     * Creates a log
     */
    public Log(String name, PrintStream ps) {
        _name = name;
        _writer = ps;
        _enabled = true;

        // this is not a black hole log
        _blackHoleLog = false;
    }

	public String getName() {
		return _name;
	}

	public void write(String msg) {
        /////////////////////////
        // Black Hole Log
        if (_blackHoleLog)
            return;
        // Black Hole Log
        /////////////////////////

        if (_enabled) {
            _writer.print(msg + "\n");
        }
	}


	public void writeWithTimestamp(String msg) {
        /////////////////////////
        // Black Hole Log
        if (_blackHoleLog)
            return;
        // Black Hole Log
        /////////////////////////

        if (_enabled) {
            Calendar calendar = new GregorianCalendar();
            String amPm = " AM";
            if (calendar.get(Calendar.AM_PM) == Calendar.PM)
                amPm = " PM";

            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMinimumIntegerDigits(2);
            formatter.setMaximumIntegerDigits(2);

            String time = "" +
                formatter.format(calendar.get(Calendar.DAY_OF_MONTH)) + "/" +
                formatter.format(calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR) + " " +
                formatter.format(calendar.get(Calendar.HOUR)) + ":" +
                formatter.format(calendar.get(Calendar.MINUTE)) + ":" +
                formatter.format(calendar.get(Calendar.SECOND)) + amPm;

            _writer.print(time + " >> " + msg + "\n");
        }
	}


	public boolean isEnabled() {
		return _enabled;
	}


	public void setEnabled(boolean e) {
		_enabled = e;
	}


	public static void main(String[] args) {
		Log log1 = null;
		Log log2 = null;
		try {
			// não persistente
			log1 = new Log("log1", "arq1.log", false);

			// persistente
			log2 = new Log("log2", "arq2.log", true);

			String msg = "mensagem 1.\nLauro Didier Lins";
			log1.write(msg);
			log2.write(msg);

			msg = "mensagem 2.";
			log1.writeWithTimestamp(msg);
			log2.writeWithTimestamp(msg);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
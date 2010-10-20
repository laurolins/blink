package blink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ConvertToEPS {
    public ConvertToEPS() {
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        File dir = new File("c:/workspace/blink/phd/fig/");
        File dirOuput = new File("c:/workspace/blink/phd/fig/");

        for (File f: dir.listFiles()) {
            String name = f.getName().toUpperCase();
            if (!name.endsWith(".JPG") && !name.endsWith(".PDF")) {
                continue;
            }

            name = f.getName();
            name = name.substring(0,name.length()-4);
            String systemCall = "C:\\Program Files\\ImageMagick\\convert "+f.getAbsolutePath()+" "+dirOuput.getAbsolutePath()+"\\"+name+".eps";
            System.out.println(""+systemCall);

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(systemCall);

            // any error message?
            StreamGobbler2 errorGobbler = new StreamGobbler2(proc.getErrorStream(), "ERROR");

            // any output?
            StreamGobbler2 outputGobbler = new StreamGobbler2(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);

        }
    }
}


class StreamGobbler2 extends Thread {
    InputStream is;
    String type;

    StreamGobbler2(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}


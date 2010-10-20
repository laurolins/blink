package linsoft.gui;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JTextField;

public class Input extends JTextField {
    public final static int TF_INTEIRO = 0;
    public final static int TF_FLOAT = 1;
    public final static int TF_DIRECTORY = 2;
    public final static int TF_INTERVALOS = 3;
    public final static int TF_LISTA_INTEIROS = 4;
    public final static int TF_FILE = 5;
    public final static int TF_TEXT = 6;

    private IHaveProperties _repository;
    private int _type;
    private String _propertyName;
    private String _defaultValue;
    public Input(IHaveProperties repository, String propertyName, String defaultValue, int type, int width) {
        _repository = repository;
        _propertyName = propertyName;
        _defaultValue = defaultValue;
        _type = type;
        this.setPreferredSize(new Dimension(width,(int)this.getPreferredSize().getHeight()));
        String value = _repository.getProperty(_propertyName);
        if (value == null || "".equals(value)) {
            _repository.setProperty(_propertyName, _defaultValue);
            value = _defaultValue;
        }
        this.setText(value);
        this.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                if (!validateInput()) {
                    setText(_repository.getProperty(_propertyName));
                }
                else {
                    _repository.setProperty(_propertyName,getText());

                    // fire change
                    for (InputListener il : _listeners) {
                        il.inputValueChanged((Input) e.getSource());
                    }
                }
            }
            public void focusGained(FocusEvent e) {
            }
        });
    }

    public void setTextAndSave(String s) {
        this.setText(s);
        _repository.setProperty(_propertyName,getText());
    }

    public boolean validateInput() {
        if (_type == TF_INTEIRO) {
            try {
                Integer.parseInt(this.getText());
            }
            catch (Exception e) {
                return false;
            }
        }
        else if (_type == TF_FLOAT) {
            try {
                Float.parseFloat(this.getText());
            }
            catch (Exception e) {
                return false;
            }
        }
        else if (_type == TF_DIRECTORY) {
            File f = new File(this.getText());
            if (!f.isDirectory() || !f.exists())
                return false;
        }
        else if (_type == TF_FILE) {
            if (!"".equals(this.getText())) {
                File f = new File(this.getText());
                if (!f.exists() || f.isDirectory())
                    return false;
            }
        }
        else if (_type == TF_INTERVALOS) {
            StringTokenizer st = new StringTokenizer(this.getText(), ",; ");
            while (st.hasMoreTokens()) {
                String s2 = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(s2, "-");
                if (st2.countTokens() == 1) {
                    try {
                        Integer.parseInt(st2.nextToken());
                    }
                    catch (Exception e) {
                        return false;
                    }
                }
                else if (st2.countTokens() == 2) {
                    try {
                        Integer.parseInt(st2.nextToken());
                        Integer.parseInt(st2.nextToken());
                    }
                    catch (Exception e) {
                        return false;
                    }
                }
                else return false;
            }
        }
        else if (_type == TF_LISTA_INTEIROS) {
            StringTokenizer st = new StringTokenizer(this.getText(), ",; ");
            while (st.hasMoreTokens()) {
                try {
                    Integer.parseInt(st.nextToken());
                }
                catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getInt() {
        return Integer.parseInt(this.getText());
    }

    public float getFloat() {
        return Float.parseFloat(this.getText());
    }

    public int[] getListaInteiros() {
        if (_type != Input.TF_LISTA_INTEIROS)
            throw new RuntimeException("Ooops");

        ArrayList<Integer> intervalos = new ArrayList<Integer>();

        StringTokenizer st = new StringTokenizer(this.getText(),",; ");
        while (st.hasMoreTokens()) {
            int i = Integer.parseInt(st.nextToken());
            intervalos.add(i);
        }

        int result[] = new int[intervalos.size()];
        for (int i=0;i<result.length;i++)
            result[i] = intervalos.get(i);

        return result;
    }

    public int[] getIntervalos() {
        if (_type != TF_INTERVALOS)
            throw new RuntimeException("Ooops");

        ArrayList<Integer> intervalos = new ArrayList<Integer>();

        StringTokenizer st = new StringTokenizer(this.getText(),",; ");
        while (st.hasMoreTokens()) {
            String s2 = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(s2,"-");
            if (st2.countTokens() == 1) {
                int i = Integer.parseInt(st2.nextToken());
                intervalos.add(i);
                intervalos.add(i);
            }
            else if (st2.countTokens() == 2) {
                int i1 = Integer.parseInt(st2.nextToken());
                int i2 = Integer.parseInt(st2.nextToken());
                intervalos.add(i1 < i2 ? i1 : i2);
                intervalos.add(i1 < i2 ? i2 : i1);
            }
        }

        int result[] = new int[intervalos.size()];
        for (int i=0;i<result.length;i++)
            result[i] = intervalos.get(i);

        return result;
    }

    public File getFile() {
        if (_type != TF_FILE)
            throw new RuntimeException("Ooops");
        File f = new File(""+this.getText());
        //if (!f.exists()) return null;
        //else return f;
        return f;
    }

    // listeners
    private ArrayList<InputListener> _listeners = new ArrayList<InputListener>();
    public void addListener(InputListener il) {
        _listeners.add(il);
    }
    public void removeListener(InputListener il) {
        _listeners.remove(il);
    }
    public interface InputListener {
        public void inputValueChanged(Input i);
    }
}

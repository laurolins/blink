package linsoft.gui;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class InputComboBox extends JComboBox {

    private IHaveProperties _repository;
    private int _type;
    private String _propertyName;
    private int _defaultValue;

    private String[] _values;
    private DefaultComboBoxModel _model;

    public InputComboBox(IHaveProperties repository, String propertyName, String values[], int defaultValue, int width) {
        _repository = repository;
        _propertyName = propertyName;
        _defaultValue = defaultValue;
        _values = values.clone();

        _model = new DefaultComboBoxModel((Object[])values);
        this.setModel(_model);

        this.setPreferredSize(new Dimension(width,(int)this.getPreferredSize().getHeight()));
        String value = blink.App.getProperty(_propertyName);
        if (value == null || "".equals(value)) {
            _repository.setProperty(_propertyName, ""+_defaultValue);
            value = ""+_defaultValue;
        }
        this.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                _repository.setProperty(_propertyName,""+getSelectedIndex());
                // fire change
                for (InputComboBoxListener il : _listeners) {
                    il.InputComboBoxValueChanged((InputComboBox) e.getSource());
                }
            }
            public void focusGained(FocusEvent e) {
            }
        });
    }

    public String getSelectedValue() {
        return _values[getSelectedIndex()];
    }

    // listeners
    private ArrayList<InputComboBoxListener> _listeners = new ArrayList<InputComboBoxListener>();
    public void addListener(InputComboBoxListener il) {
        _listeners.add(il);
    }
    public void removeListener(InputComboBoxListener il) {
        _listeners.remove(il);
    }
    public interface InputComboBoxListener {
        public void InputComboBoxValueChanged(InputComboBox i);
    }
}

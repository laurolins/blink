package linsoft.gui;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class InputBoolean extends JCheckBox {
    private IHaveProperties _repository;
    private String _propertyName;
    private boolean _defaultValue;
    public InputBoolean(IHaveProperties repository, String propertyName, boolean defaultValue, String text) {
        super(text);
        _repository = repository;
        _propertyName = propertyName;
        _defaultValue = defaultValue;
        String stValue = blink.App.getProperty(_propertyName);
        boolean value = false;
        if (stValue == null || "".equals(stValue)) {
            _repository.setProperty(_propertyName, (_defaultValue ? "1" : "0"));
            value = _defaultValue;
        }
        else value = "1".equals(stValue);
        this.setSelected(value);
        this.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _repository.setProperty(_propertyName, (isSelected() ? "1" : "0"));
            }
        });
    }
}

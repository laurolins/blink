package blink.cli;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

import blink.App;

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
public class Console extends JFrame implements TextOutput {

    CommandLineTextArea _inputArea = new CommandLineTextArea();

    public Console() {

        super("Blink - Command Line Interface - "+App.getProperty(App.DB_NAME_PROPERTY));

        // this.getContentsetLayout(new BorderLayout());

        _inputArea = new CommandLineTextArea();
        this.setContentPane(new JScrollPane(_inputArea));

    }

    public static void main(String[] args) {
        Console c = new Console();
        linsoft.gui.util.Library.resizeAndCenterWindow(c,800,600);
        c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.setVisible(true);
    }


    public void write(String st) {
        _inputArea.write(st);
    }

    public void clear() {
        _inputArea.clear();
    }

    public void prompt() {

    }

}


class CommandLineTextArea extends JTextArea {
    private ArrayList<String> _commandsSent = new ArrayList<String>();

    private int _index = 0;

    private final String PROMPT = "> ";

    private int _minValidPosition=0;
    public CommandLineTextArea() {
        super();
        // System.out.println("Font name = "+this.getFont().getFontName());
        this.setFont(new Font(Font.MONOSPACED,Font.PLAIN,18));
        this.setBackground(Color.BLACK);
        this.setForeground(Color.YELLOW);
        this.setCaretColor(Color.YELLOW);
        this.setSelectionColor(Color.YELLOW);
        this.setSelectedTextColor(Color.black);
        this.getCaret().setSelectionVisible(true);
        this.getCaret().setVisible(true);

        this.setColumns(80);
        this.setLineWrap(false);

        NavigationFilter filter = new NavigationFilter() {
            public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
                //if (dot < _minValidPosition) {
                //    fb.setDot(_minValidPosition, bias);
                //} else {
                    fb.setDot(dot, bias);
                //}
            }

            public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
                //if (dot < _minValidPosition) {
                //    fb.moveDot(_minValidPosition, bias);
                //} else {
                    fb.moveDot(dot, bias);
                //}
            }
        };


        final Document document = this.getDocument();
        DocumentFilter docFilter = new DocumentFilter() {
            public void insertString(DocumentFilter.FilterBypass fb, int offset,
                                     String text, AttributeSet attr) throws BadLocationException {
                if (offset < _minValidPosition)
                    throw new BadLocationException("",offset);
                fb.insertString(offset, text, attr);
            }

            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (offset < _minValidPosition)
                    throw new BadLocationException("", offset);
                fb.remove(offset, length);
            }

            public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
                                String text, AttributeSet attr) throws BadLocationException {
                if (offset < _minValidPosition)
                    throw new BadLocationException("",offset);
                fb.replace(offset, length, text, attr);
            }

        };
        ((AbstractDocument) document).setDocumentFilter(docFilter);

        InputMap inputMap = this.getInputMap();
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        inputMap.put(key,new ActionExecuteCommand());
        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        inputMap.put(upKey,new ActionHistoryCommand(CommandType.upHistory));
        KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_MASK);
        inputMap.put(downKey,new ActionHistoryCommand(CommandType.downHistory));
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        inputMap.put(escapeKey,new ActionHistoryCommand(CommandType.erase));

        this.write(PROMPT);
        this.setCaretPosition(_minValidPosition);
        this.setNavigationFilter(filter);
        // this.setCa
        // this.setK
    }

    /**
     * Random GBlink
     */
    class ActionExecuteCommand extends AbstractAction {
        public ActionExecuteCommand() {
            super("exec");
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void hardwork() {
            String text = null;
            try {
                text = getText(_minValidPosition, getDocument().getLength() - _minValidPosition + 1);
                String consoleText = text.replaceAll("\n","");
                if (consoleText.length() > 0)
                    _commandsSent.add(consoleText);
                _index = _commandsSent.size();
                CommandLineInterface.getInstance().execute(text);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
                return;
            }
            prompt();
        }
    }

    /**
     * Random GBlink
     */
    enum CommandType {upHistory, downHistory, erase};
    class ActionHistoryCommand extends AbstractAction {
        private CommandType _commandType;
        public ActionHistoryCommand(CommandType commandType) {
            super("history");
            _commandType = commandType;
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void hardwork() {
            if (_commandType == CommandType.erase) {
                replaceRange("",_minValidPosition,getDocument().getLength());
            }
            else {
                if (_commandsSent.size() == 0)
                    return;
                if (_commandType == CommandType.upHistory) {
                    _index--;
                    _index = (_index + _commandsSent.size()) % _commandsSent.size();
                    String st = _commandsSent.get(_index);
                    replaceRange(st,_minValidPosition,getDocument().getLength());
                }
                else if (_commandType == CommandType.downHistory) {
                    _index++;
                    _index = (_index + _commandsSent.size()) % _commandsSent.size();
                    String st = _commandsSent.get(_index);
                    replaceRange(st,_minValidPosition,getDocument().getLength());
                }
            }
        }
    }

    public void clear() {
        // NavigationFilthis.setNavigationFilter(filter);
        // this.setCaretPosition(_minValidPosition);
    }

    public void write(String st) {
        if (st == null) st="";
        int nextMinValidPosition = Math.max(this.getCaretPosition() + st.length(),_minValidPosition + st.length());
        this.append(st);
        _minValidPosition = nextMinValidPosition;
        // System.out.println("min valid position: "+_minValidPosition);
    }

    public void prompt() {
        this.write("\n"+PROMPT);
    }

}

package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

//import com.l2fprod.gui.*;
//import com.l2fprod.gui.plaf.skin.*;
//import com.l2fprod.util.*;

public class Test extends JFrame {
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JToolBar _toolBar = new JToolBar();
    JCheckBox _cbShowNumbers = new JCheckBox();
    JSplitPane _splitPane = new JSplitPane();
    JScrollPane _scrollPaneEditor = new JScrollPane();
    BeanTable _editTable = new BeanTable();
    JScrollPane _scrollPaneSelecao = new JScrollPane();
    BeanTable _selectTable = new BeanTable();

    private BeanTableModel2 _modelEditTable = new BeanTableModel2(TestObject.class, null);
    private MultipleBeanTableModel _modelSelectTable = new
        MultipleBeanTableModel(TestObject.class);

    public Test() throws Exception {
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //
        _modelSelectTable.setNumberColumnFgColor(Color.BLUE);
        _modelSelectTable.setNumberColumnBgColor(Color.YELLOW);
        _modelSelectTable.setNumberColumnWidth(50);
        _modelSelectTable.setNumberColumnFont(new Font("Tahoma",Font.BOLD,11));

        //
        Vector objects = new Vector();
        for (int i = 0; i < 100000; i++) {
            objects.add(new TestObject());
        }
        _modelSelectTable.addObject(objects);

        //
        _editTable.setModel(_modelEditTable);
        _selectTable.setModel(_modelSelectTable);
    }

    public static void main(String argv[]) throws Exception {
        //UIManager.setLookAndFeel(new com.incors.plaf.kunststoff.KunststoffLookAndFeel());

        UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.
                                 WindowsLookAndFeel());

        //
        /*
             String theme = "c:/linguagens/java/lib/skinlf-1.2.3/lib/themepack.zip";
                 SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(theme));
                 SkinLookAndFeel.enable();
         */

        Test t = new Test();
        t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        t.setBounds(100, 100, 750, 500);
        t.setVisible(true);
    }

    private void jbInit() throws Exception {
        jPanel1.setLayout(borderLayout1);
        _toolBar.setFloatable(false);
        _cbShowNumbers.setSelected(true);
        _cbShowNumbers.setText("Coluna com Número");
        _cbShowNumbers.addActionListener(new Test__cbShowNumbers_actionAdapter(this));
        _splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(_toolBar, BorderLayout.NORTH);
        jPanel1.add(_splitPane, BorderLayout.CENTER);
        _splitPane.add(_scrollPaneEditor, JSplitPane.TOP);
        _splitPane.setDividerLocation(250);
        _scrollPaneEditor.setViewportView(_editTable);
        _splitPane.add(_scrollPaneSelecao, JSplitPane.BOTTOM);
        _scrollPaneSelecao.setViewportView(_selectTable);
        _toolBar.add(_cbShowNumbers, null);

        //
        installListeners();
    }

    private void installListeners() {
        //JTable table = new JTable();
        _selectTable.getSelectionModel().addListSelectionListener(new
            ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                _modelEditTable.clear();
                int rows[] = _selectTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    _modelEditTable.addObject(_modelSelectTable.getObject(rows[
                                                                          i]));
                }
            }
        });

        //
        _modelEditTable.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                _modelSelectTable.updateAllCells();
            }
        });

    }

    void _cbShowNumbers_actionPerformed(ActionEvent e) {
        // _modelSelectTable.setShowNumberColumn(_cbShowNumbers.isSelected());
    }
}

class Test__cbShowNumbers_actionAdapter
    implements java.awt.event.ActionListener {
    Test adaptee;

    Test__cbShowNumbers_actionAdapter(Test adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee._cbShowNumbers_actionPerformed(e);
    }
}

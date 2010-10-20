package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

public class BeanEditor extends JPanel {

   // gui related objects
   BorderLayout borderLayout1 = new BorderLayout();
   BeanPanel panel = new BeanPanel();
   JSplitPane jSplitPane1 = new JSplitPane();
   ObjectListModel listModel = new ObjectListModel();
   JScrollPane jScrollPane1 = new JScrollPane();
   JList list = new JList();

   /**
    * Constructor
    */
   public BeanEditor() {
      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Set Table cell editor by class;
    */
   public void setEditor(Class c, TableCellEditor edt) {
      panel.setEditor(c,edt);
   }

   /**
    * Init gui related objects.
    * @throws Exception
    */
   private void changeItem() {
      int index = list.getSelectedIndex();
      if (index >= 0) {
         Object obj = listModel.getElementAt(index);
	 try {
	    panel.setObject(obj);
	 }
	 catch (Exception e) {
	    e.printStackTrace();
	 }
      }
   }

   /**
    * Init gui related objects.
    * @throws Exception
    */
   private void jbInit() throws Exception {
      /*
      list.addKeyListener(new KeyAdapter() {
	 public void keyPressed(KeyEvent e) {
	    changeItem();
	 }
      });
      list.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e)	{
	    changeItem();
	 }
      });
      */
      jSplitPane1.setPreferredSize(new Dimension(500, 200));
      jSplitPane1.setDividerSize(3);
      jSplitPane1.setLastDividerLocation(200);
      jSplitPane1.setDividerLocation(200);
      list.addListSelectionListener(new ListSelectionListener() {
	 public void valueChanged(ListSelectionEvent e) {
	    changeItem();
	 }
      });
      list.setModel(listModel);
      jSplitPane1.add(panel, JSplitPane.RIGHT);
      jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);
      jScrollPane1.getViewport().add(list, null);
      this.setLayout(borderLayout1);
      this.add(jSplitPane1, BorderLayout.CENTER);
   }


   /**
    * Add Object to Bean Editor
    * @param object
    */
   public void addObject(Object object) {
      listModel.addObject(object);
   }


   class ObjectListModel extends javax.swing.AbstractListModel {
      private Vector _objects;
      public ObjectListModel() {
	 super();
	 _objects = new Vector();
      }
      public void addObject(Object obj) {
	 _objects.add(obj);
	 this.fireIntervalAdded(this,_objects.size()-1,_objects.size()-1);
      }
      public int getSize() {
	 return _objects.size();
      }
      public Object getObject(int index) {
	 return _objects.get(index);
      }
      public Object getElementAt(int index) {
	 return this.getObject(index);
      }
   }
}

package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.TableCellEditor;

public class BeanTableEditor extends JPanel {

   // gui related objects
   BorderLayout borderLayout1 = new BorderLayout();
   BeanPanel panel = new BeanPanel();
   JSplitPane jSplitPane1 = new JSplitPane();
   JScrollPane jScrollPane1 = new JScrollPane();
   JList list = new JList();
   BeanTable table = new BeanTable();
   MultipleBeanTableModel model;

   // class that whose objects go on the table
   Class _beanClass = null;

   /**
    * Constructor
    */
   public BeanTableEditor(Class c) {
	  _beanClass = c;
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
	  table.setDefaultEditor(c,edt);
   }

   /**
    * Init gui related objects.
    * @throws Exception
    */
   private void changeItem() {
	  int index = table.getSelectedRow();
	  if (index >= 0) {
		 Object obj = model.getObject(index);
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
	  model = new MultipleBeanTableModel(_beanClass);

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


	  // add listeners
      table.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
		   changeItem();
		}
      });
      table.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e)	{
			changeItem();
		 }
      });


	  /*
      list.addListSelectionListener(new ListSelectionListener() {
		 public void valueChanged(ListSelectionEvent e) {
		   changeItem();
		 }
      });
      list.setModel(listModel);
	  */
	  table.setModel(model);
      jScrollPane1.getViewport().add(table, null);

      jSplitPane1.add(panel, JSplitPane.RIGHT);
      jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);

      this.setLayout(borderLayout1);
      this.add(jSplitPane1, BorderLayout.CENTER);
   }


   /**
    * Add Object to Bean Editor
    * @param object
    */
   public void addObject(Object object) {
	  model.addObject(object);
   }
}

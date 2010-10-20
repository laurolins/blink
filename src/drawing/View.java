package drawing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * The view window is a rectangle which is a drawing. It can be positioned
 * in the world like any other object. The rectangle is mapped into a new
 * rectangle positioned in another world: the screen world.
 *  
 * @author lauro
 */
public class View  {

	private Rectangle _worldRectangle;

	private Rectangle _screenRectangle;

	public View(Rectangle worldRectangle, Rectangle screenRectangle) {
		super();
		_worldRectangle = worldRectangle;
		_screenRectangle = screenRectangle;
	}

	
	public Object[] getRootTransform() {
		IDrawing sceneRoot = null;
		
		// get transform that takes points in the scene root
		// coordinate system and takes it to screen coordinate
		// system.
		
		IDrawing d= _worldRectangle;
		AffineTransform toWorldRectangle = new AffineTransform(d.getTransformToObject());
		while (d.getParent() != null) {
			d = d.getParent();
			toWorldRectangle.concatenate(d.getTransformToObject());
		}
		sceneRoot = (IDrawing) d;
		
		// mount to Screen
		d = _screenRectangle;
		AffineTransform toScreenRectangle = new AffineTransform(d.getTransformToParent());
		while (d.getParent() != null) {
			d = d.getParent();			
			AffineTransform newT = new AffineTransform(d.getTransformToParent());			
			newT.concatenate(toScreenRectangle);
			toScreenRectangle = newT;
		}

		AffineTransform T = new AffineTransform(toScreenRectangle);
		// System.out.println("toScreenRectangle "+T.toString());
		// System.out.println("toWorldRectangle "+toWorldRectangle.toString());
		T.concatenate(toWorldRectangle);
		
		return new Object[] {sceneRoot,T,toScreenRectangle,toWorldRectangle};
	}
	
	public void paint(Graphics G) {

		Object obj[] = this.getRootTransform();
		IDrawing scene = (IDrawing) obj[0];
		AffineTransform T = (AffineTransform) obj[1];
		AffineTransform toScreenRectangle = (AffineTransform) obj[2];
		AffineTransform toWorldRectangle = (AffineTransform) obj[3];
		
//		// mount AffineTransform: toViewRectangle
//		IDrawing d, scene;
//		
//		d= _worldRectangle;
//		AffineTransform toWorldRectangle = new AffineTransform(d.getTransformToObject());
//		while (d.getParent() != null) {
//			d = d.getParent();
//			toWorldRectangle.concatenate(d.getTransformToObject());
//		}
//		scene = d;
//		
//		// mount to Screen
//		d = _screenRectangle;
//		AffineTransform toScreenRectangle = new AffineTransform(d.getTransformToParent());
//		while (d.getParent() != null) {
//			d = d.getParent();			
//			AffineTransform newT = new AffineTransform(d.getTransformToParent());			
//			newT.concatenate(toScreenRectangle);
//			toScreenRectangle = newT;
//		}
//		
//		
//		AffineTransform T = new AffineTransform(toScreenRectangle);
//		System.out.println("toScreenRectangle "+T.toString());
//		System.out.println("toWorldRectangle "+toWorldRectangle.toString());
//		T.concatenate(toWorldRectangle);

		// mount clipping shape
		double points[] = {0,0,1,0,1,1,0,1};
		toScreenRectangle.transform(points,0,points,0,4);
		GeneralPath.Double P = new GeneralPath.Double();
		P.moveTo(points[0], points[1]);
		P.lineTo(points[2], points[3]);
		P.lineTo(points[4], points[5]);
		P.lineTo(points[6], points[7]);
		P.closePath();
		((Graphics2D) G).setClip(P);
		
		// draw boundary
		//G.setColor(new Color(255,255,255));
		//((Graphics2D) G).fill(P);

		// draw scene
		scene.draw((Graphics2D)G, T);
		
		// draw boundary
		G.setClip(null);
		// G.setColor(Color.black);
		// ((Graphics2D) G).draw(P);
		
	}

	public IDrawing getTopMostDrawingThatContainsPoint(double x, double y) {

		Object obj[] = this.getRootTransform();
		IDrawing scene = (IDrawing) obj[0];
		AffineTransform T = (AffineTransform) obj[1];

		try {
			T = T.createInverse();
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("OOOppsss");
		}
		
		// transform screen to scene coordinates
		double p[] = {x,y};
		T.transform(p,0,p,0,1);
		// System.out.println(String.format("Screen point (%.2f, %.2f) is World Point (%.2f, %.2f) ",x,y,p[0],p[1]));

		// transform screen to scene coordinates
		if (scene instanceof IGroup) {
			return ((IGroup) scene).getTopMostDrawingThatContainsPoint(p[0], p[1]);
		}
		else {
			return scene.containsPoint(x, y) ? scene : null;
		}
	}
	
	public double[] getWorldCoordinateForScreenCoordinate(double x, double y) {

		Object obj[] = this.getRootTransform();
		IDrawing scene = (IDrawing) obj[0];
		AffineTransform T = (AffineTransform) obj[1];

		try {
			T = T.createInverse();
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("OOOppsss");
		}
		double p[] = {x,y};
		T.transform(p,0,p,0,1);
		// System.out.println(String.format("Screen point (%.2f, %.2f) is World Point (%.2f, %.2f) ",x,y,p[0],p[1]));
		return p;
	}

	public double[] getScreenCoordinatesForWorldCoordinate(double x, double y) {
		Object obj[] = this.getRootTransform();
		AffineTransform T = (AffineTransform) obj[1];
		double p[] = {x,y};
		T.transform(p,0,p,0,1);
		// System.out.println(String.format("Screen point (%.2f, %.2f) is World Point (%.2f, %.2f) ",x,y,p[0],p[1]));
		return p;
	}
	
	
	
	public static void main(String[] args) {
		
		Rectangle rA = new Rectangle(null,0,0,0,0.2,0.2);
		Rectangle rB = new Rectangle(null,0.8,0.8,0,0.2,0.2);
		Rectangle rC = new Rectangle(null,0.4,0.4,0,0.2,0.2);
		
		Text text = new Text(null,"(WTOF)",0.4,0.4,90.0/180.0*Math.PI,0.2);
		
		Rectangle rWorld = new Rectangle(null,0.1,0.1,0,0.8,0.8);
		rWorld.setVisible(false);

		SimpleGroup world = new SimpleGroup(null);
		world.addChild(text);
		world.addChild(rA);
		world.addChild(rB);
		world.addChild(rC);
		world.addChild(rWorld);
		
		Rectangle rScreen1 = new Rectangle(null,150,80,45/180.0*Math.PI,100,200);
		Rectangle rScreen2 = new Rectangle(null,300,20,0,100,75);
		Rectangle rScreen3 = new Rectangle(null,20,300,45/180.0*Math.PI,170,90);
		Rectangle rScreen4 = new Rectangle(null,290,270,-60/180.0*Math.PI,50,50);
		Rectangle rScreen5 = new Rectangle(null,1,1,0,460,460);
		
		ArrayList<View> views = new ArrayList<View>();
		views.add(new View(rWorld,rScreen5));
		views.add(new View(rWorld,rScreen1));
		views.add(new View(rWorld,rScreen2));
		views.add(new View(rWorld,rScreen3));
		views.add(new View(rWorld,rScreen4));
		
		JFrame f = new JFrame("Séries");
		f.setBounds(0,0,500, 500);
		f.setContentPane(new TestFrame(rWorld,views));
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	static class TestFrame extends JPanel {
		private Rectangle _worldRectangle;
		private Rectangle _screenRectangle;
		
		ArrayList<View> _views = new ArrayList<View>();
		
		public TestFrame(Rectangle worldRectangle, List<View> views) {
			super();
			_views.addAll(views);
			
			_worldRectangle = worldRectangle;

			// map the keys of the keyboard 
	        ActionMap amap = this.getActionMap();
	        amap.put("zoom",  ACTION_ZOOM);
	        amap.put("unzoom", ACTION_UNZOOM);
	        amap.put("left", ACTION_LEFT);
	        amap.put("right", ACTION_RIGHT);
	        amap.put("up", ACTION_UP);
	        amap.put("down", ACTION_DOWN);

	        InputMap imap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0, false), "left");
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0, false), "right");
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, 0, false), "zoom");
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, 0, false), "unzoom");
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0, false), "up");
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0, false), "down");
	        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, 0, false), "setinterval");
		}

		private Action ACTION_ZOOM = new Action("Zoom",null,"Zoom") {
	        public void actionPerformed(ActionEvent actionEvent) {
				System.out.println("Zoom");
				double x0 = _worldRectangle.get_x0();
				double y0 = _worldRectangle.get_y0();
				double width = _worldRectangle.get_width();
				double height = _worldRectangle.get_height();
				double theta = _worldRectangle.get_theta();
				_worldRectangle.setTransformToParent(x0,y0,theta,width*0.952380952,height*0.952380952);
				repaint();
	        }
	    };
		
		private Action ACTION_UNZOOM = new Action("Unzoom",null,"Unzoom") {
	        public void actionPerformed(ActionEvent actionEvent) {
				double x0 = _worldRectangle.get_x0();
				double y0 = _worldRectangle.get_y0();
				double width = _worldRectangle.get_width();
				double height = _worldRectangle.get_height();
				double theta = _worldRectangle.get_theta();
				_worldRectangle.setTransformToParent(x0,y0,theta,width*1.05,height*1.05);
				repaint();
	        }
	    };
		
		private Action ACTION_LEFT = new Action("Left",null,"left") {
	        public void actionPerformed(ActionEvent actionEvent) {
				double x0 = _worldRectangle.get_x0();
				double y0 = _worldRectangle.get_y0();
				double width = _worldRectangle.get_width();
				double height = _worldRectangle.get_height();
				double theta = _worldRectangle.get_theta();
				_worldRectangle.setTransformToParent(x0-width*0.05,y0,theta,width,height);
				repaint();
	        }
	    };

		private Action ACTION_RIGHT = new Action("Right",null,"right") {
	        public void actionPerformed(ActionEvent actionEvent) {
				double x0 = _worldRectangle.get_x0();
				double y0 = _worldRectangle.get_y0();
				double width = _worldRectangle.get_width();
				double height = _worldRectangle.get_height();
				double theta = _worldRectangle.get_theta();
				_worldRectangle.setTransformToParent(x0+width*0.05,y0,theta,width,height);
				repaint();
	        }
	    };
	    
		private Action ACTION_UP = new Action("Up",null,"up") {
	        public void actionPerformed(ActionEvent actionEvent) {
				double x0 = _worldRectangle.get_x0();
				double y0 = _worldRectangle.get_y0();
				double width = _worldRectangle.get_width();
				double height = _worldRectangle.get_height();
				double theta = _worldRectangle.get_theta();
				_worldRectangle.setTransformToParent(x0,y0-height*0.05,theta,width,height);
				repaint();
	        }
	    };

		private Action ACTION_DOWN = new Action("Down",null,"down") {
	        public void actionPerformed(ActionEvent actionEvent) {
				double x0 = _worldRectangle.get_x0();
				double y0 = _worldRectangle.get_y0();
				double width = _worldRectangle.get_width();
				double height = _worldRectangle.get_height();
				double theta = _worldRectangle.get_theta();
				_worldRectangle.setTransformToParent(x0,y0+height*0.05,theta,width,height);
				repaint();
	        }
	    };
	    
	    
		public void paint(Graphics G) {
			super.paint(G);
	        ((Graphics2D) G).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        ((Graphics2D) G).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	        
	        for (View v: _views)
	        	v.paint(G);
			
			/*

			// mount AffineTransform: toViewRectangle
			IDrawing d, scene;
			
			d= _worldRectangle;
			AffineTransform toWorldRectangle = new AffineTransform(d.getTransformToObject());
			while (d.getParent() != null) {
				d = d.getParent();
				toWorldRectangle.concatenate(d.getTransformToObject());
			}
			scene = d;
			
			// mount to Screen
			d = _screenRectangle;
			AffineTransform toScreenRectangle = new AffineTransform(d.getTransformToParent());
			while (d.getParent() != null) {
				d = d.getParent();			
				AffineTransform newT = new AffineTransform(d.getTransformToParent());			
				newT.concatenate(toScreenRectangle);
				toScreenRectangle = newT;
			}
			
			
			AffineTransform T = new AffineTransform(toScreenRectangle);
			System.out.println("toScreenRectangle "+T.toString());
			System.out.println("toWorldRectangle "+toWorldRectangle.toString());
			T.concatenate(toWorldRectangle);

			// mount clipping shape
			double points[] = {0,0,1,0,1,1,0,1};
			toScreenRectangle.transform(points,0,points,0,4);
			GeneralPath.Double P = new GeneralPath.Double();
			P.moveTo(points[0], points[1]);
			P.lineTo(points[2], points[3]);
			P.lineTo(points[4], points[5]);
			P.lineTo(points[6], points[7]);
			P.closePath();
			((Graphics2D) G).setClip(P);
			
			// draw scene
			scene.draw((Graphics2D)G, T);
			
			// draw boundary
			G.setClip(null);
			G.setColor(Color.black);
			((Graphics2D) G).draw(P);
			*/
		}
		
	}

	private static abstract class Action extends AbstractAction {
		public Action(String name, Icon icon, String description) {
			super(name, icon);
			this.putValue(Action.SHORT_DESCRIPTION, description);
			this.putValue(Action.LONG_DESCRIPTION, description);
		}
	}
}



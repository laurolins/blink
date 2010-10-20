package drawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class Text extends AbstractDrawing {

	public Text(IGroup parent, String text, double x0, double y0, double theta, double fontSize) {
		super(parent,x0,y0,theta,1,1);
		_text = text;
		_x0 = x0;
		_y0 = y0;
		_theta = theta;
		_fontSize = fontSize;
	}

	private String _text;
	
	/**
	 * The spec of the rectangle in terms of it's parent 
	 * cordinate system.
	 */
	private double _x0, _y0, _theta, _fontSize;
	
	private Font _font = new Font(Font.SANS_SERIF,Font.PLAIN,1);
	
	public void setFont(Font f) {
		_font = f;
	}

	public boolean containsPoint(double x, double y) {
		// TODO Auto-generated method stub
		return false;
	}

	public void draw(Graphics2D G, AffineTransform T) {
		// TODO Auto-generated method stub

		if (!this.isVisible())
			return;
			
		double[] points = {0,0,1,0,0,1};

		this.transformToParent(points);
		// System.out.println(String.format("Rect Before %.2f %.2f %.2f %.2f",points[0],points[1],points[2],points[3]));
		
		T.transform(points,0,points,0,3);
		// System.out.println(String.format("Rect After %.2f %.2f %.2f %.2f",points[0],points[1],points[2],points[3]));
		
		double ux = points[2] - points[0];
		double uy = points[3] - points[1];
		double xUnitLength = Math.sqrt(ux*ux+uy*uy);
		double theta = Math.acos(ux/xUnitLength);
				
		double vx = points[4] - points[0];
		double vy = points[5] - points[1];
		double yUnitLength = Math.sqrt(vx*vx+vy*vy);

		// System.out.println(String.format("(xUnitLength, yUnitLength) = (%.5f,%.5f)    theta = %.5f deg",xUnitLength,yUnitLength,180.0 * theta/Math.PI));
		
		G.setFont(_font);
		
		AffineTransform bkupT = G.getTransform();
		G.translate(points[0], points[1]);
		G.rotate(theta);
		
//		AffineTransform bkupT2 = G.getTransform();
//		G.scale(_fontSize*xUnitLength, _fontSize*yUnitLength);
//		{
//			// define the shape  
//			GeneralPath.Double P = new GeneralPath.Double();
//			P.moveTo(0, 0);
//			P.lineTo(1, 0);
//			P.lineTo(1, -1);
//			P.lineTo(0, -1);
//			P.closePath();
//			
//			// draw the shape
//			G.setColor(new Color(0,255,0));
//			//G.fill(P);
//			//G.setColor(Color.black);
//			//G.draw(P);
//		}
//
//		G.setTransform(bkupT2);
	    int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();

	    // System.out.println(String.format("Screen Resolution (dpi) = %d",screenRes));

	    // G.setFont(new Font(Font.DIALOG,Font.PLAIN,(float)_fontSize*yUnitLength));
	    
	    // double fontSizeToMatchScreen = _fontSize*(double)screenRes / 72.0;
		// G.scale(fontSizeToMatchScreen*xUnitLength, fontSizeToMatchScreen*yUnitLength);
	    
	    G.scale(_fontSize*xUnitLength, _fontSize*yUnitLength);
		
		// TODO como fazer pra esse próximo comando (new TextLayout) não detonar a memória
		TextLayout tLayout = new TextLayout(_text,G.getFont(),G.getFontRenderContext());
		float w = (float)tLayout.getVisibleAdvance();
		float h = (float)tLayout.getBounds().getHeight();
		
				
		// if centered text
		tLayout.draw(G,-w/2.0f, h/2.0f);
		
		G.setTransform(bkupT);
		
	};

	
	
	/*
	public boolean draw(Graphics2D G, String st, float x0, float y0, float w, float h) {
		
		float wUtil = (float) (w * (1.0f - _margins[Margin.left.ordinal()] - _margins[Margin.right.ordinal()]));
		float hUtil = (float) (h * (1.0f - _margins[Margin.top.ordinal()] - _margins[Margin.bottom.ordinal()]));
		
		G.setFont(_font);
		TextLayout tLayout = new TextLayout(st,G.getFont(),G.getFontRenderContext());
		float textWidth  = tLayout.getAdvance();
		float textAscent = tLayout.getAscent();
		float textHeight = textAscent;
		
		//System.out.println(String.format("String: %s  textWidth %.2f textHeight: %.2f    wUtil: %.2f hUtil: %.2f   WxH = %.2f X %.2f",
		//		st,textWidth,textHeight,wUtil,hUtil,w,h));
		
		// align in the left and middle
		if (wUtil < textWidth || hUtil < textHeight)
			return false;

		// @todo other alignment implementations
		 
		//Rectangle2D.Double r = new Rectangle2D.Double(x0,y0,w,h);
		//G.setColor(Color.yellow);
		//G.draw(r);
		
		float xt, yt;
		if (_hAlign == HorizontalAlignment.left) {
			xt = x0 + _margins[Margin.left.ordinal()]*w;
		}
		else if (_hAlign == HorizontalAlignment.center) {
			xt = x0 + _margins[Margin.left.ordinal()]*w + (wUtil - textWidth)/2.0f;
		}
		else if (_hAlign == HorizontalAlignment.right) {
			xt = x0 + _margins[Margin.left.ordinal()]*w + wUtil - textWidth;
		}
		else throw new RuntimeException("Not Implemented");

		if (_vAlign == VerticalAlignment.middle) {
			yt = y0 + _margins[Margin.bottom.ordinal()]*h + (hUtil - textHeight)/2.0f + textHeight;
		}
		else throw new RuntimeException("Not Implemented");

		G.setColor(Color.black);
		tLayout.draw(G, xt, yt);
		
		
		return true
} */


}

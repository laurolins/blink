package drawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;

public class TextDrawIfFits {
	public enum Margin {bottom, top, left, right}; 
	public enum HorizontalAlignment {left, center, right}; 
	public enum VerticalAlignment {top, middle, bottom}; 

	private HorizontalAlignment _hAlign = HorizontalAlignment.left;
	private VerticalAlignment _vAlign = VerticalAlignment.middle;
	private float _margins[] = { 0.01f, 0.01f, 0.01f, 0.01f };
	private Font _font = new Font(Font.SANS_SERIF,Font.PLAIN,10);
	
	public void setFont(Font f) {
		_font = f;
	}
	
	public void setMargin(Margin m, float v) {
		_margins[m.ordinal()] = v;
	}
	
	public void setHorizontalAlignment(HorizontalAlignment a) {
		_hAlign = a;
	}

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

		/**
		 * @todo other alignment implementations
		 */

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
		
		
		return true;
	}

	
}

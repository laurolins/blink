package util;

import java.awt.Color;

public class Library {
	
	public static Color getColorFromString(String st) {
		String tokens[] = st.replace(" ","").split(",");
		int red = Integer.parseInt(tokens[0]);
		int green = Integer.parseInt(tokens[1]);
		int blue = Integer.parseInt(tokens[2]);
		if (tokens.length > 3) {
			int alpha = Integer.parseInt(tokens[3]);
			return new Color(red,green,blue,alpha);
		}
		else {
			return new Color(red,green,blue);
		}
	}
	
	public static String encodeColorInString(Color c) {
		return String.format("%d,%d,%d,%d",c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	}

	public static String toString(int[] v) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i: v) {
			if (!first)
				sb.append(" ");
			sb.append(i);
			first = false;
		}
		return sb.toString();
	}

}

//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Vector;

public class cParagraph extends cPrintObject {

	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int BLOCK = 3;

	private String original;
	private Vector paragraphs;
	private int alignment;

	private Font font;

	public cParagraph() {
		super();
		paragraphs = new Vector();

		alignment = 0;

		font = new Font("Default", Font.PLAIN, 10);
	}
	
	public void setFontStyle( int style ) {
		font = font.deriveFont( style );
			
		updateFont();		
	}

	public void setFontSize( float size ) {	
		font = font.deriveFont(size);
		updateFont();		
	}	
	
	private void updateFont() {
		for( int i=0; i<paragraphs.size(); i++ ) {
			((AttributedString)paragraphs.get(i)).addAttribute(TextAttribute.FONT, font);
		}			
	}
	
	private String validate(String t) {
		if (t.indexOf("\n\n") == -1)
			return t;

		StringBuffer result = new StringBuffer();
		char last = ' ';

		for (int i = 0; i < t.length(); i++) {
			if ((t.charAt(i) == '\n') && (last == '\n')) {
				result.append(" \n");
			} else {
				result.append(t.charAt(i));
			}
			last = t.charAt(i);
		}

		return result.toString();
	}

	public void setText(String t) {
		original = validate(t);

		paragraphs.clear();
		StringBuffer buffer = new StringBuffer();

		AttributedString act;

		for (int i = 0; i < original.length(); i++) {
			if (original.charAt(i) == '\n') {
				act = new AttributedString(buffer.toString());
				act.addAttribute(TextAttribute.FONT, font);
				paragraphs.add(act);
				buffer = new StringBuffer();
			} else {
				buffer.append(original.charAt(i));
			}
		}

		if (buffer.length() != 0) {
			act = new AttributedString(buffer.toString());
			act.addAttribute(TextAttribute.FONT, font);
			paragraphs.add(act);
		}
	}

	public void setTextAlignment(int v) {
		alignment = v;
	}

	public void print(Graphics2D g) {
		computePositionAndSize();

		Color saveForeground = g.getColor();

		g.setColor(color);

		switch (alignment) {
			case LEFT :
				renderLeftAligned(g);
				break;

			case RIGHT :
				renderRightAligned(g);
				break;

			case CENTER :
				renderCenterAligned(g);
				break;

		}

		g.setColor(saveForeground);
	}

	private void renderLeftAligned(Graphics2D g) {

		Point2D.Double pen = getDrawingOrigin().getPoint2D();

		double width = getDrawingSize().getWidth().getPoints();

		for (int i = 0; i < paragraphs.size(); i++) {
			LineBreakMeasurer lineBreaker =
				new LineBreakMeasurer(
					((AttributedString) paragraphs.get(i)).getIterator(),
					new FontRenderContext(null, true, true));

			TextLayout layout;
			while ((layout = lineBreaker.nextLayout((float) width)) != null) {
				pen.y += layout.getAscent();

				layout.draw(g, (float) pen.x, (float) pen.y);

				pen.y += layout.getDescent() + layout.getLeading();
			}

		}

	}

	private void renderRightAligned(Graphics2D g) {

		Point2D.Double pen = getDrawingOrigin().getPoint2D();

		double width = getDrawingSize().getWidth().getPoints();

		for (int i = 0; i < paragraphs.size(); i++) {
			LineBreakMeasurer lineBreaker =
				new LineBreakMeasurer(
					((AttributedString) paragraphs.get(i)).getIterator(),
					new FontRenderContext(null, true, true));

			TextLayout layout;
			float layoutX;

			while ((layout = lineBreaker.nextLayout((float) width)) != null) {
				pen.y += layout.getAscent();

				layoutX = ((float) (pen.x + width)) - layout.getAdvance();

				layout.draw(g, layoutX, (float) pen.y);

				pen.y += layout.getDescent() + layout.getLeading();
			}

		}
	}

	private void renderCenterAligned(Graphics2D g) {

		Point2D.Double pen = getDrawingOrigin().getPoint2D();

		double width = getDrawingSize().getWidth().getPoints();

		for (int i = 0; i < paragraphs.size(); i++) {
			LineBreakMeasurer lineBreaker =
				new LineBreakMeasurer(
					((AttributedString) paragraphs.get(i)).getIterator(),
					new FontRenderContext(null, true, true));

			TextLayout layout;

			float layoutX;

			while ((layout = lineBreaker.nextLayout((float) width)) != null) {
				pen.y += layout.getAscent();

				layoutX = ((float) (pen.x + (width / 2))) - (layout.getAdvance() / 2);

				layout.draw(g, layoutX, (float) pen.y);

				pen.y += layout.getDescent() + layout.getLeading();
			}

		}
	}

	public cSize getSize(cUnit w) {
		cCmUnit textHeight = new cCmUnit();
		float maxAdvance = -1;

		Point2D.Double pen = new Point2D.Double(0.0, 0.0);

		double width = w.sub(leftMargin).sub(rightMargin).getPoints();

		for (int i = 0; i < paragraphs.size(); i++) {
			LineBreakMeasurer lineBreaker =
				new LineBreakMeasurer(
					((AttributedString) paragraphs.get(i)).getIterator(),
					new FontRenderContext(null, true, true));

			TextLayout layout;
			while ((layout = lineBreaker.nextLayout((float) width)) != null) {
				pen.y += layout.getAscent() + layout.getDescent() + layout.getLeading();
				if( layout.getAdvance() > maxAdvance )
					maxAdvance = layout.getAdvance();
			}

		}

		textHeight.setPoints(pen.y);
		textHeight.addI(topMargin);
		textHeight.addI(bottomMargin);

		cCmUnit bwidth = new cCmUnit();
		bwidth.setPoints( maxAdvance );
		bwidth.addI( leftMargin );
		bwidth.addI( rightMargin );

		return new cSize(bwidth, textHeight);
	}

	public cPrintObject breakBlock(cUnit w, cUnit maxHeight) {
		AttributedCharacterIterator it;
		int lastPos;
		int pos = 0;

		cParagraph remainParagraph = null;

		Point2D.Double pen = new Point2D.Double(0.0, topMargin.getPoints());

		double width = w.sub(leftMargin).sub(rightMargin).getPoints();

		for (int i = 0; i < paragraphs.size(); i++) {
			it = ((AttributedString) paragraphs.get(i)).getIterator();

			LineBreakMeasurer lineBreaker =
				new LineBreakMeasurer(it, new FontRenderContext(null, true, true));

			TextLayout layout;
			lastPos = 0;

			while ((layout = lineBreaker.nextLayout((float) width)) != null) {

				pen.y += layout.getAscent() + layout.getDescent() + layout.getLeading();

				if (pen.y > maxHeight.getPoints()) {

					remainParagraph = new cParagraph();
					remainParagraph.setTextAlignment(alignment);
					remainParagraph.setTopMargin(topMargin);
					remainParagraph.setLeftMargin(leftMargin);
					remainParagraph.setRightMargin(rightMargin);

					topMargin = new cCmUnit();

					remainParagraph.setText(original.substring(0, pos + lastPos));
					this.setText(original.substring(pos + lastPos));

					return remainParagraph;
				}
				lastPos += layout.getCharacterCount();
			}

			pos += it.getEndIndex() + 1;

		}

		return remainParagraph;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		updateFont();		
	}

}

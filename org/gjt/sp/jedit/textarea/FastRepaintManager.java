/*
 * FastRepaintManager.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2005 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.textarea;

//{{{ Imports
import javax.swing.text.*;
import javax.swing.JComponent;
import java.awt.font.*;
import java.awt.*;
import java.util.*;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * Manages blitting the offscreen graphics context to speed up scrolling.
 * The text area does not use Swing's built-in double buffering, so that
 * we have access to the graphics context for fast scrolling.
 * @author Slava Pestov
 * @version $Id: FastRepaintManager.java,v 1.1 2005/02/12 20:45:53 spestov Exp $
 */
class FastRepaintManager
{
	//{{{ FastRepaintManager constructor
	FastRepaintManager(JEditTextArea textArea,
		TextAreaPainter painter)
	{
		this.textArea = textArea;
		this.painter = painter;
	} //}}}

	//{{{ updateGraphics() method
	void updateGraphics()
	{
		if(gfx != null)
			gfx.dispose();

		img = painter.createImage(
			painter.getWidth(),
			painter.getHeight());
		gfx = (Graphics2D)img.getGraphics();
		gfx.clipRect(0,0,painter.getWidth(),painter.getHeight());
	} //}}}

	//{{{ getGraphics() method
	Graphics2D getGraphics()
	{
		return gfx;
	} //}}}

	//{{{ prepareGraphics() method
	class RepaintLines
	{
		int first, last;

		RepaintLines(int first, int last)
		{
			this.first = first;
			this.last = last;
		}
	} //}}}

	//{{{ prepareGraphics() method
	RepaintLines prepareGraphics(Rectangle clipRect, int firstLine)
	{
		gfx.setFont(painter.getFont());
		gfx.setColor(painter.getBackground());

		int height = gfx.getFontMetrics().getHeight();

		if(fullRepaint)
		{
			int lineDelta = (this.firstLine - firstLine);
			int yDelta = lineDelta * height;
			int visibleLines = textArea.getVisibleLines();

			if(lineDelta > -visibleLines
				&& lineDelta < visibleLines)
			{
				if(lineDelta < 0)
				{
					gfx.copyArea(0,-yDelta,painter.getWidth(),
						painter.getHeight() + yDelta,0,yDelta);
					return new RepaintLines(
						visibleLines + this.firstLine
						- firstLine - 1,
						visibleLines - 1);
				}
				else if(lineDelta > 0)
				{
					gfx.copyArea(0,0,painter.getWidth(),
						painter.getHeight() - yDelta,0,yDelta);
					return new RepaintLines(0,
						this.firstLine - firstLine);
				}
			}
		}

		// Because the clipRect's height is usually an even multiple
		// of the font height, we subtract 1 from it, otherwise one
		// too many lines will always be painted.
		return new RepaintLines(
			clipRect.y / height,
			(clipRect.y + clipRect.height - 1) / height);
	} //}}}

	//{{{ paint() method
	void paint(Graphics g)
	{
		firstLine = textArea.getFirstLine();
		g.drawImage(img,0,0,null);
	} //}}}

	//{{{ setFullRepaint() method
	void setFullRepaint(boolean fullRepaint)
	{
		this.fullRepaint = fullRepaint;
	} //}}}

	//{{{ Private members
	private JEditTextArea textArea;
	private TextAreaPainter painter;
	private Graphics2D gfx;
	private Image img;
	private boolean fullRepaint;
	/* Most recently rendered first line */
	private int firstLine;
	//}}}
}

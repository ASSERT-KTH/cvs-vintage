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

package org.columba.core.gui.util;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;

import java.text.AttributedString;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A Swing component capable of displaying text in multiple lines.
 */
public class MultiLineLabel extends JComponent {
    private String text;
    private int[] lineBreaks;
    protected LineBreakMeasurer measurer;
    protected int lineSpacing = 4;
    
    /**
     * Creates a new label with the given text.
     */
    public MultiLineLabel(String text) {
        setForeground(UIManager.getColor("Label.foreground"));
        setFont(UIManager.getFont("Label.font"));
        setAlignmentX(LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(
                Toolkit.getDefaultToolkit().getScreenSize().width / 3, 50));
        setText(text);
    }
    
    /**
     * Returns the label's text.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets the label's text.
     */
    public void setText(String text) {
        String oldValue = this.text;
        this.text = text;
        measurer = null;
        firePropertyChange("text", oldValue, text);
        revalidate();
        repaint();
    }
    
    /**
     * Returns the amount of space between the lines.
     */
    public int getLineSpacing() {
        return lineSpacing;
    }
    
    /**
     * Sets the amount of space between the lines.
     */
    public void setLineSpacing(int lineSpacing) {
        Integer oldValue = new Integer(this.lineSpacing);
        this.lineSpacing = lineSpacing;
        firePropertyChange("lineSpacing", oldValue, new Integer(lineSpacing));
        revalidate();
        repaint();
    }
    
    /**
     * Overridden to return appropriate values. This method takes the parent
     * component's size into account.
     */
    public Dimension getMinimumSize() {
        int height = 5;
        int width = 0;
        Container parent = getParent();
        if (parent != null) {
            width = parent.getWidth();
        }
        if (width == 0) {
            width = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
        }
        LineBreakMeasurer measurer = getLineBreakMeasurer();
        TextLayout layout;
        int i = 0;
        while (measurer != null && measurer.getPosition() < text.length()) {
            layout = measurer.nextLayout(width - 20, lineBreaks[i], false);
            
            //if we stopped at line break, increase array index pointer
            if (measurer.getPosition() == lineBreaks[i]) {
                i++;
            }
            
            //increase minimum height by line height and line spacing
            height += layout.getAscent() + layout.getDescent() + 
                        layout.getLeading() + lineSpacing;
        }
        
        //add the component's border insets to our minimum dimension
        Insets insets = getInsets();
        return new Dimension(width + insets.left + insets.right,
                height + insets.top + insets.bottom);
    }
    
    protected LineBreakMeasurer getLineBreakMeasurer() {
        if (measurer == null) {
            if (text != null && text.length() > 0) {
                AttributedString string = new AttributedString(text);
                string.addAttribute(TextAttribute.FONT, getFont());
                measurer = new LineBreakMeasurer(string.getIterator(),
                        ((Graphics2D)getGraphics()).getFontRenderContext());
                
                //check for line breaks
                List temp = new LinkedList();
                int i;
                char c;
                for (i = 0; i < text.length(); i++) {
                    c = text.charAt(i);
                    if (c == '\r' || c == '\n') {
                        temp.add(new Integer(i + 1));
                    }
                }
                //put them into the array
                i = 0;
                lineBreaks = new int[temp.size() + 1];
                Iterator iterator = temp.iterator();
                while (iterator.hasNext()) {
                    lineBreaks[i++] = ((Integer)iterator.next()).intValue();
                }
                lineBreaks[i] = text.length();
            }
        } else {
            measurer.setPosition(0);
        }
        return measurer;
    }
    
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(getForeground());
        Graphics2D g = (Graphics2D)graphics;
        LineBreakMeasurer measurer = getLineBreakMeasurer();
        float wrappingWidth = getWidth() - 15;
        if (wrappingWidth <= 0 || measurer == null) {
            return;
        }
        Insets insets = getInsets();
        Point pen = new Point(5 + insets.left, 5 + insets.top);
        TextLayout layout;
        int i = 0;
        while (measurer.getPosition() < text.length()) {
            layout = measurer.nextLayout(wrappingWidth, lineBreaks[i], false);
            
            //if we stopped at line break, increase array index pointer
            if (measurer.getPosition() == lineBreaks[i]) {
                i++;
            }
            
            //draw line
            pen.y += layout.getAscent();
            float dx = layout.isLeftToRight() ?
                    0 : (wrappingWidth - layout.getAdvance());
            layout.draw(g, pen.x + dx, pen.y);
            pen.y += layout.getDescent() + layout.getLeading() + lineSpacing;
        }
    }
    
	
	/**
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		
		return getMinimumSize();
	}
}

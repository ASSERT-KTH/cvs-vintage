// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.themes.contrastcolumba;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

public class ContrastColumbaBorders { 

    // OK
    public static class BevelBorder extends AbstractBorder implements UIResource {
	private Color darkShadow;
	private Color lightShadow;
	private Color mediumShadow;
	private boolean isRaised;

	public BevelBorder(boolean isRaised, Color lightShadow, 
			   Color mediumShadow, Color darkShadow) {
	    this.isRaised = isRaised;
            this.darkShadow = darkShadow;
            this.lightShadow = lightShadow;
	    this.mediumShadow = mediumShadow;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	    ContrastColumbaGraphicUtils.drawBorder(g, x, y, w, h, 
					lightShadow, mediumShadow, darkShadow,
					isRaised);
	}

	public Insets getBorderInsets(Component c) {
	    if (isRaised)
		return new Insets(1, 1, 2, 2);
	    else
		return new Insets(2, 2, 1, 1);
	}

	public boolean isOpaque(Component c) { 
	    return true;
	}

    }


    // OK
    public static class FocusBorder extends AbstractBorder implements UIResource {
        private Color focus;
	private Color control;

	public FocusBorder(Color control, Color focus) {
            this.control = control;
            this.focus = focus;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	    if (((JComponent)c).hasFocus()) {
	        g.setColor(focus); 
		g.drawRect(x, y, w-1, h-1);
	    }
	    else {
		g.setColor(control);
		g.drawRect(x, y, w-1, h-1);
	    }
	}

	public Insets getBorderInsets(Component c) { 
	    return new Insets(1, 1, 1, 1);
	}

	final static Insets insets = new Insets(1, 1, 1, 1);
    }


    // SEEMS OK
    public static class ButtonBorder extends AbstractBorder implements UIResource {
        protected Color focus;
        protected Color darkShadow;
        protected Color lightShadow;
        protected Color mediumShadow;


        public ButtonBorder(Color light, Color medium, Color dark, Color focus) {
	    lightShadow = light;
	    mediumShadow = medium;
	    darkShadow = dark;
	    this.focus = focus;
        }
      
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            boolean isPressed = false;
            boolean hasFocus = false;
            boolean canBeDefault = false;
            boolean isDefault = false;

	    if (c instanceof AbstractButton) {
 	        AbstractButton b = (AbstractButton)c;
 	        ButtonModel model = b.getModel();

 	        isPressed = (model.isArmed() && model.isPressed());
 	        hasFocus = (model.isArmed() && isPressed) || 
		    (b.isFocusPainted() && b.hasFocus());
		if (b instanceof JButton) {
		    canBeDefault = ((JButton)b).isDefaultCapable();
		    isDefault = ((JButton)b).isDefaultButton();
		}
	    }
	    int bx1 = x+1;
	    int by1 = y+1;
	    int bx2 = x+w-2;
	    int by2 = y+h-2;
	    
	    if (canBeDefault) {
		if (isDefault) {
		    ContrastColumbaGraphicUtils.drawBorder(g, x+3, y+3, w-6, h-6,
						lightShadow, mediumShadow, darkShadow,
						false);
                }
                bx1 +=8; by1 += 8; bx2 -= 8; by2 -= 8;
            }
	    
	    if (hasFocus) {
		g.setColor(focus);
		g.drawRect(bx1, by1, bx2-bx1, by2-by1);
		bx1++; by1++; bx2--; by2--;
	    }

	    if (isPressed) {
		g.setColor(mediumShadow);
		g.drawLine(bx1, by1, bx2, by1);
		g.drawLine(bx1, by1, bx1, by2);
		g.setColor(lightShadow);
		g.drawLine(bx2, by1+1, bx2, by2);
		g.drawLine(bx1+1, by2, bx2, by2);
		g.setColor(darkShadow);
		g.drawLine(bx1+1, by1+1, bx2-2, by1+1);
		g.drawLine(bx1+1, by1+1, bx1+1, by2-1);
	    }
	    else {
		g.setColor(lightShadow);
		g.drawLine(bx1, by1, bx2-1, by1);
		g.drawLine(bx1, by1, bx1, by2-1);
		g.setColor(darkShadow);
		g.drawLine(bx2, by1, bx2, by2);
		g.drawLine(bx1, by2, bx2, by2);
		g.setColor(mediumShadow);
		g.drawLine(bx1+2, by2-1, bx2-1, by2-1);
		g.drawLine(bx2-1, by1+2, bx2-1, by2-1);
	    }

        }
	
        public Insets getBorderInsets(Component c) {
            if (c instanceof JButton) {
                JButton b = (JButton)c;
                return (b.isDefaultCapable()? new Insets(10, 10, 10, 10) : 
			new Insets(2, 2, 2, 2));
            }
            return new Insets(2, 2, 2, 2);
        }

    }


    // SEEMS OK
    public static class ToggleButtonBorder extends ButtonBorder {

        public ToggleButtonBorder(Color lightShadow, Color mediumShadow, 
				  Color darkShadow, Color focus) {
	    super(lightShadow, mediumShadow, darkShadow, focus);
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	    boolean hasFocus = false;
	    boolean isPressed = false;
	    boolean isSelected = false;

	    if (c instanceof AbstractButton) {
	        AbstractButton b = (AbstractButton)c;
	        ButtonModel model = b.getModel();

 	        isPressed = (model.isArmed() && model.isPressed());
 	        hasFocus = (model.isArmed() && isPressed) || 
		    (b.isFocusPainted() && b.hasFocus());
		isSelected = model.isSelected();
	    }

	    int bx1 = x;
	    int by1 = y;
	    int bx2 = x+w-1;
	    int by2 = y+h-1;

	    if (hasFocus) {
		g.setColor(focus);
		g.drawRect(bx1, by1, bx2-bx1, by2-by1);
		bx1++; by1++; bx2--; by2--;
	    }

	    if (isPressed) {
		ContrastColumbaGraphicUtils.drawBorder(g, bx1, by1, bx2-bx1+1, by2-by1+1,
					    lightShadow, mediumShadow, darkShadow, 
					    false);
	    } 
	    else {
		ContrastColumbaGraphicUtils.drawBorder(g, bx1, by1, bx2-bx1+1, by2-by1+1,
					    lightShadow, mediumShadow, darkShadow,
					    !isSelected);
	    }
        }

        public Insets getBorderInsets(Component c) {
	    return new Insets(1, 1, 1, 1);
        }
    }


    // SEEMS OK
    public static class RadioButtonBorder extends ButtonBorder {

        public RadioButtonBorder(Color lightShadow, Color mediumShadow,
                                 Color darkShadow, Color focus) {
	    super(lightShadow, mediumShadow, darkShadow, focus);
        }

      
        public void paintBorder(Component c, Graphics g, int x, int y, 
				int width, int height) {
	    
	    if (c instanceof AbstractButton) {
	        AbstractButton b = (AbstractButton)c;
	        ButtonModel model = b.getModel();
	      
	        if (model.isArmed() && model.isPressed() || model.isSelected()) {
		    ContrastColumbaGraphicUtils.drawBorder(g, x, y, width, height,
						lightShadow, mediumShadow, darkShadow,
						false);
	        }
		else {
		    ContrastColumbaGraphicUtils.drawBorder(g, x, y, width, height,
						lightShadow, mediumShadow, darkShadow,
						true);
	        }
	    }
	    else {	
		    ContrastColumbaGraphicUtils.drawBorder(g, x, y, width, height,
						lightShadow, mediumShadow, darkShadow,
						true);
	    }
        }
      
        public Insets getBorderInsets(Component c)       {
	    return new Insets(2, 2, 2, 2);
        }
    }


    // SEEMS OK
    public static class TableHeaderBorder extends ButtonBorder {

        public TableHeaderBorder(Color lightShadow, Color mediumShadow,
                                 Color darkShadow, Color focus) {
	    super(lightShadow, mediumShadow, darkShadow, focus);
        }

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	    ContrastColumbaGraphicUtils.drawBorder(g, x, y, w, h, 
					lightShadow, mediumShadow, darkShadow, true);
	}

	public Insets getBorderInsets(Component c) {
	    return new Insets(1, 1, 2, 2);
	}
    }


    // TOBEREVISED
    public static class ScrollPaneBorder extends ButtonBorder implements UIResource {

	private static final Insets insets = new Insets(2, 2, 2, 2);
	private static Color bgColor;
	
        public ScrollPaneBorder(Color lightShadow, Color mediumShadow,
				Color darkShadow, Color focus) {
	    super(lightShadow, mediumShadow, darkShadow, focus);
	    bgColor = UIManager.getColor("window");
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
				int w, int h) {

            JScrollPane scroll = (JScrollPane)c;
            JComponent colHeader = scroll.getColumnHeader();
            int colHeaderHeight = 0;
            if (colHeader != null)
               colHeaderHeight = colHeader.getHeight();

            JComponent rowHeader = scroll.getRowHeader();
            int rowHeaderWidth = 0;
            if (rowHeader != null)
               rowHeaderWidth = rowHeader.getWidth();

	    ContrastColumbaGraphicUtils.drawBorder(g, x, y, w, h,
					lightShadow, mediumShadow, darkShadow,
					false);
        }

        public Insets getBorderInsets(Component c)       {
            return insets;
        }
    }
    

    // OK
    public static class MenuBarBorder extends AbstractBorder implements UIResource {
        protected static Color darkShadow;
        protected static Color lightShadow;
        protected static Color mediumShadow;

	protected static Insets borderInsets = new Insets(2, 2, 2, 2);
	
	public MenuBarBorder(Color light, Color medium, Color dark) {
	    darkShadow = dark;
	    lightShadow = light;
	    mediumShadow = medium;
	}

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	    ContrastColumbaGraphicUtils.drawBorder(g, x, y, w, h,
					lightShadow, mediumShadow, darkShadow,
					true);
        }

        public Insets getBorderInsets(Component c)       {
            return borderInsets;
        }
    }


    public static class FrameBorder extends AbstractBorder implements UIResource {

        JComponent jcomp;
        Color frameHighlight;
        Color frameColor;
        Color frameShadow;

        // The width of the border
        public final static int BORDER_SIZE = 5;

        /** Constructs an FrameBorder for the JComponent <b>comp</b>.
	 */
        public FrameBorder(JComponent comp) {
            jcomp = comp;
        }

        /** Sets the FrameBorder's JComponent.
	 */
        public void setComponent(JComponent comp) {
            jcomp = comp;
        }

        /** Returns the FrameBorder's JComponent.
	 * @see #setComponent
	 */
        public JComponent component() {
            return jcomp;
        }

        protected Color getFrameHighlight() {
            return frameHighlight;
        }

        protected Color getFrameColor() {
            return frameColor;
        }
    
        protected Color getFrameShadow() {
            return frameShadow;
        }

        static Insets insets = new Insets(BORDER_SIZE, BORDER_SIZE,
					  BORDER_SIZE, BORDER_SIZE);

        public Insets getBorderInsets(Component c) {
            return insets;
        }

	/** Draws the FrameBorder's top border.
         */
        protected boolean drawTopBorder(Component c, Graphics g, 
					int x, int y, int width, int height) {
            Rectangle titleBarRect = new Rectangle(x, y, width, BORDER_SIZE);
            if (!g.getClipBounds().intersects(titleBarRect)) {
                return false;
            }

            int maxX = width - 1;
            int maxY = BORDER_SIZE - 1;

            // Draw frame
            g.setColor(frameColor);
            g.drawLine(x, y + 2, maxX - 2, y + 2);
            g.drawLine(x, y + 3, maxX - 2, y + 3);
            g.drawLine(x, y + 4, maxX - 2, y + 4);

            // Draw highlights
            g.setColor(frameHighlight);
            g.drawLine(x, y, maxX, y);
            g.drawLine(x, y + 1, maxX, y + 1);
            g.drawLine(x, y + 2, x, y + 4);
            g.drawLine(x + 1, y + 2, x + 1, y + 4);

            // Draw shadows
            g.setColor(frameShadow);
            g.drawLine(x + 4, y + 4, maxX - 4, y + 4);
            g.drawLine(maxX, y + 1, maxX, maxY);
            g.drawLine(maxX - 1, y + 2, maxX - 1, maxY);

            return true;
        }

        /** Draws the FrameBorder's left border.
	 */
        protected boolean drawLeftBorder(Component c, Graphics g, int x, int y, 
					 int width, int height) {
            Rectangle borderRect = 
                new Rectangle(0, 0, getBorderInsets(c).left, height);
            if (!g.getClipBounds().intersects(borderRect)) {
                return false;
            }

            int startY = BORDER_SIZE;

            g.setColor(frameHighlight);
            g.drawLine(x, startY, x, height - 1);
            g.drawLine(x + 1, startY, x + 1, height - 2);

            g.setColor(frameColor);
            g.fillRect(x + 2, startY, x + 2, height - 3);

            g.setColor(frameShadow);
            g.drawLine(x + 4, startY, x + 4, height - 5);

            return true;
        }

        /** Draws the FrameBorder's right border.
	 */
        protected boolean drawRightBorder(Component c, Graphics g, int x, int y, 
					  int width, int height) {
            Rectangle borderRect = new Rectangle(
						 width - getBorderInsets(c).right, 0,
						 getBorderInsets(c).right, height);
            if (!g.getClipBounds().intersects(borderRect)) {
                return false;
            }

            int startX = width - getBorderInsets(c).right;
            int startY = BORDER_SIZE;

            g.setColor(frameColor);
            g.fillRect(startX + 1, startY, 2, height - 1);

            g.setColor(frameShadow);
            g.fillRect(startX + 3, startY, 2, height - 1);

            g.setColor(frameHighlight);
            g.drawLine(startX, startY, startX, height - 1);

            return true;
        }

        /** Draws the FrameBorder's bottom border.
	 */
        protected boolean drawBottomBorder(Component c, Graphics g, int x, int y, 
					   int width, int height) {
            Rectangle    borderRect;
            int     marginHeight, startY;

            borderRect = new Rectangle(0, height - getBorderInsets(c).bottom,
				       width, getBorderInsets(c).bottom);
            if (!g.getClipBounds().intersects(borderRect)) {
                return false;
            }

            startY = height - getBorderInsets(c).bottom;

            g.setColor(frameShadow);
            g.drawLine(x + 1, height - 1, width - 1, height - 1);
            g.drawLine(x + 2, height - 2, width - 2, height - 2);

            g.setColor(frameColor);
            g.fillRect(x + 2, startY + 1, width - 4, 2);

            g.setColor(frameHighlight);
            g.drawLine(x + 5, startY, width - 5, startY);

            return true;
        }

        // Returns true if the associated component has focus.
        protected boolean isActiveFrame() {
            return jcomp.hasFocus();
        }

        /** Draws the FrameBorder in the given Rect.  Calls
	 * <b>drawTitleBar</b>, <b>drawLeftBorder</b>, <b>drawRightBorder</b> and
	 * <b>drawBottomBorder</b>.
	 */
        public void paintBorder(Component c, Graphics g, 
				int x, int y, int width, int height) {
            if (isActiveFrame()) {
                frameColor = UIManager.getColor("activeCaptionBorder");
            } else {
                frameColor = UIManager.getColor("inactiveCaptionBorder");
            }
            frameHighlight = frameColor.brighter();
            frameShadow = frameColor.darker().darker();

            drawTopBorder(c, g, x, y, width, height);
            drawLeftBorder(c, g, x, y, width, height);
            drawRightBorder(c, g, x, y, width, height);
            drawBottomBorder(c, g, x, y, width, height);
        }
    }

    public static class InternalFrameBorder extends FrameBorder {

        JInternalFrame frame;

        // The size of the bounding box for Gtk frame corners.
        public final static int CORNER_SIZE = 24;

        /** Constructs an InternalFrameBorder for the InternalFrame
	 * <b>aFrame</b>.
	 */
        public InternalFrameBorder(JInternalFrame aFrame) {
            super(aFrame);
            frame = aFrame;
        }

        /** Sets the InternalFrameBorder's InternalFrame.
	 */
        public void setFrame(JInternalFrame aFrame) {
            frame = aFrame;
        }

        /** Returns the InternalFrameBorder's InternalFrame.
	 * @see #setFrame
	 */
        public JInternalFrame frame() {
            return frame;
        }

        /** Returns the width of the InternalFrameBorder's resize controls,
	 * appearing along the InternalFrameBorder's bottom border.  Clicking
	 * and dragging within these controls lets the user change both the
	 * InternalFrame's width and height, while dragging between the controls
	 * constrains resizing to just the vertical dimension.  Override this
	 * method if you implement your own bottom border painting and use a
	 * resize control with a different size.
	 */
        public int resizePartWidth() {
            if (!frame.isResizable()) {
                return 0;
            }
            return FrameBorder.BORDER_SIZE;
        }

        /** Draws the InternalFrameBorder's top border.
         */
        protected boolean drawTopBorder(Component c, Graphics g, 
					int x, int y, int width, int height) {
            if (super.drawTopBorder(c, g, x, y, width, height) && 
                frame.isResizable()) {
                g.setColor(getFrameShadow());
                g.drawLine(CORNER_SIZE - 1, y + 1, CORNER_SIZE - 1, y + 4);
                g.drawLine(width - CORNER_SIZE - 1, y + 1, 
			   width - CORNER_SIZE - 1, y + 4);

                g.setColor(getFrameHighlight());
                g.drawLine(CORNER_SIZE, y, CORNER_SIZE, y + 4);
                g.drawLine(width - CORNER_SIZE, y, width - CORNER_SIZE, y + 4);
                return true;
            }
            return false;
        }

        /** Draws the InternalFrameBorder's left border.
	 */
        protected boolean drawLeftBorder(Component c, Graphics g, int x, int y, 
					 int width, int height) {
            if (super.drawLeftBorder(c, g, x, y, width, height) && 
                frame.isResizable()) {
                g.setColor(getFrameHighlight());
                int topY = y + CORNER_SIZE;
                g.drawLine(x, topY, x + 4, topY);
                int bottomY = height - CORNER_SIZE;
                g.drawLine(x + 1, bottomY, x + 5, bottomY);
                g.setColor(getFrameShadow());
                g.drawLine(x + 1, topY - 1, x + 5, topY - 1);
                g.drawLine(x + 1, bottomY - 1, x + 5, bottomY - 1);
                return true;
            }
            return false;
        }

        /** Draws the InternalFrameBorder's right border.
	 */
        protected boolean drawRightBorder(Component c, Graphics g, int x, int y, 
					  int width, int height) {
            if (super.drawRightBorder(c, g, x, y, width, height) && 
                frame.isResizable()) {
                int startX = width - getBorderInsets(c).right;
                g.setColor(getFrameHighlight());
                int topY = y + CORNER_SIZE;
                g.drawLine(startX, topY, width - 2, topY);
                int bottomY = height - CORNER_SIZE;
                g.drawLine(startX + 1, bottomY, startX + 3, bottomY);
                g.setColor(getFrameShadow());
                g.drawLine(startX + 1, topY - 1, width - 2, topY - 1);
                g.drawLine(startX + 1, bottomY - 1, startX + 3, bottomY - 1);
                return true;
            }
            return false;
        }

        /** Draws the InternalFrameBorder's bottom border.
	 */
        protected boolean drawBottomBorder(Component c, Graphics g, int x, int y, 
					   int width, int height) {
            if (super.drawBottomBorder(c, g, x, y, width, height) &&
                frame.isResizable()) {
                int startY = height - getBorderInsets(c).bottom;

                g.setColor(getFrameShadow());
                g.drawLine(CORNER_SIZE - 1, startY + 1, 
			   CORNER_SIZE - 1, height - 1);
                g.drawLine(width - CORNER_SIZE, startY + 1, 
			   width - CORNER_SIZE, height - 1);
        
                g.setColor(getFrameHighlight());
                g.drawLine(CORNER_SIZE, startY, CORNER_SIZE, height - 2);
                g.drawLine(width - CORNER_SIZE + 1, startY, 
			   width - CORNER_SIZE + 1, height - 2);
                return true;
            }
            return false;
        }

        // Returns true if the associated internal frame has focus.
        protected boolean isActiveFrame() {
            return frame.isSelected();
        }
    }

}



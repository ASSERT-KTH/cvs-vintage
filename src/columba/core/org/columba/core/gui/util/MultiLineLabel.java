package org.columba.core.gui.util;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;

import java.text.AttributedString;
import java.text.BreakIterator;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A Swing component capable of displaying text in multiple lines.
 */
public class MultiLineLabel extends JComponent {
    private String text = "";
    protected BreakIterator breakIterator;
    protected LineBreakMeasurer measurer;
    protected int lineSpacing = 4;
    
    /**
     * Creates a new label with the given text.
     */
    public MultiLineLabel(String text) {
        breakIterator = BreakIterator.getLineInstance();
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
        breakIterator.setText(text);
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
        while (measurer != null && measurer.getPosition() < text.length()) {
            layout = measurer.nextLayout(width - 20);
            height += layout.getAscent() + layout.getDescent() + 
                        layout.getLeading() + lineSpacing;
        }
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
                        breakIterator, ((Graphics2D)getGraphics())
                                .getFontRenderContext());
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
        while (measurer.getPosition() < text.length()) {
            layout = measurer.nextLayout(wrappingWidth);
            pen.y += layout.getAscent();
            float dx = layout.isLeftToRight() ?
                    0 : (wrappingWidth - layout.getAdvance());
            layout.draw(g, pen.x + dx, pen.y);
            pen.y += layout.getDescent() + layout.getLeading() + lineSpacing;
        }
    }
}

package org.columba.core.gui.themes.thincolumba;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import javax.swing.border.*;

public class ThinColumbaFolderInfoPanelBorder extends BevelBorder
{

    // private static final Insets insets = new Insets(0, 0, 0, 2);
    
    public ThinColumbaFolderInfoPanelBorder()
    {
	super(0);
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
                            int w, int h)
    {


        


	//g.translate( x, y);
	
	/*
            g.setColor( MetalLookAndFeel.getControl() );
            g.drawRect( 0, 0, w-1, h-1 );
            g.drawRect( 1, 1, w-2, h-2 );
	*/  
                       
            //g.translate( -x, -y);
	  
	    	g.setColor( MetalLookAndFeel.getControlHighlight() );
		    g.drawLine(0,0,c.getWidth()-1, 0 );
		    g.drawLine(0,0,0, c.getHeight()-1 );
		    
		    g.setColor( MetalLookAndFeel.getControlDarkShadow() );
		    g.drawLine(0,c.getHeight()-1,c.getWidth()-1, c.getHeight()-1 );
		    g.drawLine(c.getWidth()-1,0,c.getWidth()-1, c.getHeight()-1 );
	    

            
    }


    /*
    public Insets getBorderInsets( Component c )
    {
        return insets;
    }
    */



    
}

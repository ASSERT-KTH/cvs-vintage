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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

public class StartUpFrame extends Frame
{

    private ImageIcon[] anim = new ImageIcon[4];
    private StartUpWindow window;


    public StartUpFrame()
    {
        super();

        URL url;

          /*
            url = ClassLoader.getSystemResource("org/columba/core/images/splash.gif");
            anim[0] = ImageLoader.load( url );

            url = ClassLoader.getSystemResource("org/columba/core/images/dove00.gif");
            anim[1] = ImageLoader.load( url );

            url = ClassLoader.getSystemResource("org/columba/core/images/dove01.gif");
            anim[2] = ImageLoader.load( url );

            url = ClassLoader.getSystemResource("org/columba/core/images/dove02.gif");
            anim[3] = ImageLoader.load( url );
          */

        anim[0] = ImageLoader.getImageIcon("splash.gif");
        anim[1] = ImageLoader.getImageIcon("dove00.gif");
        anim[2] = ImageLoader.getImageIcon("dove01.gif");
        anim[3] = ImageLoader.getImageIcon("dove02.gif");


        window = new StartUpWindow( this, anim );

    }

    public void advance()
    {
        window.advance();
        window.repaint();
    }


    public void setText( String s )
    {
//        window.setText( s );
//        window.repaint();
    }


    public void setVisible( boolean b )
    {
        window.setVisible( b );
    }



    class StartUpWindow extends Window
    {
        ImageIcon img;
        ImageIcon[] anim;
        int w, h;
        String txt;
        int fh=-1;
        Font font=null;
        BufferedImage buffer;
        int status;
        
        
        StartUpWindow(Frame parent, ImageIcon[] anim)
        {
            super(parent);
            this.anim = anim;
            img = anim[0];
            
            
            status = 0;
            
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
              //Rectangle winDim = this.getBounds();
            w = img.getIconWidth();
            h = img.getIconHeight();

            buffer = new BufferedImage( w+2, h+2, BufferedImage.TYPE_INT_RGB );
            
            this.setSize(w+2, h+2);
            
            Dimension winDim = this.getSize();
            
            this.setLocation((screenDim.width - w) / 2,
                        (screenDim.height - h) / 2);
            
            this.setVisible(true);
        }

        public void advance()
        {
            status++;
        }

        public void setText( String s )
        {
            this.txt = s;
        }

        public void paint(Graphics g)
        {
            Graphics2D g2 = buffer.createGraphics();


            if (anim[status] != null)
            {
               g2.drawImage(anim[status].getImage(),1,1,this);

                  /*
                g2.setColor( UIManager.getColor( "controlLtHighlight" ) );
                g2.drawLine( 1,1,1,h-2 );
                g2.drawLine( 1,1,w-2,1 );
                g2.setColor( UIManager.getColor( "controlDkShadow" ) );
                g2.drawLine( 2,h-2, w-2, h-2 );
                g2.drawLine( w-2, 2, w-2, h-2 );
                  */


                g2.setColor( Color.black );
                g2.drawRect( 0,0,w-1+2,h-1+2 );

/*

                if ( txt != null )
                {
                    if ( font == null )
                        font = UIManager.getFont("MenuItem.font");

                    g2.setFont( font );

                    if ( fh == -1 )
                    {
                        FontMetrics m = g.getFontMetrics();
                          //fw = m.getWidth( txt );
                        fh = m.getHeight();
                    }


                    g2.setColor( Color.black );

                      //g2.drawString( txt, fh/2, h-( fh/2 ) );
                }
*/

            }

            g.drawImage( buffer, 0, 0, this );

        }

    }


}







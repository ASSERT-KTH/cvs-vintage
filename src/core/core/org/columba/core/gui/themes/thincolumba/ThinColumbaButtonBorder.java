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

package org.columba.core.gui.themes.thincolumba;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import org.columba.mail.gui.util.*;

import javax.swing.border.*;

public class ThinColumbaButtonBorder extends AbstractBorder implements UIResource
{

    protected static Insets borderInsets = new Insets( 4, 4, 4, 4 );
      
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
        JButton button = (JButton) c;
        ButtonModel model = button.getModel();

                      
        if ( model.isEnabled() )
        {
            if ( model.isPressed() && model.isArmed() )
            {
                  //drawRolloverBorder( g, x, y, w, h, true );
                ThinColumbaUtil.drawPressed3DBorder( g, x, y, w, h );
            }
            else
            {
                  
                if ( button.isRolloverEnabled() )
                {
       
                    
                    if ( model.isRollover() )
                    {
                        ThinColumbaUtil.drawRolloverBorder( g, x, y, w, h );
                    }
                    
                }
                else
                {
                    if (button.isDefaultButton())
                    {
                        ThinColumbaUtil.drawDefaultButtonBorder( g, x, y, w, h, button.hasFocus() && false);
                    } else
                    {
                        ThinColumbaUtil.drawButtonBorder( g, x, y, w, h, button.hasFocus() && false);
                    }
                                        
                }
                
            }
        } else
        { // disabled state
              //drawDisabledBorder( g, x, y, w-1, h-1 );
        }
        
    }
    
    public Insets getBorderInsets( Component c ) {
        return borderInsets;
    }


    }











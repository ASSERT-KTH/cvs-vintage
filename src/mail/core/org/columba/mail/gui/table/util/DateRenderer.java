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

package org.columba.mail.gui.table.util;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;


public class DateRenderer extends JLabel implements TableCellRenderer
{
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
      
    static SimpleDateFormat dfWeek = new SimpleDateFormat("EEE HH:mm", Locale.getDefault() );
    static DateFormat dfCommon = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    
    static final long OneDay=24*60*60*1000;
    static TimeZone localTimeZone = TimeZone.getDefault();

    
    public DateRenderer(boolean isBordered)
    {
        super();
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    public static int getLocalDaysDiff(long t)
    {
        
        return (int) ( (System.currentTimeMillis() + localTimeZone.getRawOffset() - 
                       ( (t+localTimeZone.getRawOffset() ) / OneDay ) * OneDay ) / OneDay
                       );
    }
    
    public Component getTableCellRendererComponent(
        JTable table, Object value, 
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
        if (isBordered)
        {
            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,table.getSelectionBackground() );
                }
                setBorder(selectedBorder);
                setBackground( table.getSelectionBackground() );
                setForeground( table.getSelectionForeground() );
            } else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                                                       table.getBackground() );
                }
                setBackground( table.getBackground() );
                setBorder(unselectedBorder);
                setForeground( table.getForeground() );
            }
        }

        if ( !( value instanceof Date ) )
        {
            setText("");
            return this;
        }
        
        
            
            
        Date date = (Date) value;

            //Date today = new Date();

        int diff = getLocalDaysDiff( date.getTime() );


            //if ( today
        if ( (diff>=0) && (diff<7) )
            setText( dfWeek.format( date ) );
        else
            setText( dfCommon.format( date ) );
        return this;
    }
}

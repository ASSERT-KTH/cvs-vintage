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

package org.columba.mail.parser;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateParser
{
    static final SimpleDateFormat DateHeaderFormats[]=
    {
        new SimpleDateFormat("EEE, dd MMM yy HH:mm:ss z", new DateFormatSymbols(Locale.US) ),
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", new DateFormatSymbols(Locale.US) ),
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", new DateFormatSymbols(Locale.US) ),
        new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", new DateFormatSymbols(Locale.US) ),
        new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", new DateFormatSymbols(Locale.US) )
            };
    
    public DateParser()
        {}
    
    protected static int getDayName(String str)
        {
            if (str.equalsIgnoreCase("Mon") ) return Calendar.MONDAY;
            
            if (str.equalsIgnoreCase("Tue") ) return Calendar.TUESDAY;
            if (str.equalsIgnoreCase("Wed") ) return Calendar.WEDNESDAY;
            if (str.equalsIgnoreCase("Thu") ) return Calendar.THURSDAY;
            if (str.equalsIgnoreCase("Fri") ) return Calendar.FRIDAY;
            if (str.equalsIgnoreCase("Sat") ) return Calendar.SATURDAY;
            if (str.equalsIgnoreCase("Sun") ) return Calendar.SUNDAY;
            return -1;
        }
    
    
    protected static int getMonthName(String str)
        {
            if (str.equalsIgnoreCase("Jan") ) return Calendar.JANUARY;
            if (str.equalsIgnoreCase("Feb") ) return Calendar.FEBRUARY;
            if (str.equalsIgnoreCase("Mar") ) return Calendar.MARCH;
            if (str.equalsIgnoreCase("Apr") ) return Calendar.APRIL;
            if (str.equalsIgnoreCase("Mai") ) return Calendar.MAY;
            if (str.equalsIgnoreCase("Jun") ) return Calendar.JUNE;
            if (str.equalsIgnoreCase("Jul") ) return Calendar.JULY;
            if (str.equalsIgnoreCase("Aug") ) return Calendar.AUGUST;
            if (str.equalsIgnoreCase("Sep") ) return Calendar.SEPTEMBER;
            if (str.equalsIgnoreCase("Oct") ) return Calendar.OCTOBER;
            if (str.equalsIgnoreCase("Nov") ) return Calendar.NOVEMBER;
            if (str.equalsIgnoreCase("Dec") ) return Calendar.DECEMBER;
            return -1;
            
        }
    

    public static Date parseString(String str)
        {
            
            //System.out.println("Date: "+str);

            Date date = null;
            
            for (int i=0;i<DateHeaderFormats.length;i++)
            {
                try
                {
                    date = DateHeaderFormats[i].parse( str );
                }
                catch(Exception  e)
                {
                    //System.out.println( e.getMessage() );
                }
                if( date != null ) break;
            }

			//System.out.println("Parsed Date : "+formatDate(date));

            if ( date == null ) return new Date(0);
            else
                return date;
            
        }

    public static String formatDate(Date date)
        {
                //       Date date = new Date();
                //date = parseString(str);
            if ( date==null)
            {
                return "";
            }
            else
            {
                return DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.MEDIUM).format(date);
            }
            
        }
}

    

    


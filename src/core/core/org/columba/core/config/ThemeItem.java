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

package org.columba.core.config;

import java.awt.Color;
import java.awt.Font;

import org.w3c.dom.Document;

public class ThemeItem extends DefaultItem
{
    private AdapterNode mainFontName, mainFontSize, textFontName, textFontSize;

    private AdapterNode theme;
    
    private AdapterNode foreground;
    private AdapterNode background;

    private AdapterNode iconset;
    private AdapterNode pulsator;
    
    private AdapterNode rootNode;

    public ThemeItem( Document doc, AdapterNode rootNode )
    {
        super( doc );

	this.rootNode = rootNode;

	parse();

        createMissingElements();
    }


    protected void parse()
    {
	

        for ( int i=0; i<rootNode.getChildCount(); i++ )
        {
            AdapterNode child = rootNode.getChildAt(i);

            if ( child.getName().equals("mainfont") )
            {
		AdapterNode subChild = child.getChild("name");
		mainFontName = subChild;
		subChild = child.getChild("size");
		mainFontSize = subChild;
            }
            else if ( child.getName().equals("textfont") )
            {
		AdapterNode subChild = child.getChild("name");
		textFontName = subChild;
		subChild = child.getChild("size");
		textFontSize = subChild;
            }
	    else if ( child.getName().equals("theme") )
		{
		    theme = child;
		}
	    else if ( child.getName().equals("iconset") )
		{
		    iconset = child;
		}
	    else if ( child.getName().equals("pulsator") )
		{
		    pulsator = child;
		}
	    else if ( child.getName().equals("foreground") )
		{
		    foreground = child;
		}
	    else if ( child.getName().equals("background") )
		{
		    background = child;
		}	      
	}
    }

    protected void createMissingElements()
    {
	

	if ( theme == null ) theme = addKey( rootNode, "theme", "2");
	if ( iconset == null ) iconset = addKey( rootNode, "iconset", "default");
	if ( pulsator == null ) pulsator = addKey( rootNode, "pulsator", "default");
	if ( foreground == null ) foreground = addKey( rootNode, "foreground", "16777215");
	if ( background == null ) background = addKey( rootNode, "background", "13948");

    }


    /*
    public void setThemeNode( AdapterNode node )
    {
        theme = node;
    }

        
    public void setMainFontNameNode( AdapterNode node )
    {
        mainFontName = node;
    }

    public void setMainFontSizeNode( AdapterNode node )
    {
        mainFontSize = node;
    }
    
    public void setTextFontNameNode( AdapterNode node )
    {
        textFontName = node;
    }

    public void setTextFontSizeNode( AdapterNode node )
    {
        textFontSize = node;
    }
    */
    


      /******************************************** set ***************************************/



    public void setMainFontName( String str )
    {
        setTextValue( mainFontName, str );
    }

    public void setMainFontSize( String str )
    {
        setTextValue( mainFontSize, str );
    }

    public void setTextFontSize( int i )
    {
        Integer size = new Integer( i );

        setTextFontSize( size.toString() );    
    }

    public void setMainFontSize( int i )
    {
        Integer size = new Integer( i );

        setMainFontSize( size.toString() );    
    }
    
    public void setTextFontName( String str )
    {
        setTextValue( textFontName, str );
    }

    public void setTextFontSize( String str )
    {
        setTextValue( textFontSize, str );
    }



    public void setTheme( int i )
    {
        String str = ( new Integer(i) ).toString();
        setTextValue( theme, str );
    }


    public void setIconset( String s )
    {
	setTextValue( iconset, s );
    }

    public void setPulsator( String s )
    {
	setTextValue( pulsator, s );
    }

    public void setForeground( Color c )
    {
	Integer in = new Integer( c.getRGB() );

	setTextValue( foreground, in.toString() );
    }

    public void setBackground( Color c )
    {
	Integer in = new Integer( c.getRGB() );

	setTextValue( background, in.toString() );
    }

    
      /**************************************************** get *********************************/

    
    public int getTheme()
    {
        String str = getTextValue( theme );
        int i = Integer.parseInt( str );

        return i;
    }

    public String getMainFontName()
    {
        return getTextValue( mainFontName );
    }

      /*
        public String getMainFontSize()
        {
        return getTextValue( mainFontSize );
        }
      */
    
    public int getMainFontSize()
    {
        Integer i = new Integer(  getTextValue( mainFontSize ) );
        
        return i.intValue();
    }
    
    public String getTextFontName()
    {
        return getTextValue( textFontName );
    }

      /*
        public String getTextFontSize()
        {
        return getTextValue( textFontSize );
        }
      */

    public int getTextFontSize()
    {
        Integer i = new Integer(  getTextValue( textFontSize ) );
        
        return i.intValue();
    }
    
    
    public String getIconset()
    {
	String str = getTextValue(iconset);
	return str;
    }

    public String getPulsator()
    {
	String str = getTextValue(pulsator);
	return str;
    }

    public Color getForeground()
    {
	String str = getTextValue(foreground);
	
	Color color = new Color( Integer.parseInt(str) );

	return color;
    }

    public Color getBackground()
    {
	String str = getTextValue(background);
	Color color = new Color( Integer.parseInt(str) );

	return color;
    }
    

    public Font getMainFont()
    {
	String mainName = getMainFontName();
	int mainSize = getMainFontSize();

	Font mainFont = new Font( mainName, Font.PLAIN, mainSize );

	return mainFont;
    }

    public Font getTextFont()
    {
	String textName = getTextFontName();
	int textSize = getTextFontSize();

	Font textFont = new Font( textName, Font.PLAIN, textSize );
	
	return textFont;
    }

    public void setMainFont( Font f )
    {
	setMainFontName( f.getName() );
	setMainFontSize( f.getSize() );
    }

    public void setTextFont( Font f )
    {
	setTextFontName( f.getName() );
	setTextFontSize( f.getSize() );
    }
}
    




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
package org.columba.core.config;

import java.awt.Color;

import org.columba.core.xml.XmlElement;

public class ThemeItem extends DefaultItem {
	/*
	private AdapterNode mainFontName, mainFontSize, textFontName, textFontSize;
	
	private AdapterNode theme;
	
	private AdapterNode foreground;
	private AdapterNode background;
	
	private AdapterNode iconset;
	private AdapterNode pulsator;
	
	private AdapterNode rootNode;
	*/

	public ThemeItem(XmlElement root) {
		super(root);

	}

	public Color getForeground() {
		int r = getInteger("foreground", "r");
		int g = getInteger("foreground", "g");
		int b = getInteger("foreground", "b");

		Color color = new Color(r, g, b);

		return color;
	}

	public Color getBackground() {
		int r = getInteger("background", "r");
		int g = getInteger("background", "g");
		int b = getInteger("background", "b");

		Color color = new Color(r, g, b);
		return color;
	}

	/*
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
	*/

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

	/*
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
	*/

	/**************************************************** get *********************************/

	/*
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
	*/
	/*
	  public String getMainFontSize()
	  {
	  return getTextValue( mainFontSize );
	  }
	*/
	/*
	public int getMainFontSize()
	{
	    Integer i = new Integer(  getTextValue( mainFontSize ) );
	    
	    return i.intValue();
	}
	
	public String getTextFontName()
	{
	    return getTextValue( textFontName );
	}
	*/
	/*
	  public String getTextFontSize()
	  {
	  return getTextValue( textFontSize );
	  }
	*/

	/*
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
	*/
}

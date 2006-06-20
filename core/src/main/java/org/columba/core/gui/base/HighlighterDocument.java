package org.columba.core.gui.base;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class HighlighterDocument extends UndoDocument {
	
	/* CEDRIC: not used right now. */
	public void highlightInitialText(int length) {
		SimpleAttributeSet gray = new SimpleAttributeSet();
    	StyleConstants.setForeground(gray, Color.GRAY);
		setCharacterAttributes(0, length, gray, true);
	}

    public void insertString(int offs, String str, AttributeSet a) 
        throws BadLocationException {
    	
        super.insertString(offs, str, a);
        
        String s = getText(0,getLength());
        highlightText(s);
    }
    
    public void remove(int offs,int len)
     throws BadLocationException {
    	
    	super.remove(offs, len);
    	
        String s = getText(0,getLength());
        highlightText(s);
    }
    
    public void highlightText(String str) {
    	
    	String EMailRegex = "([a-zA-Z0-9]+([_+\\.-][a-zA-Z0-9]+)*@([a-zA-Z0-9]+([\\.-][a-zA-Z0-9]+)*)+\\.[a-zA-Z]{2,4})";
    	String URLRegex = "(\\b((\\w*(:\\S*)?@)?(http|https|ftp)://[\\S]+)(?=\\s|$))";
    	String regex = EMailRegex + "|" + URLRegex;
    	Pattern EMailPat = Pattern.compile(regex);
    	Matcher EMailMatcher = EMailPat.matcher(str);
    	
    	SimpleAttributeSet standard = new SimpleAttributeSet();
    	
		SimpleAttributeSet highlighted = new SimpleAttributeSet();
    	StyleConstants.setForeground(highlighted, Color.BLUE);	
    	StyleConstants.setUnderline(highlighted, true);
    	
    	int begin = 0;
    	int end;
    	while (EMailMatcher.find()) {
    		end = EMailMatcher.start();
    		if (end > 1)
    			setCharacterAttributes(begin, end - begin, standard, true);
    		
    		begin = end;
    		end = EMailMatcher.end();
    		setCharacterAttributes(begin, end - begin, highlighted, true);
    		begin = end;
    	}
    	setCharacterAttributes(begin, str.length(), standard, true);
    }
}

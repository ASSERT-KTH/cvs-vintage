package examples;


import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Log the contents of the body. Could be used to handle errors etc. 
 */
public class LogTag extends Tag {
    boolean toBrowser = false;
    
    public LogTag(String prefix, String tagname) {
        super(prefix, tagname);
    }
    

    public int doStartTag() {
        String value = tagData.getAttributeString("tobrowser");
        toBrowser = value.equalsIgnoreCase("true") ? true : false;
        return EVAL_BODY;
    }
    
    public int doAfterBody() throws JspError {
        String s = bodyOut.getString();
        System.err.println(s);
        if (toBrowser)
            bodyOut.writeOut(getPreviousOut());
        return SKIP_BODY;
    }
}

    
        
    

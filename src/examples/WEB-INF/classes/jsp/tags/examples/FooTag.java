package examples;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.Hashtable;
import java.io.Writer;
import java.io.IOException;

/**
 * Example1: the simplest tag
 * Collect attributes and call into some actions
 *
 * <foo att1="..." att2="...." att3="...." />
 */

public class FooTag extends Tag {

    private String atts[];
    int i = 0;
    
    /**
     * Create a new Foo tag handler
     *
     * @param prefix the tag library prefix used in the directive
     * @param tagname the tag name described in the TLD
     */
    public FooTag(String prefix, String tagname) {
	super(prefix, tagname);
    }

    /**
     * Process start tag
     *
     * @return EVAL_BODY
     */
    public int doStartTag() {
        atts = new String[3];
        i = 0;
	atts[0] = tagData.getAttributeString("att1");
	atts[1] = tagData.getAttributeString("att2");
	atts[2] = tagData.getAttributeString("att3");
        
        pageContext.setAttribute("member", atts[i]);
        
	return EVAL_BODY;
    }

    public void doBeforeBody() throws JspError {
    }

    public int doAfterBody() throws JspError {
        i++;
        if (i == 3) {
            bodyOut.writeOut(getPreviousOut());
            return SKIP_BODY;
        } else
            pageContext.setAttribute("member", atts[i]);
        
        return EVAL_BODY;
    }
}


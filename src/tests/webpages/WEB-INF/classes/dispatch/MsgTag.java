package dispatch;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class MsgTag extends TagSupport
{
    String msg;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int doEndTag()
            throws JspException
    {
        try {
            pageContext.getOut().print(msg);
        } catch (java.io.IOException ex) {
            throw new JspException(ex.toString());
        }
        return EVAL_PAGE;
    }
}
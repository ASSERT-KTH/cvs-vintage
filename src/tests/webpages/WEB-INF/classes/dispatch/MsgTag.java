/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

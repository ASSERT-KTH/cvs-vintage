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
/*
 * $Id: Counter.java,v 1.4 2004/02/27 05:28:09 billbarker Exp $
 */

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.IOException;

public class Counter extends HttpServlet {

    private int count = 0;
    private static final String MethodName = "method";
    private static final String OperationName = "operation";
    private static final String HTML = "html";
    private static final String Octet = "octet";
    private static final String Reset = "reset";
    private static final String Increment = "increment";
    private static final String HTMLMimeType = "text/html";
    private static final String OctetMimeType = "application/octet-stream";
    private static final boolean Debug = true;

    public void init(ServletConfig conf)
        throws ServletException {
        super.init(conf);

        count = 0;
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        String method =
            req.getParameter(this.MethodName.toLowerCase());
        String operation =
            req.getParameter(this.OperationName.toLowerCase());

        if (method == null) {
            method = this.HTML;
        } else if ((! method.toLowerCase().equals(this.HTML)) &&
	    (! method.toLowerCase().equals(this.Octet))) {
            method = this.HTML;
        }

        if (operation == null) {
            operation = this.Reset;
        } else if ((! operation.toLowerCase().equals(this.Reset)) &&
            (! operation.toLowerCase().equals(this.Increment))) {
            operation = this.Reset;
        }

        if (operation.toLowerCase().equals(this.Reset)) {
            count = 0;
        } else if (operation.toLowerCase().equals(this.Increment)) {
            count++;
        }

        String mimeType = null;

        if (method.toLowerCase().equals(this.HTML)) {
            mimeType = this.HTMLMimeType;
        } else if (method.toLowerCase().equals(this.Octet)) {
            mimeType = this.OctetMimeType;
        }
        
        res.setContentType(mimeType);

        ServletOutputStream out = res.getOutputStream();
        byte[] b = Integer.toString(count).getBytes("UTF8");

        try {
            out.write(b);
        } catch (Exception ioe) {
	    if (this.Debug) {
                ioe.printStackTrace();
            }
        }

        out.close();
    }
}


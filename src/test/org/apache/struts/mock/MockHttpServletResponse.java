/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/mock/MockHttpServletResponse.java,v 1.5 2004/03/14 06:23:52 sraeburn Exp $
 * $Revision: 1.5 $
 * $Date: 2004/03/14 06:23:52 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.struts.mock;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;



/**
 * <p>Mock <strong>HttpServletResponse</strong> object for low-level unit tests
 * of Struts controller components.  Coarser grained tests should be
 * implemented in terms of the Cactus framework, instead of the mock
 * object classes.</p>
 *
 * <p><strong>WARNING</strong> - Only the minimal set of methods needed to
 * create unit tests is provided, plus additional methods to configure this
 * object as necessary.  Methods for unsupported operations will throw
 * <code>UnsupportedOperationException</code>.</p>
 *
 * <p><strong>WARNING</strong> - Because unit tests operate in a single
 * threaded environment, no synchronization is performed.</p>
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/14 06:23:52 $
 */

public class MockHttpServletResponse implements HttpServletResponse {



    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods




    // -------------------------------------------- HttpServletResponse Methods


    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }


    public void addDateHeader(String name, long value) {
        throw new UnsupportedOperationException();
    }


    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }


    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }


    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException();
    }


    public String encodeRedirectUrl(String url) {
        return (encodeRedirectURL(url));
    }


    public String encodeRedirectURL(String url) {
        return (url);
    }


    public String encodeUrl(String url) {
        return (encodeURL(url));
    }


    public String encodeURL(String url) {
        return (url);
    }


    public void sendError(int status) {
        throw new UnsupportedOperationException();
    }


    public void sendError(int status, String message) {
        throw new UnsupportedOperationException();
    }


    public void sendRedirect(String location) {
        throw new UnsupportedOperationException();
    }


    public void setDateHeader(String name, long value) {
        throw new UnsupportedOperationException();
    }


    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }


    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }


    public void setStatus(int status) {
        throw new UnsupportedOperationException();
    }


    public void setStatus(int status, String message) {
        throw new UnsupportedOperationException();
    }


    // ------------------------------------------------ ServletResponse Methods


    public void flushBuffer() {
        throw new UnsupportedOperationException();
    }


    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }


    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }


    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }


    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }


    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException();
    }


    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }


    public void reset() {
        throw new UnsupportedOperationException();
    }


    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }


    public void setBufferSize(int size) {
        throw new UnsupportedOperationException();
    }


    public void setContentLength(int length) {
        throw new UnsupportedOperationException();
    }


    public void setContentType(String type) {
        throw new UnsupportedOperationException();
    }


    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }


}

/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/upload/CommonsMultipartRequestHandler.java,v 1.2 2002/07/31 06:43:18 martinc Exp $
 * $Revision: 1.2 $
 * $Date: 2002/07/31 06:43:18 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


package org.apache.struts.upload;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.ApplicationConfig;


 /**
  * This class implements the <code>MultipartRequestHandler</code> interface
  * by providing a wrapper around the Jakarta Commons FileUpload library.
  *
  * @author Martin Cooper
  * @version $Revision: 1.2 $ $Date: 2002/07/31 06:43:18 $
  * @since Struts 1.1
  */
public class CommonsMultipartRequestHandler implements MultipartRequestHandler {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The default value for the maximum allowable size, in bytes, of an
     * uploaded file. The value is equivalent to 250MB.
     */
    public static final long DEFAULT_SIZE_MAX = 250 * 1024 * 1024;


    /**
     * The default value for the threshold which determines whether an uploaded
     * file will be written to disk or cached in memory. The value is equivalent
     * to 250KB.
     */
    public static final int DEFAULT_SIZE_THRESHOLD = 256 * 1024;


    // ----------------------------------------------------- Instance Variables


    /**
     * Commons Logging instance.
     */
    protected static Log log = LogFactory.getLog(
            CommonsMultipartRequestHandler.class);


    /**
     * The combined text and file request parameters.
     */
    private Hashtable elementsAll;


    /**
     * The file request parameters.
     */
    private Hashtable elementsFile;


    /**
     * The text request parameters.
     */
    private Hashtable elementsText;


    /**
     * The action mapping  with which this handler is associated.
     */
    private ActionMapping mapping;


    /**
     * The servlet with which this handler is associated.
     */
    private ActionServlet servlet;


    // ---------------------------------------- MultipartRequestHandler Methods


    /**
     * Retrieves the servlet with which this handler is associated.
     *
     * @return The associated servlet.
     */
    public ActionServlet getServlet() {
        return this.servlet;
    }


    /**
     * Sets the servlet with which this handler is associated.
     *
     * @param servlet The associated servlet.
     */
    public void setServlet(ActionServlet servlet) {
        this.servlet = servlet;
    }


    /**
     * Retrieves the action mapping with which this handler is associated.
     *
     * @return The associated action mapping.
     */
    public ActionMapping getMapping() {
        return this.mapping;
    }


    /**
     * Sets the action mapping with which this handler is associated.
     *
     * @param mapping The associated action mapping.
     */
    public void setMapping(ActionMapping mapping) {
        this.mapping = mapping;
    }


    /**
     * Parses the input stream and partitions the parsed items into a set of
     * form fields and a set of file items. In the process, the parsed items
     * are translated from Commons FileUpload <code>FileItem</code> instances
     * to Struts <code>FormFile</code> instances.
     *
     * @param request The multipart request to be processed.
     *
     * @throws ServletException if an unrecoverable error occurs.
     */
    public void handleRequest(HttpServletRequest request)
            throws ServletException {

        // Get the app config for the current request.
        ApplicationConfig ac = (ApplicationConfig) request.getAttribute(
                Action.APPLICATION_KEY);

        // Create and configure a FileUpload instance.
        FileUpload upload = new FileUpload();
        // Set the maximum size before a FileUploadException will be thrown.
        upload.setSizeMax((int) getSizeMax(ac));
        // Set the maximum size that will be stored in memory.
        upload.setSizeThreshold(getSizeThreshold(ac));
        // Set the the location for saving data on disk.
        upload.setRepositoryPath(getRepositoryPath(ac));

        // Create the hash tables to be populated.
        elementsText = new Hashtable();
        elementsFile = new Hashtable();
        elementsAll = new Hashtable();

        // Parse the request into file items.
        List items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e) {
            // Special handling for uploads that are too big.
            if (e.getMessage().endsWith("size exceeds allowed range")) {
                request.setAttribute(
                        MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED,
                        Boolean.TRUE);
                return;
            }

            log.error("Failed to parse multipart request", e);
            throw new ServletException(e);
        }

        // Partition the items into form fields and files.
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {
                addTextParameter(request, item);
            } else {
                addFileParameter(request, item);
            }
        }
    }


    /**
     * Returns a hash table containing the text (i.e. non-file) request
     * parameters.
     *
     * @return The text request parameters.
     */
    public Hashtable getTextElements() {
        return this.elementsText;
    }


    /**
     * Returns a hash table containing the file (i.e. non-text) request
     * parameters.
     *
     * @return The file request parameters.
     */
    public Hashtable getFileElements() {
        return this.elementsFile;
    }


    /**
     * Returns a hash table containing both text and file request parameters.
     *
     * @return The text and file request parameters.
     */
    public Hashtable getAllElements() {
        return this.elementsAll;
    }


    /**
     * Cleans up when a problem occurs during request processing.
     */
    public void rollback() {
        Iterator iter = elementsFile.values().iterator();

        while (iter.hasNext()) {
            FormFile formFile = (FormFile) iter.next();

            formFile.destroy();
        }
    }


    /**
     * Cleans up at the end of a request.
     */
    public void finish() {
        rollback();
    }


    // -------------------------------------------------------- Support Methods


    /**
     * Returns the maximum allowable size, in bytes, of an uploaded file. The
     * value is obtained from the current module's controller configuration.
     *
     * @param ac The current module's application configuration.
     *
     * @return The maximum allowable file size, in bytes.
     */
    protected long getSizeMax(ApplicationConfig ac) {

        String sizeString = ac.getControllerConfig().getMaxFileSize();
        int multiplier = 1;

        if (sizeString.endsWith("K")) {
            multiplier = 1024;
        } else if (sizeString.endsWith("M")) {
            multiplier = 1024 * 1024;
        } else if (sizeString.endsWith("G")) {
            multiplier = 1024 * 1024 * 1024;
        }
        if (multiplier != 1) {
            sizeString = sizeString.substring(0, sizeString.length() - 1);
        }
        
        long size = 0;
        try {
            size = Long.parseLong(sizeString);
        } catch (NumberFormatException nfe) {
            log.warn("Invalid format for maximum file size ('"
                    + ac.getControllerConfig().getMaxFileSize()
                    + "'). Using default.");
            size = DEFAULT_SIZE_MAX;
            multiplier = 1;
        }
                
        return (size * multiplier);
    }


    /**
     * Returns the size threshold which determines whether an uploaded file
     * will be written to disk or cached in memory.
     *
     * @param ac The current module's application configuration.
     *
     * @return The size threshold, in bytes.
     */
    protected int getSizeThreshold(ApplicationConfig ac) {
        return DEFAULT_SIZE_THRESHOLD;
    }


    /**
     * Returns the path to the temporary directory to be used for uploaded
     * files which are written to disk. The directory used is determined from
     * the first of the following to be non-empty.
     * <ol>
     * <li>A temp dir explicitly defined either using the <code>tempDir</code>
     *     servlet init param, or the <code>tempDir</code> attribute of the
     *     &lt;controller&gt; element in the Struts config file.</li>
     * <li>The container-specified temp dir, obtained from the
     *     <code>javax.servlet.context.tempdir</code> servlet context
     *     attribute.</li>
     * <li>The temp dir specified by the <code>java.io.tmpdir</code> system
     *     property.</li>
     * (/ol>
     *
     * @param ac The application config instance for which the path should be
     *           determined.
     *
     * @return The path to the directory to be used to store uploaded files.
     */
    protected String getRepositoryPath(ApplicationConfig ac) { 

        // First, look for an explicitly defined temp dir.
        String tempDir = ac.getControllerConfig().getTempDir();

        // If none, look for a container specified temp dir.
        if (tempDir == null || tempDir.length() == 0) {
            if (servlet != null) {
                ServletContext context = servlet.getServletContext();
                File tempDirFile = (File) context.getAttribute(
                        "javax.servlet.context.tempdir");
                tempDir = tempDirFile.getAbsolutePath();
            }

            // If none, pick up the system temp dir.
            if (tempDir == null || tempDir.length() == 0) {
                tempDir = System.getProperty("java.io.tmpdir");
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("File upload temp dir: " + tempDir);
        }

        return tempDir;
    }


    /**
     * Adds a regular text parameter to the set of text parameters for this
     * request and also to the list of all parameters. Handles the case of
     * multiple values for the same parameter by using an array for the
     * parameter value.
     *
     * @param request The request in which the parameter was specified.
     * @param item    The file item for the parameter to add.
     */
    protected void addTextParameter(HttpServletRequest request, FileItem item) {
        String name = item.getFieldName();
        String value = item.getString();

        if (request instanceof MultipartRequestWrapper) {
            MultipartRequestWrapper wrapper = (MultipartRequestWrapper) request;
            wrapper.setParameter(name, value);
        }

        String[] oldArray = (String[]) elementsText.get(name);
        String[] newArray;

        if (oldArray != null) {
            newArray = new String[oldArray.length + 1];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            newArray[oldArray.length] = value;
        } else {
            newArray = new String[] { value };
        }

        elementsText.put(name, newArray);
        elementsAll.put(name, newArray);
    }


    /**
     * Adds a file parameter to the set of file parameters for this request
     * and also to the list of all parameters.
     *
     * @param request The request in which the parameter was specified.
     * @param item    The file item for the parameter to add.
     */
    protected void addFileParameter(HttpServletRequest request, FileItem item) {
        FormFile formFile = new CommonsFormFile(item);

        elementsFile.put(item.getFieldName(), formFile);
        elementsAll.put(item.getFieldName(), formFile);
    }


    // ---------------------------------------------------------- Inner Classes


    /**
     * This class implements the Struts <code>FormFile</code> interface by
     * wrapping the Commons FileUpload <code>FileItem</code> interface. This
     * implementation is <i>read-only</i>; any attempt to modify an instance
     * of this class will result in an <code>UnsupportedOperationException</code>.
     */
    static class CommonsFormFile implements FormFile {

        /**
         * The <code>FileItem</code> instance wrapped by this object.
         */
        FileItem fileItem;


        /**
         * Constructs an instance of this class which wraps the supplied
         * file item.
         *
         * @param fileItem The Commons file item to be wrapped.
         */
        public CommonsFormFile(FileItem fileItem) {
            this.fileItem = fileItem;
        }


        /**
         * Returns the content type for this file.
         *
         * @return A String representing content type.
         */
        public String getContentType() {
            return fileItem.getContentType();
        }


        /**
         * Sets the content type for this file.
         * <p>
         * NOTE: This method is not supported in this implementation.
         *
         * @param contentType A string representing the content type.
         */
        public void setContentType(String contentType) {
            throw new UnsupportedOperationException(
                    "The setContentType() method is not supported.");
        }


        /**
         * Returns the size, in bytes, of this file.
         *
         * @return The size of the file, in bytes.
         */
        public int getFileSize() {
            return (int)fileItem.getSize();
        }


        /**
         * Sets the size, in bytes, for this file.
         * <p>
         * NOTE: This method is not supported in this implementation.
         *
         * @param filesize The size of the file, in bytes.
         */
        public void setFileSize(int filesize) {
            throw new UnsupportedOperationException(
                    "The setFileSize() method is not supported.");
        }


        /**
         * Returns the (client-side) file name for this file.
         *
         * @return The client-size file name.
         */
        public String getFileName() {
            return getBaseFileName(fileItem.getName());
        }


        /**
         * Sets the (client-side) file name for this file.
         * <p>
         * NOTE: This method is not supported in this implementation.
         *
         * @param fileName The client-side name for the file.
         */
        public void setFileName(String fileName) {
            throw new UnsupportedOperationException(
                    "The setFileName() method is not supported.");
        }


        /**
         * Returns the data for this file as a byte array. Note that this may
         * result in excessive memory usage for large uploads. The use of the
         * {@link #getInputStream() getInputStream} method is encouraged
         * as an alternative.
         *
         * @return An array of bytes representing the data contained in this
         *         form file.
         *
         * @exception FileNotFoundException If some sort of file representation
         *                                  cannot be found for the FormFile
         * @exception IOException If there is some sort of IOException
         */
        public byte[] getFileData() throws FileNotFoundException, IOException {
            return fileItem.get();
        }


        /**
         * Get an InputStream that represents this file.  This is the preferred
         * method of getting file data.
         * @exception FileNotFoundException If some sort of file representation
         *                                  cannot be found for the FormFile
         * @exception IOException If there is some sort of IOException
         */
        public InputStream getInputStream() throws FileNotFoundException, IOException {
            return fileItem.getInputStream();
        }


        /**
         * Destroy all content for this form file.
         * Implementations should remove any temporary
         * files or any temporary file data stored somewhere
         */
        public void destroy() {
            fileItem.delete();
        }


        /**
         * Returns the base file name from the supplied file path. On the surface,
         * this would appear to be a trivial task. Apparently, however, some Linux
         * JDKs do not implement <code>File.getName()</code> correctly for Windows
         * paths, so we attempt to take care of that here.
         *
         * @param filePath The full path to the file.
         *
         * @return The base file name, from the end of the path.
         */
        protected String getBaseFileName(String filePath) {

            // First, ask the JDK for the base file name.
            String fileName = new File(filePath).getName();

            // Now check for a Windows file name parsed incorrectly.
            int colonIndex = fileName.indexOf(":");
            if (colonIndex == -1) {
                // Check for a Windows SMB file path.
                colonIndex = fileName.indexOf("\\\\");
            }
            int backslashIndex = fileName.lastIndexOf("\\");

            if (colonIndex > -1 && backslashIndex > -1) {
                // Consider this filename to be a full Windows path, and parse it
                // accordingly to retrieve just the base file name.
                fileName = fileName.substring(backslashIndex + 1);
            }

            return fileName;
        }
    }
}

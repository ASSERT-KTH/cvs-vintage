package org.apache.struts.upload;

import java.util.Vector;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * The MultipartIterator class is responsible for reading the
 * input data of a multipart request and splitting it up into
 * input elements, wrapped inside of a
 * {@link org.apache.struts.upload.MultipartElement MultipartElement}
 * for easy definition.  To use this class, create a new instance
 * of MultipartIterator passing it a HttpServletRequest in the
 * constructor.  Then use the {@link #getNextElement() getNextElement}
 * method until it returns null, then you're finished.  Example: <br>
 * <pre>
 *      MultipartIterator iterator = new MultipartIterator(request);
 *      MultipartElement element;
 * 
 *      while ((element = iterator.getNextElement()) != null) {
 *           //do something with element
 *      }
 * </pre>
 *
 * @see org.apache.struts.upload.MultipartElement
 * @author Mike Schachter
 */
public class MultipartIterator {
    
    /**
     * The request instance for this class
     */
    protected HttpServletRequest request;
    
    /**
     * The input stream instance for this class
     */
    protected ServletInputStream inputStream;
    
    /**
     * The boundary for this multipart request
     */
    protected String boundary;
    
    /**
     * Whether or not the input stream is finished
     */
    protected boolean contentRead = false;
    
    /**
     * The amount of data read from a request at a time.
     * This also represents the maximum size in bytes of
     * a line read from the request
     * Defaults to 4 * 1024 (4 KB)
     */
    protected int bufferSize = 4 * 1024;

    
    public MultipartIterator(HttpServletRequest request) throws ServletException{
        this.request = request;
        
        parseRequest();
    }
    
    /**
     * Retrieves the next element in the iterator if one exists.
     *
     * @return a {@link org.apache.struts.upload.MultipartElement MultipartElement}
     *         representing the next element in the request data
     *
     */
    public MultipartElement getNextElement() {
        
        //retrieve the "Content-Disposition" header
        //and parse
        String disposition = readLine();
        
        if ((disposition != null) && (disposition.startsWith("Content-Disposition"))) {
            String name = parseDispositionName(disposition);
            String filename = parseDispositionFilename(disposition);
                       
            String contentType = null;

            byte[] data = null;
            
            if (filename != null) {
                filename = new File(filename).getName();
                
                //check for windows filenames,
                //from linux jdk's the entire filepath
                //isn't parsed correctly from File.getName()
                int colonIndex = filename.indexOf(":");
                int slashIndex = filename.lastIndexOf("\\");
                
                if ((colonIndex > -1) && (slashIndex > -1)) {
                    //then consider this filename to be a full
                    //windows filepath, and parse it accordingly
                    //to retrieve just the file name
                    filename = filename.substring(slashIndex+1, filename.length());
                }
                
                
                
                //get the content type
                contentType = readLine();
                contentType = parseContentType(contentType);
            }
            
            //read data into String form, then convert to bytes
            //for both normal text and file
            String textData = "";
            String line;
            
            //ignore next line (whitespace)
            readLine();
            
            //parse for text data
            line = readLine();
         
            while ((line != null) && (!line.startsWith(boundary))) {
                textData += line;
                line = readLine();
            }
            
            //remove the "\r\n" if it's there
            if (textData.endsWith("\r\n")) {
                textData = textData.substring(0, textData.length()-2);
            }
            
            //remove the "\n" if it's there
            if (textData.endsWith("\n")) {
                textData = textData.substring(0, textData.length()-1);
            }
            
            //convert data into byte form for MultipartElement
            try {
                data = textData.getBytes("ISO-8859-1");
            }
            catch (UnsupportedEncodingException uee) {
                data = textData.getBytes();
            }
                        
            MultipartElement element = new MultipartElement(name,
                                                            filename,
                                                            contentType,
                                                            data);
            
            return element;
        }       
        return null;       
    }
    
    /**
     * Set the maximum amount of bytes read from a line at one time
     *
     * @see javax.servlet.ServletInputStream#readLine(byte[], int, int)
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    /**
     * Get the maximum amount of bytes read from a line at one time
     *
     * @see javax.servlet.ServletInputStream#readLine(byte[], int, int)
     */
    public int getBufferSize() {
        return bufferSize;
    }
    
    /**
     * Handles retrieving the boundary and setting the input stream
     */
    protected void parseRequest() throws ServletException {
        
        //set boundary
        boundary = parseBoundary(request.getContentType());
        
        try {
            //set the input stream
            inputStream = request.getInputStream();
        }
        catch (IOException ioe) {
            throw new ServletException("MultipartIterator.parseRequest(): " +
                                       "IOException while trying to obtain " +
                                       "ServletInputStream");
        }
        
        if ((boundary == null) || (boundary.length() < 1)) {
            //try retrieving the header through more "normal" means
            boundary = parseBoundary(request.getHeader("Content-type"));
        }
        
        if ((boundary == null) || (boundary.length() < 1)) {
            throw new ServletException("MultipartIterator: cannot retrieve boundary " +
                                       "for multipart request");
        }
        
        //read first line
        if (!readLine().startsWith(boundary)) {
            throw new ServletException("MultipartIterator: invalid multipart request " +
                                       "data");
        }
    }
      
    /**
     * Parses a content-type String for the boundary.  Appends a 
     * "--" to the beginning of the boundary, because thats the
     * real boundary as opposed to the shortened one in the
     * content type.
     */
    public static String parseBoundary(String contentType) {
        if (contentType.lastIndexOf("boundary=") != -1) {
            String _boundary = "--" + 
                               contentType.substring(contentType.lastIndexOf("boundary=")+9);
            if (_boundary.endsWith("\n")) {
                //strip it off
                return _boundary.substring(0, _boundary.length()-1);
            }
            return _boundary; 
        }
        return null;      
    }
    
    /**
     * Parses the "Content-Type" line of a multipart form for a content type
     *
     * @param contentTypeString A String reprsenting the Content-Type line, 
     *        with a trailing "\n"
     * @return The content type specified, or <code>null</code> if one can't be
     *         found.
     */
    public static String parseContentType(String contentTypeString) {
        int nameIndex = contentTypeString.indexOf("Content-Type: ");
        
        if (nameIndex != -1) {
            int endLineIndex = contentTypeString.indexOf("\n");
            if (endLineIndex != -1) {
                return contentTypeString.substring(nameIndex+14, endLineIndex);
            }
        }
        return null;
    }
            
        
        
        
    
    /**
     * Retrieves the "name" attribute from a content disposition line
     * 
     * @param dispositionString The entire "Content-disposition" string
     * @return <code>null</code> if no name could be found, otherwise,
     *         returns the name
     * @see #parseForAttribute(String, String)
     */
    public static String parseDispositionName(String dispositionString) {
        return parseForAttribute("name", dispositionString);
    }
    
    /** 
     * Retrieves the "filename" attribute from a content disposition line
     *
     * @param dispositionString The entire "Content-disposition" string
     * @return <code>null</code> if no filename could be found, otherwise,
     *         returns the filename
     * @see #parseForAttribute(String, String)
     */
    public static String parseDispositionFilename(String dispositionString) {
        return parseForAttribute("filename", dispositionString);
    }
        
    
    /**
     * Parses a string looking for a attribute-value pair, and returns the value.
     * For example:
     * <pre>
     *      String parseString = "Content-Disposition: filename=\"bob\" name=\"jack\"";
     *      MultipartIterator.parseForAttribute(parseString, "name");
     * </pre>
     * That will return "bob".
     * 
     * @param attribute The name of the attribute you're trying to get
     * @param parseString The string to retrieve the value from
     * @return The value of the attribute, or <code>null</code> if none could be found
     */
    public static String parseForAttribute(String attribute, String parseString) {
        int nameIndex = parseString.indexOf(attribute + "=\"");
        if (nameIndex != -1) {
            int endQuoteIndex = parseString.indexOf("\"", nameIndex+attribute.length()+3);
            
            if (endQuoteIndex != -1) {
                return parseString.substring(nameIndex+attribute.length()+2, endQuoteIndex);
            }
        }        
        return null;
    }
    
    /**
     * Reads the input stream until it reaches a new line
     */
    protected String readLine() {
       
        byte[] bufferByte = new byte[bufferSize];
        int bytesRead;
        
        try {
            bytesRead = inputStream.readLine(bufferByte,
                                             0,
                                             bufferSize);
        }
        catch (IOException ioe) {
            return null;
        }
        if (bytesRead == -1) {
            contentRead = true;
            return null;
        }
        String retString = null;
        
        try {
            retString = new String(bufferByte, 0, bytesRead, "ISO-8859-1");
        }
        catch (UnsupportedEncodingException uee) {
            retString = new String(bufferByte);
        }
        return retString;
    }
}
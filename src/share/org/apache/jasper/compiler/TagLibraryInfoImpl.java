/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagLibraryInfoImpl.java,v 1.2 1999/10/20 11:22:55 akv Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/20 11:22:55 $
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

package org.apache.jasper.compiler;

import java.net.URL;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.Hashtable;

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;

import org.w3c.dom.*;
import org.xml.sax.*;
import com.sun.xml.tree.*;
import com.sun.xml.parser.*;

import org.apache.jasper.JspEngineContext;
import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.runtime.JspLoader;

/**
 * Implementation of the TagLibraryInfo class from the JSP spec. 
 *
 * @author Anil K. Vijendran
 */
public class TagLibraryInfoImpl extends TagLibraryInfo {
    static private final String TLD = "META-INF/taglib.tld";

    XmlDocument tld;

    Hashtable jarEntries;
    Hashtable tagCaches = new Hashtable();
    
    JspEngineContext ctxt;

    

    private final void print(String name, String value, PrintWriter w) {
        if (value != null) {
            w.print(name+" = {\n\t");
            w.print(value);
            w.print("\n}\n");
        }
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        print("tlibversion", tlibversion, out);
        print("jspversion", jspversion, out);
        print("shortname", shortname, out);
        print("urn", urn, out);
        print("info", info, out);

        for(int i = 0; i < tags.length; i++)
            out.println(tags[i].toString());
        
        return sw.toString();
    }
    
    TagLibraryInfoImpl(JspEngineContext ctxt, String prefix, String uri) 
        throws IOException, JasperException
    {
        super(prefix, uri);

        ZipInputStream zin;
        InputStream in = null;
        URL url = null;
        boolean relativeURL = false;
        
        if (!uri.startsWith("/")) {
            url = new URL(uri);
            in = url.openStream();
        } else {
            relativeURL = true;
            in = ctxt.getServletContext().getResourceAsStream(uri);
        }
        
        zin = new ZipInputStream(in);
        this.jarEntries = new Hashtable();
        this.ctxt = ctxt;

        // First copy this file into our work directory! 
        {
            File jspFile = new File(ctxt.getJspFile());
            String jarFileName = ctxt.getOutputDir()+File.separatorChar+
                jspFile.getParent().toString();
            File jspDir = new File(jarFileName);
            jspDir.mkdirs();

            if (relativeURL)
                jarFileName = jarFileName+File.separatorChar+new File(uri).getName();
            else                    
                jarFileName = jarFileName+File.separatorChar+
                    new File(url.getFile()).getName();

            Constants.message("jsp.message.copyinguri", 
                              new Object[] { uri, jarFileName },
                              Constants.MED_VERBOSITY);

            if (relativeURL)
                copy(ctxt.getServletContext().getResourceAsStream(uri),
                     jarFileName);
            else
                copy(url.openStream(), jarFileName);
            
            ctxt.getClassLoader().addJar(jarFileName);
        }
        

        boolean tldFound = false;
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals(TLD)) {
                /*******
                 * This hack is necessary because XML reads until the end 
                 * of an inputstream -- does not use available()
                 * -- and closes the inputstream when it can't
                 * read no more.
                 */

                // BEGIN HACK
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int b;
                while (zin.available() != 0) {
                    b = zin.read();
                    if (b == -1)
                        break;
                    baos.write(b);
                }
                    
                baos.close();
                ByteArrayInputStream bais 
                    = new ByteArrayInputStream(baos.toByteArray());
                // END HACK
                tldFound = true;
                parseTLD(bais);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int b;
                while (zin.available() != 0) {
                    b = zin.read();
                    if (b == -1)
                        break;
                    baos.write(b);
                }
                baos.close();
                jarEntries.put(entry.getName(), baos.toByteArray());
            }
            zin.closeEntry();
        }
        if (!tldFound)
            throw new JasperException(Constants.getString("jsp.error.tld_not_found",
                                                          new Object[] {
                                                              TLD
                                                          }
                                                          ));
    }
    
        
    private void parseTLD(InputStream in) 
        throws JasperException
    {
        XmlDocumentBuilder builder = new XmlDocumentBuilder();

                
        com.sun.xml.parser.ValidatingParser 
            parser = new com.sun.xml.parser.ValidatingParser();

        /***
         * These lines make sure that we have an internal catalog entry for 
         * the taglib.dtd file; this is so that jasper can run standalone 
         * without running out to the net to pick up the taglib.dtd file.
         */
        Resolver resolver = new Resolver();
        URL dtdURL = this.getClass().getResource(Constants.TAGLIB_DTD_RESOURCE);
        
        resolver.registerCatalogEntry(Constants.TAGLIB_DTD_PUBLIC_ID, 
                                      dtdURL.toString());
        
        try {
            parser.setEntityResolver(resolver);
            parser.setDocumentHandler(builder);
            builder.setParser(parser);
            builder.setDisableNamespaces(false);
            parser.parse(new InputSource(in));
        } catch (SAXException sx) {
            throw new JasperException(Constants.getString("jsp.error.parse.error.in.TLD",
                                                          new Object[] {
                                                              sx.getMessage()
                                                          }
                                                          ));
        } catch (IOException io) {
            throw new JasperException(Constants.getString("jsp.error.unable.to.open.TLD",
                                                          new Object[] {
                                                              io.getMessage()
                                                          }
                                                          ));
        }
        
        tld = builder.getDocument();
        Vector tagVector = new Vector();
        NodeList list = tld.getElementsByTagName("taglib");

        if (list.getLength() != 1)
            throw new JasperException(Constants.getString("jsp.error.more.than.one.taglib"));

        Element elem = (Element) list.item(0);
        list = elem.getChildNodes();

        for(int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String tname = e.getTagName();
            if (tname.equals("tlibversion")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    this.tlibversion = t.getData();
            } else if (tname.equals("jspversion")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    this.jspversion = t.getData();
            } else if (tname.equals("shortname")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    this.shortname = t.getData();
            } else if (tname.equals("urn")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    this.urn = t.getData();
            } else if (tname.equals("info")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    this.info = t.getData();
            } else if (tname.equals("tag"))
                tagVector.addElement(createTagInfo(e));
            else 
                Constants.message("jsp.warning.unknown.element.in.TLD", 
                                  new Object[] {
                                      e.getTagName()
                                  },
                                  Constants.WARNING
                                  );
        }

        this.tags = new TagInfo[tagVector.size()];
        tagVector.copyInto (this.tags);
    }

    private TagInfo createTagInfo(Element elem) throws JasperException {
        String name = null, tagclass = null, teiclass = null;
        String bodycontent = null, info = null;
        
        Vector attributeVector = new Vector();
        NodeList list = elem.getChildNodes();
        for(int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String tname = e.getTagName();
            if (tname.equals("name")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    name = t.getData();
            } else if (tname.equals("tagclass")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tagclass = t.getData();
            } else if (tname.equals("teiclass")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    teiclass = t.getData();
            } else if (tname.equals("bodycontent")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    bodycontent = t.getData();
            } else if (tname.equals("info")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    info = t.getData();
            } else if (tname.equals("attribute"))
                attributeVector.addElement(createAttribute(e));
            else 
                Constants.message("jsp.warning.unknown.element.in.tag", 
                                  new Object[] {
                                      e.getTagName()
                                  },
                                  Constants.WARNING
                                  );
        }
	TagAttributeInfo[] tagAttributeInfo 
            = new TagAttributeInfo[attributeVector.size()];
	attributeVector.copyInto (tagAttributeInfo);

        TagExtraInfo tei = null;

        if (teiclass != null && !teiclass.equals(""))
            try {
                Class teiClass = ctxt.getClassLoader().loadClass(teiclass);

                tei = (TagExtraInfo) teiClass.newInstance();
            } catch (ClassNotFoundException cex) {
                Constants.message("jsp.warning.teiclass.is.null",
                                  new Object[] {
                                      teiclass, cex.getMessage()
                                  },
                                  Constants.WARNING
                                  );
            } catch (IllegalAccessException iae) {
                Constants.message("jsp.warning.teiclass.is.null",
                                  new Object[] {
                                      teiclass, iae.getMessage()
                                  },
                                  Constants.WARNING
                                  );
            } catch (InstantiationException ie) {
                Constants.message("jsp.warning.teiclass.is.null",
                                  new Object[] {
                                      teiclass, ie.getMessage()
                                  },
                                  Constants.WARNING
                                  );
            }
        
        TagInfo taginfo = new TagInfo(name, tagclass, bodycontent,
                                      info, this, 
                                      tei,
                                      tagAttributeInfo);
        return taginfo;
    }

    TagAttributeInfo createAttribute(Element elem) {
        String name = null;
        boolean required = false, rtexprvalue = false, reqTime = false;
        String type = null;
        
        NodeList list = elem.getChildNodes();
        for(int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String tname = e.getTagName();
            if (tname.equals("name"))  {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    name = t.getData();
            } else if (tname.equals("required"))  {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    required = Boolean.valueOf(t.getData()).booleanValue();
            } else if (tname.equals("rtexprvalue")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    rtexprvalue = Boolean.valueOf(t.getData()).booleanValue();
            } else if (tname.equals("reqtime")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    reqTime = Boolean.valueOf(t.getData()).booleanValue();
            } else if (tname.equals("type")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    type = t.getData();
            } else 
                Constants.message("jsp.warning.unknown.element.in.attribute", 
                                  new Object[] {
                                      e.getTagName()
                                  },
                                  Constants.WARNING
                                  );
        }
        
        return new TagAttributeInfo(name, required, rtexprvalue, type, 
                                    reqTime);
    }

    static void copy(InputStream in, String fileName) 
        throws IOException, FileNotFoundException 
    {
        byte[] buf = new byte[1024];

        FileOutputStream out = new FileOutputStream(fileName);
        int nRead;
        while ((nRead = in.read(buf, 0, buf.length)) != -1)
            out.write(buf, 0, nRead);
    }

    TagCache getTagCache(String shortTagName) {
        return (TagCache) tagCaches.get(shortTagName);
    }

    void putTagCache(String shortTagName, TagCache tc) {
        tagCaches.put(shortTagName, tc);
    }
}

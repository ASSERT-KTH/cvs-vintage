// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import java.io.*;
//import common.*;

import org.columba.core.xml.XmlElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


public class XmlIO extends DefaultHandler {
  // List of sub-elements
  Vector Elements;
  // Top level element (Used to hold everything else)
  XmlElement rootElement;
  // The current element you are working on
  XmlElement currentElement;

  // For writing out the data
  // Indent for each level
  int writeIndent = 2;
  // Maximum data to put on a "one liner"
  int maxOneLineData = 20;

  // The SAX 2 parser...
  private XMLReader xr;

  // Buffer for collecting data from
  // the "characters" SAX event.
  private CharArrayWriter contents = new CharArrayWriter();

  // Default constructor
  public XmlIO(){
  }

  // setup and load constructor
  public XmlIO(String FilePath){
    currentElement = null;
    load(FilePath);
  }

  // Load a file. This is what starts things off.
  public boolean load(String in){
    Elements = new Vector();
    rootElement = new XmlElement("__CULUMBA_XML_TREE_TOP__");
    currentElement = rootElement;

    try{
      // Use an instance of ourselves as the SAX event handler
      DefaultHandler handler = this;
      // Create the XML reader...
      //      xr = XMLReaderFactory.createXMLReader();
       SAXParserFactory factory = SAXParserFactory.newInstance();
      // Set the ContentHandler...
      //      xr.setContentHandler( this );

       SAXParser saxParser = factory.newSAXParser();
      // Parse the file...
      System.out.println("About to parse XML document." );
      //xr.parse( in );
      System.out.println(in);
      System.out.println(saxParser);

      saxParser.parse( in, this);
      System.out.println("XML document parsing complete.");
    }
    catch(javax.xml.parsers.ParserConfigurationException ex){
      System.out.println("XML config error while attempting to read XML file");
      System.out.println(ex.toString());
      ex.printStackTrace();
      return(false);
    }
    catch(org.xml.sax.SAXException ex){
      // Error
      System.out.println("XML parse error while attempting to read XML file");
      System.out.println(ex.toString());
      ex.printStackTrace();
      return(false);
    }
    catch(java.io.IOException ex){
      System.out.println("File read error while attempting to read XML file");
      System.out.println(ex.toString());
      ex.printStackTrace();
      return(false);
    }

    return(true);
  }

  // Implement the content hander methods that
  // will delegate SAX events to the tag tracker network.

  public void startElement( String namespaceURI,
                            String localName,
                            String qName,
                            Attributes attrs ) throws SAXException {

    // Resetting contents buffer.
    // Assuming that tags either tag content or children, not both.
    // This is usually the case with XML that is representing
    // data strucutures in a programming language independant way.
    // This assumption is not typically valid where XML is being
    // used in the classical text mark up style where tagging
    // is used to style content and several styles may overlap
    // at once.
    try{
      contents.reset();
      String Name = localName; // element name
      if (Name.equals("")) Name = qName; // namespaceAware = false

      XmlElement P = currentElement;

      currentElement = currentElement.addSubElement(Name);
      currentElement.setParent(P);

      if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); i++) {
          String aName = attrs.getLocalName(i); // Attr name
          if (aName.equals("")) aName = attrs.getQName(i);

          currentElement.addAttribute(aName,attrs.getValue(i));
        }
      }
    }catch(java.lang.NullPointerException ex){
      System.out.println("Null!!!");
      System.out.println(ex.toString());
      ex.printStackTrace();
    }
  }

  public void endElement( String namespaceURI,
                          String localName,
                          String qName ) throws SAXException {

    currentElement.setData(contents.toString().trim());
    contents.reset();

    currentElement = currentElement.getParent();
  }


  public void characters( char[] ch, int start, int length )
    throws SAXException {
    // accumulate the contents into a buffer.
    contents.write( ch, start, length );
  }

  public XmlElement getRoot(){
    return(rootElement);
  }


  //
  // Writer interface
  //
  public void write(OutputStream out) throws IOException {
    PrintWriter PW = new PrintWriter(out);
    PW.println("<?xml version=\"1.0\"?>");
    if(rootElement.SubElements.size() > 0){
      for(int i = 0;i<rootElement.SubElements.size();i++){
        _writeSubNode(PW,(XmlElement)rootElement.SubElements.get(i),0);
      }
    }
    PW.flush();
  }

  private void _writeSubNode(PrintWriter out,
                             XmlElement Element,
                             int indent) throws IOException {
    _writeSpace(out,indent);
    out.print("<"+Element.getName());
    for (Enumeration e = Element.getAttributeNames();e.hasMoreElements();) {
      String K = (String)e.nextElement();
      out.print(K+"=\""+Element.getAttribute(K)+"\" ");
    }
    out.print(">");
    String Data = Element.getData();
    if(Data != null && ! Data.equals("")){
      if(Data.length() > maxOneLineData){
        out.println("");
        _writeSpace(out,indent+writeIndent);
      }
      out.print(Data);
    }
    Vector SubElements = Element.getElements();

    if(SubElements.size() > 0){
      out.println("");
      for(int i = 0;i<SubElements.size();i++){
        _writeSubNode(out,(XmlElement)SubElements.get(i),indent+writeIndent);
      }
      _writeSpace(out,indent);
    }
    if(Data.length() > maxOneLineData){
      out.println("");
      _writeSpace(out,indent);
    }
    out.println("</"+Element.getName()+">");
  }

  private void _writeSpace(PrintWriter out,int numSpaces) throws IOException{
    for(int i=0;i<numSpaces;i++) out.print(" ");
  }


}// End class XmlIO

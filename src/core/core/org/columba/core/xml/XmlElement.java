// this is -*- java -*- code
///////////////////////////////////////////////////////////////////////////
// FILE:          XmlElement.java
// SUMMARY:
// USAGE:
//
// AUTHOR:        Tony Parent
// ORIG-DATE:     8-Oct-02 at 12:38:10
// LAST-MOD:     18-Oct-02 at 12:28:56 by Tony Parent
// DESCRIPTION:
// DESCRIP-END.
//
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
//
// $Log: file-hdr.java,v $
/////////////////////////////////////////////////////////////////////////////

package org.columba.core.xml;
/////////////////////////////////////////////////////////////////////////////
//                          IMPORT STATEMENTS                              //
/////////////////////////////////////////////////////////////////////////////

import java.util.*;
import java.io.*;
/////////////////////////////////////////////////////////////////////////////
//                                 CODE                                    //
/////////////////////////////////////////////////////////////////////////////

/**
 * The XmlElement is a generic containment class for elements within an XML
 * file.
 *
 * @author       Tony Parent
 */
public class XmlElement {
  String Name;
  String Data;
  Hashtable Attributes;
  Vector SubElements;
  XmlElement Parent;

  /**
   * **FIXME** This function needs documentation
   *
   * Constructor
   *
   */
  public XmlElement(){
    SubElements = new Vector();
  }
  /**
   * **FIXME** This function needs documentation
   *
   * Constructor
   * @param String Name
   *
   */
  public XmlElement(String Name){
    this.Name = Name;
    this.Attributes = new Hashtable(1000);
    SubElements = new Vector();
  }
  /**
   * **FIXME** This function needs documentation
   *
   * Constructor
   * @param String Name
   * @param Hashtable Attributes
   *
   */
  public XmlElement(String Name,Hashtable Attributes){
    this.Name       = Name;
    this.Attributes = Attributes;
    SubElements = new Vector();
  }
  /**
   * **FIXME** This function needs documentation
   *
   * Constructor
   * @param Name String
   * @param Data String
   *
   */
  public XmlElement(String Name,String Data){
    this.Name   = Name;
    this.Data   = Data;
    SubElements = new Vector();
  }
  /**
   * **FIXME** This function needs documentation
   *
   * @return  Object
   * @param String Name
   * @param  String Value
   *
   */
  public Object addAttribute(String Name, String Value){
    return(Attributes.put(Name,Value));
  }
  /**
   * **FIXME** This function needs documentation
   *
   * @return  String
   * @param String Name
   *
   */
  public String getAttribute(String Name){
    return((String)Attributes.get(Name));
  }
  /**
   * **FIXME** This function needs documentation
   *
   * @return  String
   * @param String Name
   *
   */
  public Hashtable getAttributes(){
    return(Attributes);
  }
  /**
   * **FIXME** This function needs documentation
   *
   *
   * @param Attrs Hashtable to use as the attributes
   *
   */
  public void setAttributes(Hashtable Attrs){
    Attributes = Attrs;
  }
  /**
   * **FIXME** This function needs documentation
   *
   * @return  Enumeration
   *
   */
  public Enumeration getAttributeNames(){
    return(Attributes.keys());
  }
  /**
   * **FIXME** This function needs documentation
   *
   * @return  boolean
   * @param XmlElement E
   *
   */
  public boolean addElement(XmlElement E){
    return(SubElements.add(E));
  }

  public XmlElement removeElement(XmlElement E){
    XmlElement child = null;
    for(int i=0;i<SubElements.size();i++){
      child = (XmlElement)SubElements.get(i);
      // FIXME -- This will most likely not work.
      //          You want the element removed if the contents are the same
      //          Not just if the element reference is the same.
      if(child == E){
        SubElements.remove(i);
      }
    }
    return(child);
  }

  /**
   * **FIXME** This function needs documentation
   *
   * @return  Vector
   *
   */
  public Vector getElements(){
    return(SubElements);
  }

  /**
   * **FIXME** This function needs documentation
   *
   * @return  XmlElement
   * @param String Path
   *
   */
  public XmlElement getElement(String Path){
    int i = Path.indexOf('/');
    String topName,subName;
    if(i == 0){
      Path = Path.substring(1);
      i = Path.indexOf('/');
    }
    if(i > 0){
      topName = Path.substring(0,i);
      subName = Path.substring(i+1);
    }else{
      topName = Path;
      subName = null;
    }
    int j;
    for(j=0;j<SubElements.size();j++){
      if(((XmlElement)SubElements.get(j)).getName().equals(topName)){
        if(subName != null){
          return(((XmlElement)SubElements.get(j)).getElement(subName));
        }else{
          return((XmlElement)SubElements.get(j));
        }
      }
    }
    return(null);
  }

  /**
   * Adds a sub element to this one
   *
   * @return  XmlElement
   * @param Name The name of the sub element to add
   *
   */
  public XmlElement addSubElement(String Name){
    XmlElement E = new XmlElement(Name);
    E.setParent(this);
    SubElements.add(E);
    return(E);
  }

  /**
   * Adds a sub element to this one
   *
   * @return  XmlElement
   * @param   element The XmlElement to add
   *
   */
  public XmlElement addSubElement(XmlElement E){
    E.setParent(this);
    SubElements.add(E);
    return(E);
  }

  /**
   * Adds a sub element to this one
   *
   * @return  XmlElement
   * @param Name The name of the sub element to add
   * @param Data String Data for this element
   */
  public XmlElement addSubElement(String Name,String Data){
    XmlElement E = new XmlElement(Name);
    E.setData(Data);
    SubElements.add(E);
    return(E);
  }

  /**
   * Sets the parent element
   *
   * @param Parent The XmlElement that contains this one
   *
   */
  public void setParent(XmlElement Parent){
    this.Parent = Parent;
  }
  /**
   * Gives the XmlElement containing the current element
   *
   * @return  XmlElement
   *
   */
  public XmlElement getParent(){
    return(Parent);
  }
  /**
   * Sets the data for this element
   *
   * @param D The String representation of the data
   *
   */
  public void setData(String D){
    Data = D;
  }
  /**
   * Returns the data associated with the current Xml element
   *
   * @return  String
   *
   */
  public String getData(){
    return(Data);
  }

  /**
   * Returns the name of the current Xml element
   *
   * @return  String
   *
   */
  public String getName(){
    return(Name);
  }

  /**
   * **FIXME** This function needs documentation
   *
   * @param out OutputStream to print the data to
   *
   */
  public void write(OutputStream out)
    throws IOException {
    PrintWriter PW = new PrintWriter(out);
    PW.println("<?xml version=\"1.0\"?>");
    if(SubElements.size() > 0){
      for(int i = 0;i<SubElements.size();i++){
        ((XmlElement)SubElements.get(i))._writeSubNode(PW,4);
      }
    }
    PW.flush();
  }

  /**
   * Prints sub nodes to the given data stream
   *
   * @param out    PrintWriter to use for printing
   * @param indent Number of spaces to indent things
   *
   */
  private void _writeSubNode(PrintWriter out,int indent)
    throws IOException {
    _writeSpace(out,indent);
    out.print("<"+Name);
    for (Enumeration e = Attributes.keys() ; e.hasMoreElements() ;) {
      String K = (String)e.nextElement();
      out.print(K+"=\""+Attributes.get(K)+"\" ");
    }
    out.print(">");

    if(Data != null && ! Data.equals("")){
      if(Data.length() > 20){
        out.println("");
        _writeSpace(out,indent+2);
      }
      out.print(Data);
    }
    if(SubElements.size() > 0){
      out.println("");
      for(int i = 0;i<SubElements.size();i++){
        ((XmlElement)SubElements.get(i))._writeSubNode(out,indent+4);
      }
      _writeSpace(out,indent);
    }
    out.println("</"+Name+">");

  }
  /**
   * Prints out a given number of spaces
   *
   * @param out       PrintWriter to use for printing
   * @param numSpaces Number of spaces to print
   *
   */
  private void _writeSpace(PrintWriter out,int numSpaces)
    throws IOException{

    for(int i=0;i<numSpaces;i++) out.print(" ");
  }
} // END public class XmlElement

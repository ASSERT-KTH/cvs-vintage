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

import java.util.*;
import java.io.*;

public class XmlElement {
  String Name;
  String Data;
  Hashtable Attributes;
  Vector SubElements;
  XmlElement Parent;

  public XmlElement(){
    SubElements = new Vector();
  }
  public XmlElement(String Name){
    this.Name = Name;
    this.Attributes = new Hashtable(1000);
    SubElements = new Vector();
  }
  public XmlElement(String Name,Hashtable Attributes){
    this.Name       = Name;
    this.Attributes = Attributes;
    SubElements = new Vector();
  }
  public Object addAttribute(String Name, String Value){
    return(Attributes.put(Name,Value));
  }
  public String getAttribute(String Name){
    return((String)Attributes.get(Name));
  }
  public Enumeration getAttributeNames(){
    return(Attributes.keys());
  }
  public boolean addElement(XmlElement E){
    return(SubElements.add(E));
  }
  public Vector getElements(){
    return(SubElements);
  }

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

  public XmlElement addSubElement(String Name){
    XmlElement E = new XmlElement(Name);
    SubElements.add(E);
    return(E);
  }

  public void setParent(XmlElement Parent){
    this.Parent = Parent;
  }
  public XmlElement getParent(){
    return(Parent);
  }
  public void setData(String D){
    Data = D;
  }
  public String getData(){
    return(Data);
  }

  public String getName(){
    return(Name);
  }

  public void write(OutputStream out)
    throws IOException {
    System.out.println("HELLO");
    PrintWriter PW = new PrintWriter(out);
    PW.println("<?xml version=\"1.0\"?>");
    if(SubElements.size() > 0){
      for(int i = 0;i<SubElements.size();i++){
        ((XmlElement)SubElements.get(i))._writeSubNode(PW,4);
      }
    }
    PW.flush();
  }

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
  private void _writeSpace(PrintWriter out,int numSpaces)
    throws IOException{

    for(int i=0;i<numSpaces;i++) out.print(" ");
  }
} // END public class XmlElement

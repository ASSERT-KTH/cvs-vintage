package org.jboss.ejb.jrmp12.interfaces.proxy;



import java.io.*;



public interface Replaceable extends Serializable

{

  public Object writeReplace() throws ObjectStreamException;

}


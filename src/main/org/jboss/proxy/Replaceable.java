package org.jboss.proxy;



import java.io.*;



public interface Replaceable extends Serializable

{

  public Object writeReplace() throws ObjectStreamException;

}


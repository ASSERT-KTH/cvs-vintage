package org.jboss.proxy.compiler;



import java.io.*;



public interface Replaceable extends Serializable

{

  public Object writeReplace() throws ObjectStreamException;

}


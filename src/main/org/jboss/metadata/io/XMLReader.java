package org.jboss.metadata.io;

import org.jboss.metadata.ServerMetaData;
import java.io.*;
import java.util.*;

public interface XMLReader {
    public ServerMetaData readXML(Reader input) throws IOException;
    public String getFileName();
}
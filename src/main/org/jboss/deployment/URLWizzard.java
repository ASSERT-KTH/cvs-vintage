/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.URL;
import java.net.URLConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 *	Encapsulates URL bases copy and jar file operations.
 * Very scratchy! Any improvements are welcome!
 *      
 *	@author Daniel Schulze <daniel.schulze@telkel.com>
 *	@version $Revision: 1.1 $
 */
public class URLWizzard
{
    
   // Static --------------------------------------------------------
   public static void main (String[] _args) throws Exception
   {
      downloadAndInflate (new URL (_args[0]), new URL (_args[1]));
   }
   
   
   
   
   /** copies the source to the destination url. As destination
       are currently only file:/... urls supported */
   public static void download (URL _src, URL _dest) throws IOException
   {
      InputStream in;
      OutputStream out;
/*
      URLConnection urlCon = _src.openConnection ();
      urlCon.setDoInput (true);
      in = urlCon.getInputStream ();
*/
      in = _src.openStream ();

      
      boolean jar = false;
      String jarPath = "";
      String filePath;
      String fileName;
      String s = _dest.toString ();
      
      if (_dest.getProtocol ().equals ("jar"))
      {
      	// get the path in the jar
         int pos = s.indexOf ("!");
      	jarPath = s.substring (pos + 1);
      	s = s.substring (0, pos);
      }
      
      // get the FileName
      int pos = s.lastIndexOf ("/");
   	fileName = s.substring (pos + 1);
   	s = s.substring (0, pos);

      // get the FilePath
      pos = Math.max (0, s.lastIndexOf (":"));
   	filePath = s.substring (pos + 1);
      filePath = filePath.replace ('/', File.separatorChar);
      
      if (jar)
      {
         // open jarFile
         throw new IOException ("write into a jar file NOT yet implemented!");         	
      }
      else
      {
      	File dir = new File (filePath);
      	if (!dir.exists ())
      	   dir.mkdirs ();
      	
      	out = new FileOutputStream (filePath + File.separator + fileName); 
      }
      
      write (in, out);

      out.close ();
      in.close ();
   }

   /** inflates the given zip file into the given directory */
   public static void downloadAndInflate (URL _src, URL _dest) throws IOException
   {
      InputStream in;
      OutputStream out;

      in = _src.openStream ();

      boolean jar = false;
      String jarPath = "";
      String filePath;
      String fileName;
      String s = _dest.toString ();

      if (!_dest.getProtocol ().equals ("file"))
         throw new IOException ("only file:/ is as destination allowed!");         	
      	
   	File base = new File (_dest.getFile ());
   	if (base.exists ())
   	   deleteTree (_dest);
  	
  	   base.mkdirs ();

      ZipInputStream zin = new ZipInputStream (in);
      ZipEntry entry;
      while ((entry = zin.getNextEntry ()) != null)
      {
         String name = entry.getName ();
         if (!entry.isDirectory ()) // there are not all directories listed (?!)- so this way...
         {
            // create directory structure if necessary
            // System.out.println ("entry: "+name);
            int x = name.lastIndexOf ("/");
            if (x != -1)
            {
               File dir = new File (base.getCanonicalPath () + File.separator + name.substring (0, x));
               if (!dir.exists ())
                  dir.mkdirs ();
            }
            // and extract...
            out = new FileOutputStream (base.getCanonicalPath () + File.separator + name);
            write (zin, out);
            out.close ();
         }
      }
      zin.close ();
   }
   


   /** deletes the given file:/... url recursively */    
   public static void deleteTree (URL _dir) throws IOException
   {
   	if (!_dir.getProtocol ().equals ("file"))
         throw new IOException ("Protocol not supported");

      File f = new File (_dir.getFile ());
      if (!delete (f))
         throw new IOException ("deleting " + _dir.toString () + "recursively failed!");
   }

   /** deletes a file recursively */    
   private static boolean delete (File _f) throws IOException
   {
   	if (_f.exists ())
   	{
      	if (_f.isDirectory ())
      	{
      		File[] files = _f.listFiles ();
      		for (int i = 0, l = files.length; i < l; ++i)
      		   if (!delete (files[i]))
      		       return false;
         }
         return _f.delete ();
      }
      return true;
   }


   /** writes the content of the InputStream into the OutputStream */
   private static void write (InputStream _in, OutputStream _out) throws IOException
   {
      int b;
      while ((b = _in.read ()) != -1)
         _out.write ((byte)b);
         	
      _out.flush ();
   }


}

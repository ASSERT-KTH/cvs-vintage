/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;

import java.net.URL;
import java.net.URLConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;



/**
*	Encapsulates URL bases copy and jar file operations.
* Very scratchy! Any improvements are welcome!
*      
*	@author Daniel Schulze <daniel.schulze@telkel.com>
*	@author Andreas Schaefer <andreas@jboss.org>
*	@version $Revision: 1.9 $
*/
public class URLWizzard
{
   
	private static byte[] buffer = new byte[1024*512];


   // Static --------------------------------------------------------
   public static void main (String[] _args) throws Exception
   {
      downloadAndPack (new URL (_args[0]), new URL (_args[1]));
   }
   
   
   
   
   /** copies the source to the destination url. As destination
   are currently only file:/... urls supported */
   public static URL download (URL _src, URL _dest) throws IOException
   {
      if (!_dest.getProtocol ().equals ("file"))
         throw new IOException ("only file: protocoll is allowed as destination!");         	
      
      InputStream in;
      OutputStream out;
      
      String s = _dest.getFile ();
      File dir = new File (s.substring (0, s.lastIndexOf("/")));
      if (!dir.exists ())
         dir.mkdirs ();
      
      in = _src.openStream ();
      out = new FileOutputStream (s); 
      
      write (in, out);
      
      out.close ();
      in.close ();
      
      return _dest;
   }
   
   /** copies the source to the destination. The destination is composed from the
   _destDirectory, _prefix and _suffix  */
   public static URL downloadTemporary (URL _src, URL _destDirectory, String _prefix, String _suffix) throws IOException
   {
      
      return download (_src, createTempFile (_destDirectory, _prefix, _suffix));
   }
   
   
   /** packs the source directory the _src url points to to a jar archiv at
   the _dest position */
   public static void downloadAndPack (URL _src, URL _dest) throws IOException
   {
      if (!_dest.getProtocol ().equals ("file"))
         throw new IOException ("only file: protocoll is allowed as destination!");         	
      if (!_src.getProtocol ().equals ("file"))
         throw new IOException ("only file: protocoll is allowed as source!");         	
      
      InputStream in;
      OutputStream out;
      
      String s = _dest.getFile ();
      File dir = new File (s.substring (0, s.lastIndexOf("/")));
      if (!dir.exists ())
         dir.mkdirs ();
      
      JarOutputStream jout = new JarOutputStream (new FileOutputStream (_dest.getFile()));
      
      // put all into the jar...
      add (jout, new File (_src.getFile()), "");
      jout.close ();
   }
   
   /** used by downloadAndPack*. */
   private static void add (JarOutputStream _jout, File _dir, String _prefix) throws IOException
   {
      File[] content = _dir.listFiles ();
      for (int i = 0, l = content.length; i < l; ++i)
      {
         if (content[i].isDirectory ())
         {
            add (_jout, content[i], _prefix+(_prefix.equals ("") ? "" : "/")+content[i].getName ());
         }
         else
         {
            // If no prefix then no '/' necessary
            _jout.putNextEntry(
               new ZipEntry(
                  ( "".equals( _prefix ) ? "" : _prefix + "/" ) + content[i].getName()
               )
            );
            FileInputStream in = new FileInputStream (content[i]);
            write (in, _jout);
            in.close ();
         }
      }
   }
   
   
   /** packs the source directory the _src url points to to a jar archiv at
   the position composed from _destDir, _prefix and _suffix */
   public static URL downloadAndPackTemporary (URL _src, URL _destDir, String _prefix, String _suffix) throws IOException
   {
      if (!_destDir.getProtocol ().equals ("file"))
         throw new IOException ("only file: protocoll is allowed as destination!");         	
      if (!_src.getProtocol ().equals ("file"))
         throw new IOException ("only file: protocoll is allowed as source!");         	
      
      InputStream in;
      OutputStream out;
      
      File dest = new File (createTempFile (_destDir, _prefix, _suffix).getFile ()); 
      JarOutputStream jout = new JarOutputStream (new FileOutputStream (dest));
      
      // put all into the jar...
      add (jout, new File (_src.getFile()), "");
      jout.close ();
      
      return dest.toURL ();
   }
   
   
   /** inflates the given zip file into the given directory  */
   public static URL downloadAndInflate (URL _src, URL _dest) throws IOException
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
         throw new IOException ("only file: protocoll is allowed as destination!");         	
      
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
      
      return _dest;
   }

   /** inflates the given zip file into a directory created in the dest directory with the 
   given prefix */
   public static URL downloadAndInflateTemporary (URL _src, URL _destDir, String _prefix) throws IOException
   {
      return downloadAndInflate (_src, createTempDir (_destDir, _prefix));
   }
   
   
   /** creates a directory like the File.createTempFile method */
   public static URL createTempDir (URL _baseDir, String _prefix) throws IOException
   {
      do 
      {
         File f = new File (_baseDir.getFile (), _prefix + getId ());
         if (!f.exists ())
         {
            f.mkdirs ();
			return new URL("file:"+f.getCanonicalPath());
            //return f.toURL ();
         }
      }
      while (true); // the endless loop should never cause trouble
   }
   
   private static int id = 1000; 
   
   /** used by createTempDir */
   private static String getId ()
   {
      return String.valueOf (++id);
   }
   
   /** creates a temporary file like File.createTempFile() */
   public static URL createTempFile (URL _baseDir, String _prefix, String _suffix) throws IOException
   {
      File f = new File (_baseDir.getFile ()); 
      if (!f.exists ())
         f.mkdirs ();
      
      File file;   
      do
      {
         file = new File (f, _prefix + getId () + _suffix); 
      }
      while (!file.createNewFile ());
         
      return new URL("file:"+file.getCanonicalPath());
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
   private static synchronized void write (InputStream _in, OutputStream _out) throws IOException
   {
      int read;
	  while (true)
	  {
		  read = _in.read(buffer);
		  if (read == -1)
			  break;

		  _out.write(buffer, 0, read);
	  }
      
      _out.flush ();
   }



}

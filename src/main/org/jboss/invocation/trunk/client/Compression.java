/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.invocation.trunk.client;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.jboss.logging.Logger;

/**
 * This is a support class used by the trunks to compress/uncompress byte data.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class Compression
{
   
   private static final Logger log = Logger.getLogger(Compression.class);

   /**
    * Should we use compression
    */
   public static boolean compressionEnabled =
      "true".equals(System.getProperty("org.jboss.invocation.trunk.enable_compression", "false"));

   /**
    * What compression level should we use: 0-9, defaults to 3
    */
   public static int compressionLevel =
      Integer.parseInt(System.getProperty("org.jboss.invocation.trunk.compression_level", "3"));

   /**
    * Compresses the input data.
    * @returns null if compression results in larger output.
    */
   static public byte[] compress(byte[] input)
   {
      if (!compressionEnabled)
         return null;

      // Too small to spend time compressing
      if (input.length < 10)
         return null;

      Deflater deflater = new Deflater(compressionLevel);
      deflater.setInput(input, 0, input.length);
      deflater.finish();
      byte[] buff = new byte[input.length + 50];
      int wsize = deflater.deflate(buff);

      int compressedSize = deflater.getTotalOut();
      
      // Did this data compress well?
      if (deflater.getTotalIn() != input.length)
         return null;
      if (compressedSize >= input.length-4)
         return null;

      byte[] output = new byte[compressedSize + 4];
      System.arraycopy(buff, 0, output, 4, compressedSize);
      output[0] = (byte)(input.length >> 24);
      output[1] = (byte)(input.length >> 16);
      output[2] = (byte)(input.length >> 8);
      output[3] = (byte)(input.length);
      return output;
   }

   /**
    * Un-compresses the input data.
    * @throws IOException if the input is not valid.
    */
   static public byte[] uncompress(byte[] input) throws IOException
   {
      try {
         int uncompressedSize = 
             (((input[0] & 0xff) << 24) +
             ((input[1] & 0xff) << 16) +
             ((input[2] & 0xff) << 8) +
             ((input[3] & 0xff)));         

         Inflater inflater = new Inflater();
         inflater.setInput(input, 4, input.length - 4);
         inflater.finished();
         
         byte[] out = new byte[uncompressedSize];
         inflater.inflate(out);
         
         inflater.reset();         
         return out;
         
      } catch (DataFormatException e ) {
         throw new IOException("Input Stream is corrupt: "+e);
      }
   }
   
}

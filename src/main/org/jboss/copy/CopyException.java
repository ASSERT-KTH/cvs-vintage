/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.jboss.util.NestedThrowable;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public class CopyException extends RuntimeException implements NestedThrowable
{
   private Throwable throwable;

   public CopyException()
   {
      super();
   }

   public CopyException(final String message)
   {
      super(message);
   }

   public CopyException(final Throwable throwable)
   {
      super();
      this.throwable = throwable;
   }

   public CopyException(final String message, final Throwable throwable)
   {
      super(message);
      this.throwable = throwable;
   }

   public Throwable getNested()
   {
      return throwable;
   }

   public Throwable getCause()
   {
      return throwable;
   }

   /**
    * Returns the composite throwable message.
    *
    * @return  The composite throwable message.
    */
   public String getMessage() {
      return NestedThrowable.Util.getMessage(super.getMessage(), throwable);
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print stream.
    *
    * @param stream  Stream to print to.
    */
   public void printStackTrace(final PrintStream stream)
   {
      if(throwable == null || NestedThrowable.PARENT_TRACE_ENABLED)
      {
         super.printStackTrace(stream);
      }
      NestedThrowable.Util.print(throwable, stream);
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print writer.
    *
    * @param writer  Writer to print to.
    */
   public void printStackTrace(final PrintWriter writer)
   {
      if(throwable == null || NestedThrowable.PARENT_TRACE_ENABLED)
      {
         super.printStackTrace(writer);
      }
      NestedThrowable.Util.print(throwable, writer);
   }

   /**
    * Prints the composite message and the embedded stack trace to
    * <tt>System.err</tt>.
    */
   public void printStackTrace()
   {
      printStackTrace(System.err);
   }
}

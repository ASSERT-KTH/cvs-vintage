package org.jboss.cmp.schema;

public class PersistenceException extends Exception
{
   private Throwable cause;

   public PersistenceException()
   {
   }

   public PersistenceException(String message)
   {
      super(message);
   }

   public PersistenceException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   public PersistenceException(Throwable cause)
   {
      super(cause == null ? null : cause.toString());
      this.cause = cause;
   }

   public Throwable getCause()
   {
      return cause;
   }

   public synchronized Throwable initCause(Throwable cause)
   {
      if (cause == this)
      {
         throw new IllegalArgumentException();
      }
      if (this.cause != null)
      {
         throw new IllegalStateException();
      }
      this.cause = cause;
      return this;
   }
}

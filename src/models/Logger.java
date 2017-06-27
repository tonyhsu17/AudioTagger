package models;

public interface Logger
{    
    public default void info(String message)
    {
        System.out.println("[INFO] " + this.getClass().getSimpleName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName() + "(): " + message);
    }
    
    public default void debug(String message)
    {
        System.out.println("[DEBUG] " + this.getClass().getSimpleName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName() + "(): " + message);
    }
    
    public default void error(String message)
    {
        System.err.println("[ERROR] " + this.getClass().getSimpleName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName() + "(): " + message);
    }
}

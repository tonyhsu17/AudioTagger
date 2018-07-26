package modules.controllers.base;

public interface TagBase<T extends Enum<T>>
{
    String name();
    
    Class<T> getDeclaringClass();
}

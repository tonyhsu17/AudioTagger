package support;

public interface TagBase<T extends Enum<T>>
{
    String name();
    
    Class<T> getDeclaringClass();
}

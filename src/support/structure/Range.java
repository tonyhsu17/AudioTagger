package support.structure;

import org.tonyhsu17.utilities.Logger;

public class Range implements Logger
{
    private int start;
    private int end;
    
    public Range(int start, int end)
    {
        this.start = start;
        this.end = end;
    }
    
    public int start()
    {
        return start;
    }

    public int end()
    {
        return end;
    }
}
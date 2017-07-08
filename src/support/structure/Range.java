package support.structure;

import support.Logger;

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
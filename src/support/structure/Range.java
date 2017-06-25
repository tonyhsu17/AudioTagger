package support.structure;

public class Range
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
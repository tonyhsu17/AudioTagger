import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import support.Utilities;

public class UtilitiesJUnitTest
{
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
        // reset data points
    }
    
    @Test
    public void testFindIntVal()
    {
        assertEquals(1, Utilities.findIntValue("1st"));
    }
    
    @Test
    public void testCommaSeparatedStringAnd()
    {
        List<String> list = new ArrayList<String>();
        
        list.add("one");
        assertEquals("one", Utilities.getCommaSeparatedStringWithAnd(list));
        
        list.add("two");
        assertEquals("one & two", Utilities.getCommaSeparatedStringWithAnd(list));
        
        list.add("three");
        assertEquals( "one, two & three", Utilities.getCommaSeparatedStringWithAnd(list));
        
        list.add("four");
        assertEquals("one, two, three & four", Utilities.getCommaSeparatedStringWithAnd(list));
        
        list.add("five");
        assertEquals("one, two, three, four & five", Utilities.getCommaSeparatedStringWithAnd(list));
    }
    
    @Test
    public void testCompareImage()
    {
//        Utilities.getComparedImage();
    }
    
    @Test
    public void testCompareName()
    {
        assertEquals("miku", Utilities.getComparedName("miku", "miku"));
        assertEquals("<Different Values>", Utilities.getComparedName("miku", "hatsune miku"));
    }
    
    @Test
    public void testSplitName()
    {
        String[] result = Utilities.splitName("first middle last");
        assertEquals("first middle", result[0]);
        assertEquals("last", result[1]);
        
        result = Utilities.splitName("aSingleName");
        assertEquals("aSingleName", result[0]);
        assertEquals("", result[1]);
        
    }
    
    @Test
    public void testCreateQuestionMarks()
    {
        assertEquals("?, ?, ?, ?, ?",Utilities.createQuestionMarks(5));
    }
    
    @Test
    public void testSplitBySeparators()
    {
        String names = "fripSide, claris, Arisa Meigeo & illy von esienberg";
        String[] result = Utilities.splitBySeparators(names);
        assertEquals(4, result.length);
        assertEquals("fripSide", result[0]);
        assertEquals("claris", result[1]);
        assertEquals("Arisa Meigeo",  result[2]);
        assertEquals("illy von esienberg", result[3]);
    }
}

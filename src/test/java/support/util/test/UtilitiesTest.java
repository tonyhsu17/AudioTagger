package support.util.test;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import support.structure.Range;
import support.util.Utilities;



public class UtilitiesTest {
    @DataProvider(name = "RangeValues")
    public static Object[][] rangeValues() {
        return new Object[][] {{"012345", 0, "0123", 0, 4},
            {"012345", 3, "0123", 0, 4}, 
            {"012345", 5, "", 5, 5},
            {"012345", 2, "2", 2, 3},
            {"", 0, "", 0, 0}};
    }
    
    @Test(dataProvider = "RangeValues")
    public void getRangeTest(String fullText, int caretPosition, String selectedText, int expectedStart, int expectedEnd) {
        Range range = Utilities.getRange(fullText, caretPosition, selectedText);
        assertEquals(range.start(), expectedStart, "range start");
        assertEquals(range.end(), expectedEnd, "range end");
    }
    

    @DataProvider(name = "IntegerValues")
    public static Object[][] integerValues() {
        return new Object[][] {{"random test 1st", 1},
            {"22nd awards", 22}, 
            {"33rd", 33},
            {"text 4th test", 4}, {"", -1}, {"123 something", 123},
            {null, -1},
            {"M-01 Ending theme", 1}};
    }
    
    @Test(dataProvider = "IntegerValues")
    public void testFindIntVal(String str, int value) {
        assertEquals(Utilities.findIntValueWithSuffix(str), value);
    }

    @DataProvider(name = "BooleanValues")
    public static Object[][] booleanValues() {
        return new Object[][] {{"y", true},
            {"Y", true}, 
            {"yea", true},
            {"asd", false}, {"", false}, {"n", false}};
    }
    
    @Test(dataProvider = "BooleanValues")
    public void testConvertToBoolean(String str, boolean value) {
        assertEquals(Utilities.convertToBoolean(str), value);
    }
}

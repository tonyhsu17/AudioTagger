package junit.support.util;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import support.util.Utilities;



public class UtilitiesTest {
    @Before
    public void setUp() {}

    @After
    public void tearDown() {
        // reset data points
    }

    @Test
    public void testFindIntVal() {
        assertEquals(1, Utilities.findIntValue("1st"));
    }


    @Test
    public void testCompareImage() {
        //        Utilities.getComparedImage();
    }


}

package support.util.test;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import support.util.StringUtil;



public class StringUtilTest {
    @DataProvider(name = "SplitNameValues")
    public static Object[][] rangeValues() {
        return new Object[][] {{"first middle last", "first middle", "last"},
            {"aSingleName", "aSingleName", ""}};
    }

    @Test(dataProvider = "SplitNameValues")
    public void testSplitName(String fullName, String first, String last) {
        String[] result = StringUtil.splitName(fullName);
        assertEquals(first, result[0], "first name");
        assertEquals(last, result[1], "last name");

    }

    @Test
    public void testCommaSeparatedStringAnd() {
        List<String> list = new ArrayList<String>();

        list.add("one");
        assertEquals("one", StringUtil.getCommaSeparatedStringWithAnd(list));

        list.add("two");
        assertEquals("one & two", StringUtil.getCommaSeparatedStringWithAnd(list));

        list.add("three");
        assertEquals("one, two & three", StringUtil.getCommaSeparatedStringWithAnd(list));

        list.add("four");
        assertEquals("one, two, three & four", StringUtil.getCommaSeparatedStringWithAnd(list));

        list.add("five");
        assertEquals("one, two, three, four & five", StringUtil.getCommaSeparatedStringWithAnd(list));
    }

    @Test
    public void testCompareName() {
        assertEquals("miku", StringUtil.getComparedName("miku", "miku"));
        assertEquals("<Different Values>", StringUtil.getComparedName("miku", "hatsune miku"));
    }


    @Test
    public void testCreateQuestionMarks() {
        assertEquals("?, ?, ?, ?, ?", StringUtil.createQuestionMarks(5));
    }

    @Test
    public void testSplitBySeparators() {
        String names = "fripSide, claris, Arisa Meigeo & illy von esienberg";
        String[] result = StringUtil.splitBySeparators(names);
        assertEquals(4, result.length);
        assertEquals("fripSide", result[0]);
        assertEquals("claris", result[1]);
        assertEquals("Arisa Meigeo", result[2]);
        assertEquals("illy von esienberg", result[3]);
    }

    @Test
    public void testGetDiffInDelim() {
        // also tests getStrInDelim()
        String[] posTest1 = {"High Free Spirits [TV.ver]", "High Free Spirits (TV Size)"};
        String[] posResult1 = {"[TV.ver]", "(TV Size)"};
        String[] posTest2 = {"level 5 ~judgelight~ [TVver]", "Level 5 -Judgelight- (TV Size)"};
        String[] posResult2 = {"~judgelight~", "-Judgelight-", "[TVver]", "(TV Size)"};

        List<String[]> list = StringUtil.getDiffInDelim(posTest1[0], posTest1[1]);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0)[0], posResult1[0]);
        assertEquals(list.get(0)[1], posResult1[1]);
        list = StringUtil.getDiffInDelim(posTest2[0], posTest2[1]);
        assertEquals(list.size(), 2);
        assertEquals(list.get(0)[0], posResult2[0]);
        assertEquals(list.get(0)[1], posResult2[1]);
        assertEquals(list.get(1)[0], posResult2[2]);
        assertEquals(list.get(1)[1], posResult2[3]);
    }
}

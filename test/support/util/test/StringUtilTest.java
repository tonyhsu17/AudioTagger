package support.util.test;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import support.Constants;
import support.util.StringUtil;



public class StringUtilTest {
    @DataProvider(name = "dataSplitName")
    public static Object[][] dataSplitName() {
        return new Object[][] {{"first middle last", "first middle", "last"},
            {"aSingleName", "aSingleName", ""},
            {"a b c d", "a b c", "d"}};
    }

    @Test(dataProvider = "dataSplitName")
    public void testSplitName(String fullName, String first, String last) {
        String[] result = StringUtil.splitName(fullName);
        assertEquals(first, result[0], "first name");
        assertEquals(last, result[1], "last name");
    }

    @DataProvider(name = "dataSplitBySeparators")
    public static Object[][] dataSplitBySeparators() {
        return new Object[][] {
            {"fripSide, claris, Arisa Meigeo & illya von esienberg", "fripSide", "claris", "Arisa Meigeo", "illya von esienberg"},
            {"fripSide & claris", "fripSide", "claris"},
            {"", ""},
            {"illya", "illya"}};
    }

    @Test(dataProvider = "dataSplitBySeparators")
    public void testSplitBySeparators(String fullString, String... individuals) {
        String[] strings = StringUtil.splitBySeparators(fullString);
        assertEquals(strings.length, individuals.length);
        for(int i = 0; i < strings.length; i++) {
            assertEquals(strings[i], individuals[i]);
        }
    }

    @DataProvider(name = "dataCreateQuestionMarks")
    public static Object[][] questionMarksValues() {
        return new Object[][] {
            {1, "?"},
            {2, "?, ?"},
            {5, "?, ?, ?, ?, ?"}};
    }

    @Test(dataProvider = "dataCreateQuestionMarks")
    public void testCreateQuestionMarks(int count, String expected) {
        assertEquals(StringUtil.createQuestionMarks(count), expected);
    }

    @DataProvider(name = "dataCompareName")
    public static Object[][] dataCompareName() {
        return new Object[][] {{"miku", "miku", "miku"},
            {"hatsune miku", "miku", Constants.KEYWORD_DIFF_VALUE},
            {"miku", "hatsune miku", Constants.KEYWORD_DIFF_VALUE},
            {"", "miku", Constants.KEYWORD_DIFF_VALUE},
            {null, "miku", Constants.KEYWORD_DIFF_VALUE},
            {"aasd", null, Constants.KEYWORD_DIFF_VALUE},
            {null, null, null}};
    }

    @Test(dataProvider = "dataCompareName")
    public void testCompareName(String s1, String s2, String compareVal) {
        assertEquals(StringUtil.getComparedName(s1, s2), compareVal);
    }

    @Test
    public void testIsKeyword() {
        assertEquals(StringUtil.isKeyword(Constants.KEYWORD_DIFF_VALUE), true);
        assertEquals(StringUtil.isKeyword("not keyword"), false);
    }

    @Test
    public void testGetCommaSeparatedStringWithAnd() {
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

    @DataProvider(name = "dataGetStrInDelim")
    public static Object[][] dataGetStrInDelim() {
        return new Object[][] {{"asd [instru]", "[instru]"},
            {"asd -tv size- asddsa", "-tv size-"},
            {"asd (tv ver)-instrumental- asd ~remix~;", "(tv ver)", "-instrumental-", "~remix~"}};
    }

    @Test(dataProvider = "dataGetStrInDelim")
    public void testGetStrInDelim(String search, String... expectedList) {
        List<String> list = StringUtil.getStrInDelim(search);
        assertEquals(expectedList.length, list.size());
        for(int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), expectedList[i]);
        }
    }

    @DataProvider(name = "dataGetDiffInDelim")
    public static Object[][] dataGetDiffInDelim() {
        return new Object[][] {
            {"High Free Spirits (TV Size)", "High Free Spirits (TV Size)", ""},
            {"High Free Spirits [TV.ver]", "High Free Spirits (TV Size)", "[TV.ver]", "(TV Size)"},
            {"level 5 ~judgelight~ [TVver](remix)", "Level 5 -Judgelight- (TV Size) (remix)", "~judgelight~", "-Judgelight-", "[TVver]", "(TV Size)"},
            {"<Different Values>", "<Different Values>", ""}};
    }

    @Test(dataProvider = "dataGetDiffInDelim")
    public void testGetDiffInDelim(String s1, String s2, String... expected) {
        List<String[]> list = StringUtil.getDiffInDelim(s1, s2);
        assertEquals(list.size(), expected.length / 2);
        for(int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i)[0], expected[i * 2]);
            assertEquals(list.get(i)[1], expected[i * 2 + 1]);
        }
    }
}

package unit.test;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import model.base.TagBase;
import model.information.VGMDBParser;
import support.util.Utilities.EditorTag;

public class VGMDBParserJUnitTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
        VGMDBParser parser = new VGMDBParser();
        System.out.println("Tag: " + EditorTag.ALBUM + " value: " + parser.getDataForTag(EditorTag.ALBUM, ""));
        TagBase<?>[] addtional = parser.getAdditionalTags();
        for(TagBase<?> t : addtional)
        {
            System.out.println("Tag: " + t + " value: " + parser.getDataForTag(t, ""));
        }
       
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void test()
    {
        fail("Not yet implemented");
    }

}

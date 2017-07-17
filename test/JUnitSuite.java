


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.support.util.StringUtilTest;
import junit.support.util.UtilitiesTest;

/**
 * JUnit Test Suite
 * 
 * @author Tony Hsu
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({ 
   DatabaseJUnitTest.class,
   AudioFileJUnitTest.class,
   UtilitiesTest.class,
   StringUtilTest.class
})

public class JUnitSuite {
}

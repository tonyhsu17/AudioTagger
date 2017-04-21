


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JUnit Test Suite
 * 
 * @author Tony Hsu
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({ 
   DatabaseJUnitTest.class,
   AudioFileJUnitTest.class,
   UtilitiesJUnitTest.class,
})

public class JUnitSuite {
}

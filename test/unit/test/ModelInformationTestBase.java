package unit.test;

import java.util.ArrayList;
import java.util.List;

import modules.controllers.base.TagBase;



public class ModelInformationTestBase {

    public String keywordsTest(List<TagBase<?>> actual, List<TagBase<?>> expected) {
        List<TagBase<?>> actualCopy;
        List<TagBase<?>> expectedCopy = new ArrayList<TagBase<?>>(expected);

        for(TagBase<?> tag : expected) {
            if(actual.contains(tag)) {
                actual.remove(tag);
                expectedCopy.remove(tag);
            }
        }

        actualCopy = new ArrayList<TagBase<?>>(actual);
        for(TagBase<?> tag : actual) {
            if(expectedCopy.contains(tag)) {
                actualCopy.remove(tag);
                expectedCopy.remove(tag);
            }
        }

        String returnString = "";
        if(expectedCopy.size() > 0) {
            returnString += "Not found in actual: [";
            String comma = "";
            for(TagBase<?> tag : expectedCopy) {
                returnString += comma + tag;
                comma = ", ";
            }
            returnString += "]\n";
        }
        if(actualCopy.size() > 0) {
            returnString += "Extra in actual found: [";
            String comma = "";
            for(TagBase<?> tag : actualCopy) {
                returnString += comma + tag;
                comma = ", ";
            }
            returnString += "]\n";
        }
        return returnString;
    }
}

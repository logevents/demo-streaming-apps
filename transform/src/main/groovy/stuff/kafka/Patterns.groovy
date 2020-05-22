package stuff.kafka

import java.util.regex.Matcher
import java.util.regex.Pattern

class Patterns {
    static String fetch1g(String text, Pattern oneGroup) {
        def m = oneGroup.matcher(text)

        m.find() ? m.group(1) : null
    }

    static List<String> fetchGroups(String text, Pattern groups) {
        def m = groups.matcher(text)

        m.find() ? groupsInternal(m) : null
    }

    private static List<String> groupsInternal(Matcher m) {
        def r = []
        for (int i = 1; i <= m.groupCount(); i++) {
            r.add(m.group(i))
        }
        r
    }
}

package stuff.kafka

import java.util.regex.Pattern

class StateAnalyzer {
    static final Pattern STATE_PATTERN = ~/Finished: (SUCCESS|FAILURE|ABORTED|NOT_BUILT|UNSTABLE)/

    String getState(LogEvent log) {
        Patterns.fetch1g(log.message, STATE_PATTERN)
    }
}

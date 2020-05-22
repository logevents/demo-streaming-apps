package stuff.kafka

import com.fasterxml.jackson.annotation.JsonProperty

class FilebeatEntity {
    static class Fields  {
        String master
        String protocol
        String domainSuffix
    }

    static class Log {
        static class File {
            String path
        }

        long offset
        File file
    }

    Date timestamp
    String message
    Fields fields
    Log log

    @JsonProperty("@timestamp")
    Date getTimestamp() {
        timestamp
    }
}

package stuff.kafka

import com.fasterxml.jackson.annotation.JsonProperty

class LogEvent {
    String master
    String buildUrl
    String jobName
    int buildNum
    int fileOffset
    long kafkaOffset
    String message

    @JsonProperty("@timestamp")
    Date timestamp


    @Override
    public String toString() {
        return "LogEvent{" +
                "master='" + master + '\'' +
                ", buildUrl='" + buildUrl + '\'' +
                ", jobName='" + jobName + '\'' +
                ", buildNum=" + buildNum +
                ", fileOffset=" + fileOffset +
                ", kafkaOffset=" + kafkaOffset +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

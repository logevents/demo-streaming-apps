package stuff.kafka

class DurationEvent {
    String buildUrl
    String key
    String jobName
    int buildNum
    String name
    Date started
    Date finished
    long elapsedMillis

    @Override
    String toString() {
        return "DurationEvent{" +
                "buildUrl='" + buildUrl + '\'' +
                ", key='" + key + '\'' +
                ", jobName='" + jobName + '\'' +
                ", buildNum=" + buildNum +
                ", name='" + name + '\'' +
                ", started=" + started +
                ", finished=" + finished +
                ", elapsedMillis=" + elapsedMillis +
                '}';
    }
}

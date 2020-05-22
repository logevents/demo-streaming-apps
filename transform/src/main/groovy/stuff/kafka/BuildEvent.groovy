package stuff.kafka

class BuildEvent {
    String buildUrl
    String key
    String jobName
    int buildNum
    String state
    Date started
    Date finished
    long elapsedMillis
    Date lastUpdate


    @Override
    public String toString() {
        return "BuildEvent{" +
                "buildUrl='" + buildUrl + '\'' +
                ", key='" + key + '\'' +
                ", jobName='" + jobName + '\'' +
                ", buildNum=" + buildNum +
                ", state='" + state + '\'' +
                ", started=" + started +
                ", finished=" + finished +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}

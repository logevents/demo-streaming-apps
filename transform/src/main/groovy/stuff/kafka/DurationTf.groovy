package stuff.kafka

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized

class DurationTf extends StreamsRunner {
    private final String logSource
    private final String durationTarget

    DurationTf(String servers, String appId, String autoOffset,
               String logSource, String durationTarget) {
        super(servers, appId, autoOffset)
        this.durationTarget = durationTarget
        this.logSource = logSource
    }

    static void create(String[] args) {
        new DurationTf(args[0], args[1], args[2], args[3], args[4]).start()
    }

    @Override
    StreamsBuilder createTopologyBuilder() {
        def builder = new StreamsBuilder()
        def logs = builder.stream(logSource, Consumed.with(Serdes.String(), KafkaConsts.createJsonSerde(LogEvent)))

        logs.filter {
            key, value -> value.type == 'duration'
        }
        .map {
            key, log ->
                def duration = new DurationEvent()
                duration.buildUrl = log.buildUrl
                duration.jobName = log.jobName
                duration.buildNum = log.buildNum
                duration.key = log.buildUrl + '/' + log.message
                duration.name = log.message
                if (log.subType == 'start') {
                    duration.started = log.timestamp
                } else {
                    duration.finished = log.timestamp
                }

                new KeyValue<String, DurationEvent>(duration.key, duration)
        }
        .groupByKey(Grouped.with(Serdes.String(), KafkaConsts.createJsonSerde(DurationEvent)))
                .aggregate({ new DurationEvent() }, {
            key, newDuration, existingDuration ->
                existingDuration.buildUrl = newDuration.buildUrl ?: existingDuration.buildUrl
                existingDuration.jobName = newDuration.jobName ?: existingDuration.jobName
                existingDuration.buildNum = newDuration.buildNum ?: existingDuration.buildNum

                existingDuration.name = newDuration.name?: existingDuration.name

                if (newDuration.started) {
                    existingDuration.started = newDuration.started
                }

                if (newDuration.finished) {
                    existingDuration.finished = newDuration.finished
                }

                existingDuration.elapsedMillis = existingDuration.started && existingDuration.finished ?
                        existingDuration.finished.time - existingDuration.started.time : 0

                existingDuration.key = existingDuration.key?: newDuration.key

                existingDuration
        }, Materialized.with(Serdes.String(), KafkaConsts.createJsonSerde(DurationEvent)))
                .toStream()
                .peek { key, value ->
            println 'created: ' + value.toString()
        }
        .to(durationTarget)

        return builder
    }
}

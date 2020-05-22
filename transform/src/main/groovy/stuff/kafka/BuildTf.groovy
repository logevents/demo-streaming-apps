package stuff.kafka

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KeyValueMapper
import org.apache.kafka.streams.kstream.Materialized

class BuildTf extends StreamsRunner {
    private final String logSource
    private final String buildTarget

    BuildTf(String servers, String appId, String autoOffset,
            String logSource, String buildTarget) {
        super(servers, appId, autoOffset)
        this.buildTarget = buildTarget
        this.logSource = logSource
    }

    static void create(String[] args) {
        new BuildTf(args[0], args[1], args[2], args[3], args[4]).start()
    }

    @Override
    StreamsBuilder createTopologyBuilder() {
        def builder = new StreamsBuilder()
        def logs = builder.stream(logSource, Consumed.with(Serdes.String(), KafkaConsts.createJsonSerde(LogEvent)))

        def stateAnalyzer = new StateAnalyzer()

        logs
                .map(new KeyValueMapper<String, LogEvent, KeyValue<String, BuildEvent>>() {
                    @Override
                    KeyValue<String, BuildEvent> apply(String key, LogEvent log) {
                        def build = new BuildEvent()
                        build.buildUrl = log.buildUrl
                        build.jobName = log.jobName
                        build.buildNum = log.buildNum
                        build.lastUpdate = log.timestamp

                        build.state = stateAnalyzer.getState(log)

                        new KeyValue<String, BuildEvent>(log.buildUrl, build)
                    }
                })
                .groupByKey(Grouped.with(Serdes.String(), KafkaConsts.createJsonSerde(BuildEvent)))
                .aggregate({ new BuildEvent() }, {
                    key, newBuild, existingBuild ->
                        existingBuild.buildUrl = newBuild.buildUrl ?: existingBuild.buildUrl
                        existingBuild.jobName = newBuild.jobName ?: existingBuild.jobName
                        existingBuild.buildNum = newBuild.buildNum ?: existingBuild.buildNum
                        existingBuild.state = newBuild.state ?: existingBuild.state
                        existingBuild.lastUpdate = newBuild.lastUpdate

                        // considers unordered events
                        if (existingBuild.started && newBuild.lastUpdate) {
                            if (newBuild.lastUpdate < existingBuild.started) {
                                existingBuild.lastUpdate = existingBuild.started
                                existingBuild.started = newBuild.lastUpdate
                            } else {
                                existingBuild.lastUpdate = existingBuild.lastUpdate < newBuild.lastUpdate ?
                                        existingBuild.lastUpdate : newBuild.lastUpdate
                            }

                            existingBuild.elapsedMillis = existingBuild.lastUpdate.time - existingBuild.started.time
                        } else {
                            existingBuild.started = existingBuild.started ?: newBuild.lastUpdate
                            existingBuild.lastUpdate = existingBuild.lastUpdate ?: newBuild.lastUpdate
                        }

                        if (existingBuild.state) {
                            existingBuild.finished = existingBuild.lastUpdate
                        }

                        existingBuild.key

                        existingBuild
                }, Materialized.with(Serdes.String(), KafkaConsts.createJsonSerde(BuildEvent)))
                .toStream()
                .peek { key, value ->
                    println 'created: ' + value.toString()
                }
                .to(buildTarget)

        return builder
    }
}

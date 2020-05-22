package stuff.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig

abstract class StreamsRunner {
    private props

    StreamsRunner(String servers, String appId, String autoOffset) {
        props = new Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId)
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, servers)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset)
    }

    def start() {
        def streams = new KafkaStreams(createTopologyBuilder().build(), props)
        streams.start()
        Runtime.getRuntime().addShutdownHook({streams.close()})
    }

    abstract StreamsBuilder createTopologyBuilder()
}

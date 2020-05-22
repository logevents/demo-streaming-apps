package stuff.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import java.util.concurrent.Executors

interface LoopProcessor {
    void process(ConsumerRecords<String, String> records)
}

class LoopClient implements Runnable {
    private LoopProcessor processor
    private final props
    private KafkaConsumer consumer

    LoopClient(String bootstrapServers, String groupId) {
        props = new Properties()

        this.props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        this.props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        this.props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 'latest')
    }

    void init(String topic, LoopProcessor processor) {
        this.processor = processor
        this.consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer())
        consumer.subscribe([topic])

        def pool = Executors.newSingleThreadExecutor()
        pool.submit(this)
    }

    @Override
    void run() {
        while (true) {
            def records = consumer.poll(5000)

            if (!records.isEmpty()) {
                processor.process(records)
            }
        }
    }
}

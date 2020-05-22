package stuff.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

class ProducerClient<T> {
    Properties props = new Properties()
    KafkaProducer<String, T> producer
    private final Serializer<T> ser

    ProducerClient(String bootstrapServers, String clientId, Serializer<T> ser) {
        this.ser = ser
        this.props.put(ProducerConfig.ACKS_CONFIG, 'all')
        this.props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        this.props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId)
    }

    void init() {
        this.producer = new KafkaProducer<String, T>(props, new StringSerializer(), ser)
    }

    def send(String topic, String key, T message) {
        producer.send(new ProducerRecord<>(topic, key, message))
    }
}

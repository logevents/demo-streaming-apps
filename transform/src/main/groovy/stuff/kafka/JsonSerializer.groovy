package stuff.kafka

import org.apache.kafka.common.serialization.Serializer

class JsonSerializer<T> implements Serializer<T> {
    @Override
    byte[] serialize(String topic, T data) {
        return KafkaConsts.JSON.writeValueAsString(data).bytes
    }
}

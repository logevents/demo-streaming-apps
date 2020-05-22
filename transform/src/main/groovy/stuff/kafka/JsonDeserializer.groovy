package stuff.kafka

import org.apache.kafka.common.serialization.Deserializer

class JsonDeserializer<T> implements Deserializer<T> {
    private final Class<T> c

    JsonDeserializer(Class<T> c) {
        this.c = c
    }

    @Override
    T deserialize(String topic, byte[] data) {
        return KafkaConsts.JSON.readValue(data, c)
    }
}

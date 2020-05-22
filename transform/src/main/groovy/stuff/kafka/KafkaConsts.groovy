package stuff.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes

import java.text.SimpleDateFormat

class KafkaConsts {
    static final JSON = createMapper()

    private static ObjectMapper createMapper() {
        def mapper = new ObjectMapper()
        mapper.setTimeZone(TimeZone.getTimeZone('UTC'))
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        mapper
    }
    static final JSON_SE = new JsonSerializer()

    static <T> Serde<T> createJsonSerde(Class<T> c) {
        Serdes.serdeFrom(JSON_SE, new JsonDeserializer<T>(c))
    }
}

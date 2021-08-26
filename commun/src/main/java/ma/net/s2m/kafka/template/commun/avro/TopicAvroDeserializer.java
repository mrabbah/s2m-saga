package ma.net.s2m.kafka.template.commun.avro;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import org.apache.avro.Schema;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rabbah
 * @param <T>
 */
public class TopicAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicAvroDeserializer.class);

    protected final Class<T> targetType;
    
    private final boolean useBinaryEncoding;

    public TopicAvroDeserializer(Class<T> targetType, boolean useBinaryEncoding) {
        this.targetType = targetType;
        this.useBinaryEncoding = useBinaryEncoding;
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
        // No-op
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            T result = null;
            if (data != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("data='{}' ({})", DatatypeConverter.printHexBinary(data), new String(data));
                }
                Class<? extends SpecificRecordBase> specificRecordClass =
                        (Class<? extends SpecificRecordBase>) targetType;
                Schema schema = specificRecordClass.newInstance().getSchema();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("schema='{}'", schema.toString());
                }
                DatumReader<T> datumReader =
                        new SpecificDatumReader<>(schema);
                Decoder decoder = useBinaryEncoding ?
                        DecoderFactory.get().binaryDecoder(data, null) :
                        DecoderFactory.get().jsonDecoder(schema, new ByteArrayInputStream(data));;

                result = datumReader.read(null, decoder);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("deserialized data={}:{}", targetType.getName(), result);
                }
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new SerializationException("Can't deserialize data '" + Arrays.toString(data) + "'", e);
        }
    }
    
}

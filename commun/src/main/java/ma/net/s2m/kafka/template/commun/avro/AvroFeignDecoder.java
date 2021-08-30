package ma.net.s2m.kafka.template.commun.avro;

// import com.vladkrava.converter.serialization.AvroDeserializer;
// import com.vladkrava.converter.serialization.DataSerializationException;
import feign.FeignException;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.IOException;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author rabbah
 */
@Slf4j
public class AvroFeignDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        if (type instanceof Class) {
            Class<?> c = (Class) type;
            if (SpecificRecordBase.class.isAssignableFrom(c)) {
                return decode(response, new SpecificDatumReader<>(c));
            } else if (GenericRecord.class.isAssignableFrom(c)) {
                return decode(response, new GenericDatumReader<GenericRecord>());
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported type: %s", type));
    }

    private <T> T decode(Response response, DatumReader<T> datumReader) throws IOException {
        try (DataFileStream<T> dataStream = new DataFileStream<>(response.body().asInputStream(), datumReader)) {
            return dataStream.hasNext() ? dataStream.next() : null;
        }
    }
}

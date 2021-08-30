package ma.net.s2m.kafka.template.commun.avro;

import feign.Request;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 *
 * @author rabbah
 */
public class AvroFeignEncoder implements Encoder {
    
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        IndexedRecord record = (IndexedRecord) object;
        Schema schema = record.getSchema();
        DatumWriter<IndexedRecord> datumWriter = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            try (DataFileWriter<IndexedRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {
                dataFileWriter.create(schema, outputStream);
                dataFileWriter.append(record);
            }
        } catch (IOException ex) {
            throw new EncodeException("Failed to encode object", ex);
        }
        
        template.body(Request.Body.encoded(outputStream.toByteArray(), null));
    }

}

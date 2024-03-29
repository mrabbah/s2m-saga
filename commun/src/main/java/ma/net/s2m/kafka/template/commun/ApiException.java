package ma.net.s2m.kafka.template.commun;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1;
    private static final ZoneId utc = ZoneId.of("UTC");
    private OffsetDateTime timestamp;
    private HttpStatus status;

    public ApiException(HttpStatus status, String msg) {
        super(msg);
        this.timestamp = OffsetDateTime.now(utc);
        this.status = status;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

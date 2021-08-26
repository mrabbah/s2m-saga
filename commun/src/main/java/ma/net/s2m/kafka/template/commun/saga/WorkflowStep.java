package ma.net.s2m.kafka.template.commun.saga;

import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
public interface WorkflowStep {
    WorkflowStepStatus getStatus();
    Mono<Boolean> process();
    Mono<Boolean> revert();
}

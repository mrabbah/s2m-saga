package ma.net.s2m.kafka.template.commun.saga;

import java.util.List;

/**
 *
 * @author rabbah
 */
public interface Workflow {
    List<WorkflowStep> getSteps();
}

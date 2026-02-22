package info.quazi.valueProtect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    public void moveToAppraiserQueue(String appraisalId) {
        log.info("Workflow transition: appraisal {} moved to APPRAISER_QUEUE", appraisalId);
    }
}

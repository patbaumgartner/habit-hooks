package com.patbaumgartner.habithooks.tasks;

import java.util.List;

/** A compact task generated from one or more analyzer findings. */
public record AgentTask(String id, String title, String ruleId, String dimension, String severity, int count,
        String verificationCommand, List<String> acceptanceCriteria, List<String> locations) {

    /** Creates a defensive-copy task. */
    public AgentTask {
        acceptanceCriteria = List.copyOf(acceptanceCriteria);
        locations = List.copyOf(locations);
    }

}

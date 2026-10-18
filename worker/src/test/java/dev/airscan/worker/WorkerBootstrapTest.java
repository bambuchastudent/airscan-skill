package dev.airscan.worker;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class WorkerBootstrapTest {
    @Test
    void workerHasIndependentEntrypoint() throws Exception {
        assertNotNull(WorkerBootstrap.class.getDeclaredMethod("main", String[].class));
    }
}

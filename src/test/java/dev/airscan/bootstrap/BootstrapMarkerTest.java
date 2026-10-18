package dev.airscan.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BootstrapMarkerTest {
    @Inject
    BootstrapMarker marker;

    @Test
    void startsQuarkusAndInjectsBootstrapBean() {
        assertEquals("ready", marker.status());
    }
}

package dev.airscan.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BootstrapResourceTest {
    @Inject
    BootstrapResource resource;

    @Test
    void returnsBootstrapStatus() {
        assertEquals("ready", resource.status());
    }
}

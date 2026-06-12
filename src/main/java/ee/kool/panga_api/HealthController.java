package ee.kool.panga_api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@Tag(name = "Health", description = "API tervisekontroll")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Kontrollib, kas API töötab")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "panga-api",
                "timestamp", Instant.now().toString()
        );
    }
}
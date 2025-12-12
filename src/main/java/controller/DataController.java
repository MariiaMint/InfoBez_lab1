package controller;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        return ResponseEntity.ok(Map.of("message", "public OK"));
    }

    @GetMapping("/data")
    public ResponseEntity<?> privateData(Authentication auth) {

        List<Map<String, Object>> data = List.of(
                Map.of(
                        "id", 1,
                        "author", StringEscapeUtils.escapeHtml4("Alice <script>bad()</script>"),
                        "text", StringEscapeUtils.escapeHtml4("Hello")
                ),
                Map.of(
                        "id", 2,
                        "author", StringEscapeUtils.escapeHtml4("Bob"),
                        "text", StringEscapeUtils.escapeHtml4("Private for: " + auth.getName())
                )
        );

        return ResponseEntity.ok(data);
    }
}

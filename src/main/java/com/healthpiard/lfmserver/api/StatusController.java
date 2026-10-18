package com.healthpiard.lfmserver.api;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/lfm")
public class StatusController {
 @GetMapping("/status") public Map<String,Object> status() { return Map.of("service","lfm-server","version","0.7.0","status","UP"); }
}

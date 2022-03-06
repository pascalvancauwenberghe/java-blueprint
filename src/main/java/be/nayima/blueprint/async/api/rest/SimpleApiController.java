package be.nayima.blueprint.async.api.rest;

import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleApiController {
    @GetMapping("/example")
    public String example() {
        return "Hello User !! " + new Date();
    }
}
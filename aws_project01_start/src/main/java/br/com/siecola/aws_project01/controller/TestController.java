package br.com.siecola.aws_project01.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/dog/{name}")
    public ResponseEntity<?> dogTest(@PathVariable String name) {
        LOG.info("Test controller - name: {}", name);

        return ResponseEntity.ok("Name: " + name);
    }

    @GetMapping("/dog/color")
    public ResponseEntity<?> dogColor() {
        LOG.info("Test controller - Always black!");

        return ResponseEntity.ok("Always black!");
    }


    @GetMapping("/dog/age")
    public ResponseEntity<?> dogAge() {
        LOG.info("Test controller - Age: 5");

        return ResponseEntity.ok("Age: 5");
    }

    // create a post method to insert a new dog
    @PostMapping("/dog")
    public ResponseEntity<?> dogInsert(@RequestBody String name) {
        LOG.info("Test controller - name: {}", name);

        return ResponseEntity.ok("Name: " + name);
    }
}

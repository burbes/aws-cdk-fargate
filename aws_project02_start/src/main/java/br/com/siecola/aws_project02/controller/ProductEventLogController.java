package br.com.siecola.aws_project02.controller;

import br.com.siecola.aws_project02.model.ProductEventLog;
import br.com.siecola.aws_project02.model.ProductEventLogDTO;
import br.com.siecola.aws_project02.repository.ProductEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
public class ProductEventLogController {

    private final ProductEventLogRepository productEventLogRepository;

    @Autowired
    public ProductEventLogController(ProductEventLogRepository productEventLogRepository) {
        this.productEventLogRepository = productEventLogRepository;
    }

    @GetMapping("/events")
    public List<ProductEventLogDTO> getEvents() {
        return StreamSupport.stream(productEventLogRepository.findAll().spliterator(), false)
                .map(ProductEventLogDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/events/{code}")
    public List<ProductEventLogDTO> getEventsByCode(@PathVariable String code) {
        return productEventLogRepository.findAllByPk(code).stream()
                .map(ProductEventLogDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/events/{code}/{eventType}")
    public List<ProductEventLogDTO> getEventsByCodeAndEventType(@PathVariable String code, @PathVariable String eventType) {
        return productEventLogRepository.findAllByPkAndSkStartsWith(code, eventType).stream()
                .map(ProductEventLogDTO::new)
                .collect(Collectors.toList());
    }

}

package br.com.siecola.aws_project01.controller;

import br.com.siecola.aws_project01.model.Invoice;
import br.com.siecola.aws_project01.model.UrlResponse;
import br.com.siecola.aws_project01.repository.InvoiceRepository;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Value("${aws.s3.bucket.invoice.name}")
    private String bucketName;

    private AmazonS3 amazonS3;
    private InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceController(AmazonS3 amazonS3, InvoiceRepository invoiceRepository) {
        this.amazonS3 = amazonS3;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createInvoiceUrl() {
        UrlResponse urlResponse = new UrlResponse();
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5));
        String processID = UUID.randomUUID().toString();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, processID)
                        .withMethod(HttpMethod.PUT) //using EventType.OBJECT_CREATED_PUT in order to limit the event to only PUT events
                        .withExpiration(Date.from(expirationTime));


        urlResponse.setUrl(amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString());
        urlResponse.setExpirationTime(expirationTime.toEpochMilli());

        return ResponseEntity.ok(urlResponse);
    }

    /**
     * {
     * "invoiceNumber": "ABC-123",
     * "customerName": "Matilde",
     * "totalValue": 1250.00,
     * "productID": 1,
     * "quantity": 2
     * }
     */


    @GetMapping
    public Iterable<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @GetMapping(path = "/bycustomername")
    public Iterable<Invoice> findByCustomerName(@RequestParam String customerName) {
        return invoiceRepository.findAllByCustomerName(customerName);
    }

}
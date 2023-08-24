package br.com.siecola.aws_project02.service;

import br.com.siecola.aws_project02.enums.EventType;
import br.com.siecola.aws_project02.model.Envelope;
import br.com.siecola.aws_project02.model.ProductEvent;
import br.com.siecola.aws_project02.model.ProductEventLog;
import br.com.siecola.aws_project02.model.SnsMessage;
import br.com.siecola.aws_project02.repository.ProductEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final ProductEventLogRepository productEventLogRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ProductEventConsumer(ObjectMapper objectMapper, ProductEventLogRepository productEventLogRepository) {
        this.objectMapper = objectMapper;
        this.productEventLogRepository = productEventLogRepository;
    }

    @JmsListener(destination = "${aws.sqs.queue.product.events.name}")
    public void receiveProductEvent(TextMessage message) throws JMSException, IOException {
        log.info("Product event received: {}", message);

        SnsMessage snsMessage = objectMapper.readValue(message.getText(), SnsMessage.class);
        log.info("SNS message: {}", snsMessage.toString());


        Envelope envelope = objectMapper.readValue(snsMessage.getMessage(), Envelope.class);
        EventType eventType = envelope.getEventType();

        ProductEvent productEvent = objectMapper.readValue(envelope.getData(), ProductEvent.class);

        Long productId = productEvent.getProductId();
        String code = productEvent.getCode();
        String username = productEvent.getUsername();

        log.info("Event type: {} Product event received - ProductID: {}, Code: {}, User: {}, MessageId: {}",
                eventType, productId, code, username,
                snsMessage.getMessageId());


        //save into DynamoDB
        ProductEventLog productEventLog = buildProductEventLog(envelope, productEvent, snsMessage.getMessageId());
        productEventLogRepository.save(productEventLog);
    }

    private ProductEventLog buildProductEventLog(Envelope envelope,
                                                 ProductEvent productEvent,
                                                 String messageId) {

        long timestamp = Instant.now().toEpochMilli();

        ProductEventLog productEventLog = new ProductEventLog();
        productEventLog.setPk(productEvent.getCode());
        productEventLog.setSk(envelope.getEventType().toString() + "_" + timestamp);
        productEventLog.setEventType(envelope.getEventType());
        productEventLog.setProductId(productEvent.getProductId());
        productEventLog.setUsername(productEvent.getUsername());
        productEventLog.setTimestamp(timestamp);
        productEventLog.setTtl(Instant.now().plus(Duration.ofMinutes(10)).getEpochSecond());
        productEventLog.setMessageId(messageId);

        return productEventLog;

    }

}

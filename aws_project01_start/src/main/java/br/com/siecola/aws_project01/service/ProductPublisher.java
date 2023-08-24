package br.com.siecola.aws_project01.service;

import br.com.siecola.aws_project01.enums.EventType;
import br.com.siecola.aws_project01.model.Envelope;
import br.com.siecola.aws_project01.model.Product;
import br.com.siecola.aws_project01.model.ProductEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPublisher.class);
    private final AmazonSNS snsClient;
    private final Topic productEventsTopic;
    private final ObjectMapper objectMapper;

    public ProductPublisher(AmazonSNS snsClient, @Qualifier("productEventsTopic") Topic productEventsTopic,
                            ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.productEventsTopic = productEventsTopic;
        this.objectMapper =objectMapper;
    }


    public void publishProductEvent(Product product, EventType eventType, String username) {

        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(product.getId());
        productEvent.setCode(product.getCode());
        productEvent.setUsername(username);


        Envelope envelope = new Envelope();
        envelope.setEventType(eventType);

        try {
            envelope.setData(objectMapper.writeValueAsString(productEvent));

            PublishResult publishResult = snsClient.publish(productEventsTopic.getTopicArn(), objectMapper.writeValueAsString(envelope));
            LOG.info("Product event published - EventType: {} ProductId: {}, MessageId: {}",
                    envelope.getEventType(),
                    product.getId(),
                    publishResult.getMessageId());

        } catch (JsonProcessingException e) {
            LOG.error("Error while publishing product event {}", e.getMessage());
        }
    }

}

package br.com.jck.aws_project01.service;

import br.com.jck.aws_project01.enums.EventType;
import br.com.jck.aws_project01.model.Product;
import br.com.jck.aws_project01.sns.Envelope;
import br.com.jck.aws_project01.sns.ProductEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductSnsPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ProductSnsPublisher.class);

    private AmazonSNS snsClient;
    private Topic productTopicEvent;
    private ObjectMapper objectMapper;

    public ProductSnsPublisher(AmazonSNS snsClient, @Qualifier("productEventsTopic")Topic productTopicEvent, ObjectMapper objectMapper){
        this.snsClient = snsClient;
        this.productTopicEvent = productTopicEvent;
        this.objectMapper = objectMapper;
    }

    public void publishProductEvent(Product product, EventType eventType, String userName){
        ProductEvent productEvent =  new ProductEvent();
        productEvent.setProductId(product.getId());
        productEvent.setCode(product.getCode());
        productEvent.setUserName(userName);

        Envelope envelope = new Envelope();
        envelope.setEventType(eventType);

        try {
            envelope.setData(objectMapper.writeValueAsString(productEvent));
            snsClient.publish(
                    productTopicEvent.getTopicArn(),
                    objectMapper.writeValueAsString(envelope));
        } catch (JsonProcessingException e) {
            LOG.error("Failed to create product event message");
        }
    }
}

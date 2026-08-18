package com.codingshuttle.razorpay.common.config;

//package com.codingshuttle.razorpay.common.config;

import com.codingshuttle.razorpay.common.enums.EventAggregateType;
import jakarta.annotation.PostConstruct;//this is gwrefsdo
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProperties {

    private Map<String, String> topics = new HashMap<>();

    public String topicFor(EventAggregateType aggregateType) {

        String topic = topics.get(aggregateType.name().toLowerCase());

        if (topic == null) {
            throw new IllegalStateException(
                    "No Kafka topic is configured for aggregateType: " + aggregateType
            );
        }

        return topic;
    }

    @PostConstruct
    public void printTopics() {
        System.out.println("******** KAFKA CONFIG ********");
        System.out.println("topics = " + topics);
        System.out.println("*******************************");
    }
}

//import com.codingshuttle.razorpay.common.enums.EventAggregateType;
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
//@Configuration
//@ConfigurationProperties(prefix = "app.kafka")
//@Getter
//@Setter
//public class KafkaProperties {
//
//    private Map<String, String> topics = new HashMap<>();
//
////    public String topicFor(EventAggregateType aggregateType) {
////        String topic = topics.get(aggregateType.name().toLowerCase());
////        if (topic == null) {
////            throw new IllegalStateException("No Kafka topic is configured for aggregateType: "+aggregateType);
////        }
////        return topic;
//    }
//
//}
//}

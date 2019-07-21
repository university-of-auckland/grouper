package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 *
 * Publishes Grouper events to kafka server as JSON strings
 *
 */
public class EsbKafkaPublisher extends EsbListenerBase {

    private static final Log LOG = GrouperUtil.getLog(EsbKafkaPublisher.class);

    private static final String CLIENT_ID = "grouper-producer";

    private static String brokerList;

    private static String topicName;

    private static Integer retries;

    @Override
    public boolean dispatchEvent(String eventJsonString, String consumerName) {
        String propNamePrefix = "changeLog.consumer." + consumerName + ".publisher.kafka.";
        if (brokerList == null) {
            brokerList = GrouperLoaderConfig.retrieveConfig().propertyValueString(propNamePrefix + "servers");
            LOG.debug("brokerlist " + brokerList);
        }
        if (topicName == null) {
            topicName = GrouperLoaderConfig.retrieveConfig().propertyValueString(propNamePrefix + "topic");
            LOG.debug("topicName " + topicName);
        }
        if (retries == null) {
            retries = GrouperLoaderConfig.retrieveConfig().propertyValueInt(propNamePrefix + "retry", 0);
            LOG.debug("retires " + retries);
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, eventJsonString);
        try {
            producer.send(record, (metadata, exception) -> {
                if (metadata != null) {
                    LOG.info("Sent message " + eventJsonString + ", meta(offset=" + metadata.offset()+")");
                } else {
                    LOG.error("Error happens on sending message to kafka server: " + eventJsonString, exception);
                }
            });
        }finally {
            producer.flush();
            producer.close();
        }
        return true;

    }

    @Override
    public void disconnect() {
        // Unused, client does not maintain a persistent connection in this version

    }

}

package za.co.sbg.tag.skeleton.it.config;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.jms.endpoint.JmsEndpoint;

public final class SkeletonEndpointConfig {

    public static final long MESSAGE_TIMEOUT = 15_000L;

    public static final String INCOMING_JSON_DESTINATION =
            "command.za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1";
    public static final String INCOMING_FIXED_LENGTH_DESTINATION =
            "command.za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1";
    public static final String OUTGOING_XML_DESTINATION =
            "event.za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1";
    public static final String OUTGOING_TEXT_DESTINATION =
            "event.za.co.sbg.tag.skeleton.messages.events.OutgoingTextMessageV1";
    public static final String POISON_DESTINATION = "TAG.POISON";

    public static final HttpClient tagClient = new HttpClientBuilder()
            .requestUrl("http://localhost:" + SkeletonContainerConfig.appContainer.getFirstMappedPort())
            .build();

    public static final JmsEndpoint incomingJson =
            SkeletonContainerConfig.activeMQContainer.createJmsEndpoint(INCOMING_JSON_DESTINATION);

    public static final JmsEndpoint incomingFixedLength =
            SkeletonContainerConfig.activeMQContainer.createJmsEndpoint(INCOMING_FIXED_LENGTH_DESTINATION);

    public static final JmsEndpoint outgoingXml = createVirtualTopicEndpoint(OUTGOING_XML_DESTINATION);
    public static final JmsEndpoint outgoingText = createVirtualTopicEndpoint(OUTGOING_TEXT_DESTINATION);
    public static final JmsEndpoint poison =
            SkeletonContainerConfig.activeMQContainer.createJmsEndpoint(POISON_DESTINATION);

    private SkeletonEndpointConfig() {
    }

    private static JmsEndpoint createVirtualTopicEndpoint(String topicName) {
        String virtualQueueName = "Tag.Virtual.skeleton-it." + topicName;
        var connectionFactory = SkeletonContainerConfig.activeMQContainer.createConnectionFactory();
        try (var connection = connectionFactory.createConnection();
             var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             var consumer = session.createConsumer(session.createQueue(virtualQueueName))) {
            connection.start();
        } catch (JMSException exception) {
            throw new ExceptionInInitializerError(exception);
        }
        return SkeletonContainerConfig.activeMQContainer.createJmsEndpoint(virtualQueueName);
    }
}

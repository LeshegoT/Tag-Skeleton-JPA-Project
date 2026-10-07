package za.co.sbg.tag.skeleton.it.poison;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.message.JmsMessageHeaders;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import za.co.sbg.tag.skeleton.it.config.SkeletonContainerConfig;
import za.co.sbg.tag.skeleton.it.config.SkeletonEndpointConfig;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;

import static org.citrusframework.actions.PurgeEndpointAction.Builder.purgeEndpoints;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

@MicroShedTest
@SharedContainerConfig(SkeletonContainerConfig.class)
@ExtendWith(CitrusExtension.class)
class PoisonMessageIT {

    private static final String MALFORMED_JSON = "{not-valid-json";
    private static final String INVALID_FIXED_LENGTH =
            "Fixed integration   20260922135000+02:00ABCDEF";

    @BeforeEach
    void purgePoisonQueue(@CitrusResource TestCaseRunner runner) {
        runner.run(purgeEndpoints().endpoint(SkeletonEndpointConfig.poison));
    }

    @Test
    @CitrusTest
    void givenMalformedJsonMessage_whenConsumed_thenForwardOriginalMessageToPoisonQueue(
            @CitrusResource TestCaseRunner runner) {
        sendInvalidMessage(
                runner,
                SkeletonEndpointConfig.incomingJson,
                IncomingJsonMessageV1.class.getCanonicalName(),
                MALFORMED_JSON);

        receivePoisonMessage(runner, MALFORMED_JSON);
    }

    @Test
    @CitrusTest
    void givenInvalidFixedLengthNumber_whenConsumed_thenForwardOriginalMessageToPoisonQueue(
            @CitrusResource TestCaseRunner runner) {
        sendInvalidMessage(
                runner,
                SkeletonEndpointConfig.incomingFixedLength,
                IncomingFixedLengthDelimitedMessageV1.class.getCanonicalName(),
                INVALID_FIXED_LENGTH);

        receivePoisonMessage(runner, INVALID_FIXED_LENGTH);
    }

    private static void sendInvalidMessage(
            TestCaseRunner runner,
            JmsEndpoint endpoint,
            String messageType,
            String body) {
        runner.run(send()
                .endpoint(endpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .header(JmsMessageHeaders.TYPE, messageType)
                .body(body));
    }

    private static void receivePoisonMessage(TestCaseRunner runner, String expectedBody) {
        runner.run(receive()
                .endpoint(SkeletonEndpointConfig.poison)
                .message()
                .type(MessageType.PLAINTEXT)
                .body(expectedBody)
                .timeout(SkeletonEndpointConfig.MESSAGE_TIMEOUT));
    }
}

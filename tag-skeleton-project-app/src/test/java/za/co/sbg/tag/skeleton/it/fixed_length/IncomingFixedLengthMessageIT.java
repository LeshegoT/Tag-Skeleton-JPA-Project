package za.co.sbg.tag.skeleton.it.fixed_length;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.jms.message.JmsMessageHeaders;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import za.co.sbg.tag.skeleton.it.config.SkeletonContainerConfig;
import za.co.sbg.tag.skeleton.it.config.SkeletonEndpointConfig;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingTextMessageV1;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

@MicroShedTest
@SharedContainerConfig(SkeletonContainerConfig.class)
@ExtendWith(CitrusExtension.class)
class IncomingFixedLengthMessageIT {

    @Test
    @CitrusTest
    void givenIncomingFixedLengthMessage_whenConsumed_thenPublishTextEvent(
            @CitrusResource TestCaseRunner runner) {
        runner.run(send()
                .endpoint(SkeletonEndpointConfig.incomingFixedLength)
                .message()
                .type(MessageType.PLAINTEXT)
                .header(JmsMessageHeaders.TYPE,
                        IncomingFixedLengthDelimitedMessageV1.class.getCanonicalName())
                .body(new Resources.ClasspathResource(
                        "it/fixed-length/IncomingFixedLengthDelimitedMessageV1.txt")));

        runner.run(receive()
                .endpoint(SkeletonEndpointConfig.outgoingText)
                .message()
                .type(MessageType.PLAINTEXT)
                .header(JmsMessageHeaders.TYPE, OutgoingTextMessageV1.class.getCanonicalName())
                .body(new Resources.ClasspathResource(
                        "it/fixed-length/ExpectedOutgoingTextMessageV1.txt"))
                .timeout(SkeletonEndpointConfig.MESSAGE_TIMEOUT));
    }
}

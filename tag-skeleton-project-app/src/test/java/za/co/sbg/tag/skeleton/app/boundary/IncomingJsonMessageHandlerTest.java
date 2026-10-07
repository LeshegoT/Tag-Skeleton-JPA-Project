package za.co.sbg.tag.skeleton.app.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.sbg.tag.platform.messaging.lifecycle.bus.MessageBus;
import za.co.sbg.tag.platform.messaging.message.ContextualizedMessage;
import za.co.sbg.tag.platform.messaging.message.MessageContext;
import za.co.sbg.tag.skeleton.app.controller.SkeletonController;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomingJsonMessageHandlerTest {

    @Mock
    SkeletonController controller;

    @Mock
    MessageBus messageBus;

    @InjectMocks
    IncomingJsonMessageHandler handler;

    @Test
    void givenCountryHeader_whenHandling_thenProcessWithCountryAndPublish() {
        var incoming = IncomingJsonMessageV1.builder().name("Json name").build();
        var outgoing = OutgoingXmlMessageV1.builder().name("Json name").build();
        var contextualizedMessage = ContextualizedMessage.<IncomingJsonMessageV1>builder()
                .message(incoming)
                .context(MessageContext.builder()
                        .messageHeader("Country_ISO", "ZAF")
                        .build())
                .build();
        when(controller.processJson(incoming, "ZAF")).thenReturn(outgoing);

        handler.handle(contextualizedMessage);

        verify(controller).processJson(incoming, "ZAF");
        verify(messageBus).publish(outgoing);
    }

    @Test
    void givenNoCountryHeader_whenHandling_thenProcessWithNullCountryAndPublish() {
        var incoming = IncomingJsonMessageV1.builder().name("Json name").build();
        var outgoing = OutgoingXmlMessageV1.builder().name("Json name").build();
        var contextualizedMessage = ContextualizedMessage.<IncomingJsonMessageV1>builder()
                .message(incoming)
                .context(MessageContext.builder().build())
                .build();
        when(controller.processJson(incoming, null)).thenReturn(outgoing);

        handler.handle(contextualizedMessage);

        verify(controller).processJson(incoming, null);
        verify(messageBus).publish(outgoing);
    }
}

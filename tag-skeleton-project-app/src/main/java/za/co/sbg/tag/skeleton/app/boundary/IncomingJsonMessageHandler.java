package za.co.sbg.tag.skeleton.app.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import za.co.sbg.tag.platform.messaging.lifecycle.bus.MessageBus;
import za.co.sbg.tag.platform.messaging.lifecycle.processing.Handle;
import za.co.sbg.tag.platform.messaging.lifecycle.processing.MessageHandler;
import za.co.sbg.tag.platform.messaging.message.ContextualizedMessage;

import za.co.sbg.tag.skeleton.app.controller.SkeletonController;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;

@ApplicationScoped
@MessageHandler
public class IncomingJsonMessageHandler {

    @Inject
    SkeletonController skeletonController;

    @Inject
    MessageBus messageBus;

    @Handle
    public void handle(
            ContextualizedMessage<IncomingJsonMessageV1> contextualizedMessage) {

        IncomingJsonMessageV1 incoming =
                contextualizedMessage.getMessage();

        var headers =
                contextualizedMessage
                        .getContext()
                        .getMessageHeaders();

        String countryIso = null;

        if (headers.containsKey("Country_ISO")) {
            countryIso =
                    String.valueOf(
                            headers.get("Country_ISO").getValue()
                    );
        }

        var outgoing =
                skeletonController.processJson(
                        incoming,
                        countryIso
                );

        messageBus.publish(outgoing);
    }
}

package za.co.sbg.tag.skeleton.it.json;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.jms.message.JmsMessageHeaders;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import za.co.sbg.tag.skeleton.it.config.SkeletonContainerConfig;
import za.co.sbg.tag.skeleton.it.config.SkeletonEndpointConfig;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

@MicroShedTest
@SharedContainerConfig(SkeletonContainerConfig.class)
@ExtendWith(CitrusExtension.class)
class IncomingJsonMessageIT {

    private static final String REQUEST_FIXTURE = "it/json/IncomingJsonMessageV1.json";
    private static final String RESPONSE_FIXTURE = "it/json/ExpectedOutgoingXmlMessageV1.xml";

    @Test
    @CitrusTest
    void givenIncomingJsonMessages_whenConsumed_thenUpdateSingleRowAndPublishXmlEvents(
            @CitrusResource TestCaseRunner runner,
            @CitrusResource TestContext context) {
        String name = uniqueName("jms");
        String firstReference = "first-" + UUID.randomUUID();
        String secondReference = "second-" + UUID.randomUUID();

        sendIncomingJson(runner, firstReference, name,
                "2026-09-22T13:45:00+02:00[Africa/Johannesburg]", 17);
        receiveOutgoingXmlFixture(runner);
        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select is_updated from skeleton.skeleton_name where name = '" + name + "'")
                .extract("is_updated", "firstUpdated"));
        Assertions.assertEquals("false", context.getVariable("firstUpdated"));

        sendIncomingJson(runner, secondReference, name,
                "2026-09-23T08:20:00+02:00[Africa/Johannesburg]", 29);
        receiveOutgoingXmlFixture(runner);
        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select count(*) as name_count, bool_and(is_updated) as is_updated "
                        + "from skeleton.skeleton_name where name = '" + name + "'")
                .validate("name_count", "1")
                .validate("is_updated", "true"));
    }

    @Test
    @CitrusTest
    void givenUsaJsonMessage_whenConsumed_thenConvertDateToNewYorkTime(
            @CitrusResource TestCaseRunner runner) {
        String name = uniqueName("usa-jms");
        String date = "2026-09-22T13:45:00+02:00[Africa/Johannesburg]";
        String expectedDate = ZonedDateTime.parse(date)
                .withZoneSameInstant(ZoneId.of("America/New_York"))
                .toString();

        sendIncomingJson(
                runner,
                "usa-" + UUID.randomUUID(),
                name,
                date,
                expectedDate,
                31,
                "USA");
        receiveOutgoingXmlFixture(runner);
        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select count(*) as name_count from skeleton.skeleton_name "
                        + "where name = '" + name + "'")
                .validate("name_count", "1"));
    }

    private static void sendIncomingJson(
            TestCaseRunner runner,
            String reference,
            String name,
            String date,
            int random) {
        sendIncomingJson(
                runner,
                reference,
                name,
                date,
                ZonedDateTime.parse(date).toString(),
                random,
                "ZAF");
    }

    private static void sendIncomingJson(
            TestCaseRunner runner,
            String reference,
            String name,
            String date,
            String expectedDate,
            int random,
            String countryIso) {
        runner.variable("reference", reference);
        runner.variable("name", name);
        runner.variable("date", date);
        runner.variable("expectedDate", expectedDate);
        runner.variable("random", random);

        runner.run(send()
                .endpoint(SkeletonEndpointConfig.incomingJson)
                .message()
                .type(MessageType.JSON)
                .header(JmsMessageHeaders.TYPE, IncomingJsonMessageV1.class.getCanonicalName())
                .header("Country_ISO", countryIso)
                .body(new Resources.ClasspathResource(REQUEST_FIXTURE)));
    }

    private static void receiveOutgoingXmlFixture(TestCaseRunner runner) {
        runner.run(receive()
                .endpoint(SkeletonEndpointConfig.outgoingXml)
                .message()
                .type(MessageType.XML)
                .header(JmsMessageHeaders.TYPE, OutgoingXmlMessageV1.class.getCanonicalName())
                .body(new Resources.ClasspathResource(RESPONSE_FIXTURE))
                .timeout(SkeletonEndpointConfig.MESSAGE_TIMEOUT));
    }

    private static String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}

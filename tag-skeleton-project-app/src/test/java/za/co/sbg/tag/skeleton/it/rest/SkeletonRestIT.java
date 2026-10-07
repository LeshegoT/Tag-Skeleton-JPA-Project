package za.co.sbg.tag.skeleton.it.rest;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.jms.message.JmsMessageHeaders;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.message.MessageType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import za.co.sbg.tag.skeleton.it.config.SkeletonContainerConfig;
import za.co.sbg.tag.skeleton.it.config.SkeletonEndpointConfig;
import za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1;

import java.util.UUID;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;
import static org.citrusframework.validation.xml.XpathMessageValidationContext.Builder.xpath;

@MicroShedTest
@SharedContainerConfig(SkeletonContainerConfig.class)
@ExtendWith(CitrusExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SkeletonRestIT {

    private static final String ORDERED_NAME = uniqueName("rest");
    private static final String CREATED_DATE = "2026-09-22T11:50:00Z";
    private static final String UPDATED_DATE = "2026-09-23T09:00:00Z";

    @Test
    @CitrusTest
    @Order(1)
    void givenApplicationStartup_thenFlywayMigrationIsApplied(
            @CitrusResource TestCaseRunner runner) {

        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select count(*) as migration_count "
                        + "from skeleton.flyway_schema_history "
                        + "where success = true "
                        + "and script = 'V20261006_181024__create_skeleton_name.sql'")
                .validate("migration_count", "1"));
    }

    @Test
    @CitrusTest
    @Order(2)
    void givenNewName_whenCreating_thenPersistAndPublishEvent(
            @CitrusResource TestCaseRunner runner) {

        createName(runner, ORDERED_NAME, CREATED_DATE, false);
        receiveOutgoingEvent(runner, ORDERED_NAME);
        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select count(*) as name_count, bool_and(is_updated) as is_updated "
                        + "from skeleton.skeleton_name where name = '" + ORDERED_NAME + "'")
                .validate("name_count", "1")
                .validate("is_updated", "false"));
    }

    @Test
    @CitrusTest
    @Order(3)
    void givenPersistedName_whenRetrievingUnupdatedNames_thenReturnSuccess(
            @CitrusResource TestCaseRunner runner) {

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .get("/tag/skeleton/name?isUpdated=false")
                .message()
                .accept(MediaType.APPLICATION_JSON_VALUE));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .type(MessageType.JSON));
    }

    @Test
    @CitrusTest
    @Order(4)
    void givenPersistedName_whenUpdating_thenUpdateAndPublishEvent(
            @CitrusResource TestCaseRunner runner) {

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .put("/tag/skeleton/name")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\":\"" + ORDERED_NAME + "\",\"date\":\"" + UPDATED_DATE + "\"}"));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.OK)
                .message());
        receiveOutgoingEvent(runner, ORDERED_NAME);
        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select is_updated from skeleton.skeleton_name where name = '" + ORDERED_NAME + "'")
                .validate("is_updated", "true"));
    }

    @Test
    @CitrusTest
    @Order(5)
    void givenPersistedName_whenDeleting_thenDeleteAndPublishEvent(
            @CitrusResource TestCaseRunner runner) {

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .delete("/tag/skeleton/name?name=" + ORDERED_NAME));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.NO_CONTENT)
                .message());
        receiveOutgoingEvent(runner, ORDERED_NAME);
        assertNameCount(runner, ORDERED_NAME, 0);
    }

    @Test
    @CitrusTest
    void givenMissingName_whenUpdating_thenReturnNotFoundWithoutCreatingARecord(
            @CitrusResource TestCaseRunner runner) {
        String name = uniqueName("missing-update");

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .put("/tag/skeleton/name")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\":\"" + name + "\",\"date\":\"2026-09-23T09:00:00Z\"}"));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.NOT_FOUND)
                .message()
                .body("Name not found"));
        assertNameCount(runner, name, 0);
    }

    @Test
    @CitrusTest
    void givenMissingName_whenDeleting_thenReturnNotFound(
            @CitrusResource TestCaseRunner runner) {
        String name = uniqueName("missing-delete");

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .delete("/tag/skeleton/name?name=" + name));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.NOT_FOUND)
                .message()
                .body("Name not found"));
        assertNameCount(runner, name, 0);
    }

    @Test
    @CitrusTest
    void givenSameNameTwice_whenCreating_thenUpdateTheExistingRecord(
            @CitrusResource TestCaseRunner runner) {
        String name = uniqueName("rest-upsert");

        createName(runner, name, "2026-09-22T11:50:00Z", false);
        receiveOutgoingEvent(runner, name);
        createName(runner, name, "2026-09-24T15:30:00Z", true);
        receiveOutgoingEvent(runner, name);

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
    void givenInvalidDate_whenCreatingName_thenReturnServerErrorWithoutPersisting(
            @CitrusResource TestCaseRunner runner) {
        String name = uniqueName("invalid-date");

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .post("/tag/skeleton/name?name=" + name + "&date=not-a-date"));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.INTERNAL_SERVER_ERROR)
                .message());

        assertNameCount(runner, name, 0);
    }

    @Test
    @CitrusTest
    void givenUnsupportedMediaType_whenUpdatingName_thenReturnUnsupportedMediaType(
            @CitrusResource TestCaseRunner runner) {
        String name = uniqueName("wrong-content-type");

        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .put("/tag/skeleton/name")
                .message()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .body("name=" + name));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .message());

        assertNameCount(runner, name, 0);
    }

    private static void createName(
            TestCaseRunner runner,
            String name,
            String date,
            boolean expectedUpdated) {
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .send()
                .post("/tag/skeleton/name?name=" + name + "&date=" + date)
                .message()
                .accept(MediaType.APPLICATION_JSON_VALUE));
        runner.run(http().client(SkeletonEndpointConfig.tagClient)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .type(MessageType.JSON)
                .validate(jsonPath()
                        .expression("$.name", name)
                        .expression("$.updated", expectedUpdated)));
    }

    private static void assertNameCount(TestCaseRunner runner, String name, int expectedCount) {
        runner.run(sql()
                .dataSource(SkeletonContainerConfig.dbContainer.createDataSource())
                .query()
                .statement("select count(*) as name_count from skeleton.skeleton_name "
                        + "where name = '" + name + "'")
                .validate("name_count", Integer.toString(expectedCount)));
    }

    private static void receiveOutgoingEvent(TestCaseRunner runner, String name) {
        runner.run(receive()
                .endpoint(SkeletonEndpointConfig.outgoingXml)
                .message()
                .type(MessageType.XML)
                .header(JmsMessageHeaders.TYPE, OutgoingXmlMessageV1.class.getCanonicalName())
                .validate(xpath()
                        .expression("/OutgoingXmlMessageV1/name", name)
                        .expression("/OutgoingXmlMessageV1/date", "@ignore@"))
                .timeout(SkeletonEndpointConfig.MESSAGE_TIMEOUT));
    }

    private static String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}

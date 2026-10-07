package za.co.sbg.tag.skeleton.it.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import za.co.sbg.tag.test.integration.container.TagActiveMQContainer;
import za.co.sbg.tag.test.integration.container.TagPostgresContainer;
import za.co.sbg.tag.test.integration.util.TagAppContainerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SkeletonContainerConfig implements SharedContainerConfiguration {

    private static final int APP_PORT = 8080;
    private static final String DATABASE_ALIAS = "dbhost";
    private static final String ACTIVE_MQ_ALIAS = "amqhost";

    public static final Network network = Network.newNetwork();

    @Container
    public static final ApplicationContainer appContainer =
            TagAppContainerFactory.createTagAppContainer(APP_PORT)
                    .withEnv("APP_PORT", Integer.toString(APP_PORT))
                    .withEnv("DB_HOST", DATABASE_ALIAS)
                    .withEnv("DB_PORT", Integer.toString(TagPostgresContainer.PORT))
                    .withEnv("DB_NAME", "PACManDB")
                    .withEnv("DB_USER", "postgres")
                    .withEnv("DB_PASSWORD", "admin")
                    .withEnv("BROKER_URL", "tcp://" + ACTIVE_MQ_ALIAS + ":" + TagActiveMQContainer.PORT)
                    .withEnv("BROKER_USER", "admin")
                    .withEnv("BROKER_PASSWORD", "admin")
                    .withNetwork(network)
                    .waitingFor(Wait.forHttp("/tag/skeleton/name?isUpdated=false")
                            .forPort(APP_PORT)
                            .forStatusCodeMatching(code -> code >= 200 && code <= 299)
                            .withStartupTimeout(Duration.ofSeconds(180)));

    //health endpoint exists by default
    @Container
    public static final TagPostgresContainer dbContainer =
            new TagPostgresContainer(TagAppContainerFactory.POSTGRES_DOCKER_IMAGE_NAME)
                    .withClasspathResourceMapping(
                            "it-docker/skeleton-init.sql",
                            "/docker-entrypoint-initdb.d/skeleton-init.sql",
                            BindMode.READ_ONLY)
                    .withNetwork(network)
                    .withNetworkAliases(DATABASE_ALIAS)
                    .withDatabase("PACManDB")
                    .withUsername("postgres")
                    .withPassword("admin");

    @Container
    public static final TagActiveMQContainer activeMQContainer =
            new TagActiveMQContainer(TagAppContainerFactory.ACTIVE_MQ_DOCKER_IMAGE_NAME)
                    .withNetwork(network)
                    .withNetworkAliases(ACTIVE_MQ_ALIAS);

    @SneakyThrows
    @Override
    public void startContainers() {
        CompletableFuture.allOf(
                CompletableFuture.runAsync(dbContainer::start),
                CompletableFuture.runAsync(activeMQContainer::start)
        ).get();
        appContainer.start();

        log.info("Application port: {}", appContainer.getFirstMappedPort());
        log.info("ActiveMQ port: {}", activeMQContainer.getFirstMappedPort());
        log.info("PostgreSQL port: {}", dbContainer.getFirstMappedPort());
    }
}

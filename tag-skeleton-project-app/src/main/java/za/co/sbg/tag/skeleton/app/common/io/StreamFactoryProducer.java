package za.co.sbg.tag.skeleton.app.common.io;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.beanio.StreamFactory;

@ApplicationScoped
public class StreamFactoryProducer {

    @Produces
    @ApplicationScoped
    public StreamFactory streamFactory(
            FixedLengthStreamBuilderProvider provider) {

        StreamFactory factory = StreamFactory.newInstance();

        factory.define(provider.createStreamBuilder());

        return factory;
    }
}
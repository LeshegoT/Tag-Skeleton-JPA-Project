package za.co.sbg.tag.skeleton.app.common.io;

import org.beanio.BeanReader;
import org.beanio.StreamFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.sbg.tag.skeleton.messages.beanio.IncomingFixedLengthRecord;

import java.io.InputStream;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixedLengthReaderTest {

    @Mock
    private StreamFactory streamFactory;

    @Mock
    private BeanReader beanReader;

    @Mock
    private InputStream content;

    @InjectMocks
    private FixedLengthReader reader;

    @Test
    void givenValidContent_whenReading_thenReturnRecordAndCloseBeanReader() {
        var expected = IncomingFixedLengthRecord.builder()
                .name("Fixed name")
                .date("20260922135000+02:00")
                .random("000042")
                .build();
        when(streamFactory.createReader(
                eq(FixedLengthStreamBuilderProvider.FIXED_LENGTH_STREAM),
                any(Reader.class)
        )).thenReturn(beanReader);
        when(beanReader.read()).thenReturn(expected);

        var result = reader.read(content);

        assertSame(expected, result);
        verify(beanReader).close();
    }

    @Test
    void givenBeanIoFailure_whenReading_thenCloseBeanReaderAndPropagateFailure() {
        var failure = new IllegalArgumentException("Invalid fixed-length record");
        when(streamFactory.createReader(
                eq(FixedLengthStreamBuilderProvider.FIXED_LENGTH_STREAM),
                any(Reader.class)
        )).thenReturn(beanReader);
        when(beanReader.read()).thenThrow(failure);

        var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> reader.read(content)
        );

        assertSame(failure, thrown);
        verify(beanReader).close();
    }
}

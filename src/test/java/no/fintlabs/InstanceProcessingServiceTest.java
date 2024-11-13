package no.fintlabs;

import no.fintlabs.exception.InstanceFieldNotFoundException;
import no.fintlabs.exception.ValueConvertingKeyNotFoundException;
import no.fintlabs.exception.ValueConvertingNotFoundException;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.kafka.configuration.ConfigurationMappingRequestProducerService;
import no.fintlabs.kafka.error.InstanceMappingErrorEventProducerService;
import no.fintlabs.mapping.InstanceMappingService;
import no.fintlabs.model.configuration.ObjectMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;


class InstanceProcessingServiceTest {

    @InjectMocks
    private InstanceProcessingService instanceProcessingService;

    @Mock
    private InstanceMappedEventProducerService instanceMappedEventProducerService;

    @Mock
    private ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;

    @Mock
    private ConfigurationMappingRequestProducerService configurationMappingRequestProducerService;

    @Mock
    private InstanceMappingService instanceMappingService;

    @Mock
    private InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessSuccessfully() {
        Long integrationId = 123L;
        Long configurationId = 456L;
        InstanceFlowHeaders instanceFlowHeaders = InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .correlationId(UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"))
                .integrationId(integrationId)
                .build();

        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceObject instance = mock(InstanceObject.class);
        ConsumerRecord<String, InstanceObject> consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn(instance);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);
        when(flytConsumerRecord.getConsumerRecord()).thenReturn(consumerRecord);

        Map mappedInstance = mock(Map.class);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instance)).thenReturn(mappedInstance);

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappedEventProducerService).publish(ArgumentMatchers.argThat(
                ifh -> ifh.getSourceApplicationId() == 1L
                        && ifh.getCorrelationId().equals(UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"))
                        && ifh.getIntegrationId().equals(integrationId)
                        && ifh.getConfigurationId().equals(configurationId)
        ), ArgumentMatchers.same(mappedInstance));
    }

    @Test
    void givenNoActiveConfigurationIdShouldPublishConfigurationNotFoundErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.empty());

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService, times(1))
                .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders);
    }

    @Test
    void givenNoConfigurationShouldPublishConfigurationNotFoundErrorEvent() {
        Long integrationId = 123L;
        Long configurationId = 456L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.empty());

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService, times(1))
                .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders);
    }

    @Test
    void givenValueConvertingNotFoundExceptionShouldPublishMissingValueConvertingErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        Long configurationId = 456L;
        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);
        InstanceObject instanceObject = mock(InstanceObject.class);
        ConsumerRecord consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn(instanceObject);
        when(flytConsumerRecord.getConsumerRecord()).thenReturn(consumerRecord);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
                .thenThrow(new ValueConvertingNotFoundException(678L));

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService, times(1))
                .publishMissingValueConvertingErrorEvent(instanceFlowHeaders, 678L);
    }

    @Test
    void givenValueConvertingKeyNotFoundExceptionShouldPublishMissingValueConvertingKeyErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        Long configurationId = 456L;
        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);
        InstanceObject instanceObject = mock(InstanceObject.class);
        ConsumerRecord consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn(instanceObject);
        when(flytConsumerRecord.getConsumerRecord()).thenReturn(consumerRecord);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
                .thenThrow(new ValueConvertingKeyNotFoundException(678L, "testKey"));

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService, times(1))
                .publishMissingValueConvertingKeyErrorEvent(instanceFlowHeaders, 678L, "testKey");
    }

    @Test
    void givenInstanceFieldNotFoundExceptionShouldPublishInstanceFieldNotFoundErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        Long configurationId = 456L;
        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);
        InstanceObject instanceObject = mock(InstanceObject.class);
        ConsumerRecord consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn(instanceObject);
        when(flytConsumerRecord.getConsumerRecord()).thenReturn(consumerRecord);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
                .thenThrow(new InstanceFieldNotFoundException("testKey"));

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService, times(1))
                .publishInstanceFieldNotFoundErrorEvent(instanceFlowHeaders, "testKey");
    }

    @Test
    void givenRuntimeExceptionShouldPublishGeneralSystemErrorEvent() {
        Long integrationId = 123L;

        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenThrow(new RuntimeException());

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService, times(1))
                .publishGeneralSystemErrorEvent(instanceFlowHeaders);
    }

}

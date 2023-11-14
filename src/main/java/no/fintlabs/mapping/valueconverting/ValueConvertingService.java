package no.fintlabs.mapping.valueconverting;

import no.fintlabs.exception.ValueConvertingKeyNotFoundException;
import no.fintlabs.exception.ValueConvertingNotFoundException;
import no.fintlabs.kafka.configuration.ValueConvertingRequestProducerService;
import no.fintlabs.mapping.InstanceReferenceService;
import no.fintlabs.mapping.valueconverting.converters.TextConvertingService;
import no.fintlabs.model.instance.InstanceObject;
import no.fintlabs.model.valueconverting.ValueConverting;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ValueConvertingService {

    private final ValueConvertingRequestProducerService valueConvertingRequestProducerService;
    private final InstanceReferenceService instanceReferenceService;
    private final ValueConvertingReferenceService valueConvertingReferenceService;

    private final TextConvertingService textConvertingService;

    public ValueConvertingService(
            ValueConvertingRequestProducerService valueConvertingRequestProducerService,
            InstanceReferenceService instanceReferenceService,
            ValueConvertingReferenceService valueConvertingReferenceService,
            TextConvertingService textConvertingService
    ) {
        this.valueConvertingRequestProducerService = valueConvertingRequestProducerService;
        this.instanceReferenceService = instanceReferenceService;
        this.valueConvertingReferenceService = valueConvertingReferenceService;
        this.textConvertingService = textConvertingService;
    }

    public String convertValue(
            String mappingString,
            Map<String, String> instanceValuePerKey,
            InstanceObject[] selectedCollectionObjectsPerKey
    ) {
        String instanceValue = instanceReferenceService.getFirstInstanceValue(
                mappingString,
                instanceValuePerKey,
                selectedCollectionObjectsPerKey
        );
        Long valueConvertingId = valueConvertingReferenceService.getFirstValueConverterId(mappingString);
        if (valueConvertingId < 0) {
            return convertProgramatically(valueConvertingId, instanceValue);
        }
        ValueConverting valueConverting = valueConvertingRequestProducerService.get(valueConvertingId)
                .orElseThrow(() -> new ValueConvertingNotFoundException(valueConvertingId));
        Map<String, String> convertingMap = valueConverting.getConvertingMap();
        if (!convertingMap.containsKey(instanceValue)) {
            throw new ValueConvertingKeyNotFoundException(valueConvertingId, instanceValue);
        }
        return valueConverting.getConvertingMap().get(instanceValue);
    }

    private String convertProgramatically(long valueConvertingId, String value) {
        if (valueConvertingId == -1) {
            return textConvertingService.toUpperCase(value);
        }
        if (valueConvertingId == -2) {
            return textConvertingService.toLowerCase(value);
        }
        if (valueConvertingId == -3) {
            return textConvertingService.toLowerCaseWithFirstLetterUpperCase(value);
        }
        throw new IllegalArgumentException();
    }

}

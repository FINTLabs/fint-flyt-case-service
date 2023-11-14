package no.fintlabs.mapping.valueconverting.converters;

import org.springframework.stereotype.Service;

@Service
public class TextConvertingService {

    public String toUpperCase(String value) {
        if (value == null) {
            return null;
        }
        return value.toUpperCase();
    }

    public String toLowerCase(String value) {
        if (value == null) {
            return null;
        }
        return value.toLowerCase();
    }

    public String toLowerCaseWithFirstLetterUpperCase(String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

}

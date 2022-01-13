package no.fintlabs.model.configuration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public
class RecordConfiguration {
    private List<Field> fields = new ArrayList<>();
}
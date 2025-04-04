package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DartThrow {
    private Dart target;
    private Dart result;
}
package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dart {
    private DartBoardSection section;
    private DartboardSectionArea area;

    public int getScore() {
        return section.getScore(area);
    }

    public Dart(Dart dart) {
        this(dart.getSection(), dart.getArea());
    }
}

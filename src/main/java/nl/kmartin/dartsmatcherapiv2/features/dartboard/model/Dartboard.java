package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class Dartboard {
    private final List<DartBoardSection> sections;
    private final List<DartboardSectionAreaDimen> areaDimensions;

    public Dartboard() {
        // Sections starting at 6 counting counterclockwise. Reasoning is that for polar planes
        // the convention is to start at 0 degrees on the x-axis and then increases counterclockwise.
        this.sections = List.of(
                DartBoardSection.SIX,
                DartBoardSection.THIRTEEN,
                DartBoardSection.FOUR,
                DartBoardSection.EIGHTEEN,
                DartBoardSection.ONE,
                DartBoardSection.TWENTY,
                DartBoardSection.FIVE,
                DartBoardSection.TWELVE,
                DartBoardSection.NINE,
                DartBoardSection.FOURTEEN,
                DartBoardSection.ELEVEN,
                DartBoardSection.EIGHT,
                DartBoardSection.SIXTEEN,
                DartBoardSection.SEVEN,
                DartBoardSection.NINETEEN,
                DartBoardSection.THREE,
                DartBoardSection.SEVENTEEN,
                DartBoardSection.TWO,
                DartBoardSection.FIFTEEN,
                DartBoardSection.TEN,
                DartBoardSection.SIX
        );

        this.areaDimensions = List.of(
                new DartboardSectionAreaDimen(DartboardSectionArea.DOUBLE_BULL, 0, 7),
                new DartboardSectionAreaDimen(DartboardSectionArea.SINGLE_BULL, 7, 17),
                new DartboardSectionAreaDimen(DartboardSectionArea.INNER_SINGLE, 17, 97),
                new DartboardSectionAreaDimen(DartboardSectionArea.TRIPLE, 97, 107),
                new DartboardSectionAreaDimen(DartboardSectionArea.OUTER_SINGLE, 107, 160),
                new DartboardSectionAreaDimen(DartboardSectionArea.DOUBLE, 160, 170),
                new DartboardSectionAreaDimen(DartboardSectionArea.MISS, 170, Integer.MAX_VALUE)
        );
    }
}
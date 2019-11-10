package no.ntnu.imt3281.ludo.gui;

public class LudoBoardGridMapping {
    /**
     * Class for holding the position in cell coordinates
     */
    public static class CellCoordinates {
        int gridRow;
        int gridColumn;

        public CellCoordinates(int gridRow, int gridColumn){
            this.gridRow = gridRow;
            this.gridColumn = gridColumn;
        }
    }

    /**
     * This is a mapping of ludoBoardPositions (index) to Grid Locations (value).
     * <p>
     *     E.g. coordinates[2] is the bottom home position for the red player in
     *     grid coordinates (row: 2, column: 5).
     * </p>
     */
    private final static CellCoordinates[] cellCoordinates = {
            // red home coordinates in "homeGrid"
            new CellCoordinates(1, 6),
            new CellCoordinates(2, 7),
            new CellCoordinates(2, 5),
            new CellCoordinates(3, 6),

            // blue home coordinates in "homeGrid"
            new CellCoordinates(5, 6),
            new CellCoordinates(6, 7),
            new CellCoordinates(6, 5),
            new CellCoordinates(7, 6),

            // yellow home coordinates in "homeGrid"
            new CellCoordinates(5, 2),
            new CellCoordinates(6, 1),
            new CellCoordinates(6, 3),
            new CellCoordinates(7, 2),

            // green home coordinates in "homeGrid"
            new CellCoordinates(1, 2),
            new CellCoordinates(2, 1),
            new CellCoordinates(2, 3),
            new CellCoordinates(3, 2),

            // all board coordinates in "movingGrid"
            // coordinates between green/red
            new CellCoordinates(1, 8),
            new CellCoordinates(2, 8),
            new CellCoordinates(3, 8),
            new CellCoordinates(4, 8),
            new CellCoordinates(5, 8),

            // coordinates between red-blue
            new CellCoordinates(6, 9),
            new CellCoordinates(6, 10),
            new CellCoordinates(6, 12),
            new CellCoordinates(6, 13),
            new CellCoordinates(6, 14),
            new CellCoordinates(7, 14),
            new CellCoordinates(8, 14),
            new CellCoordinates(8, 13),
            new CellCoordinates(8, 12),
            new CellCoordinates(8, 11),
            new CellCoordinates(8, 10),
            new CellCoordinates(8, 9),

            // coordinates between blue-yellow
            new CellCoordinates(9, 8),
            new CellCoordinates(10, 8),
            new CellCoordinates(11, 8),
            new CellCoordinates(12, 8),
            new CellCoordinates(13, 8),
            new CellCoordinates(14, 8),
            new CellCoordinates(14, 7),
            new CellCoordinates(14, 6),
            new CellCoordinates(13, 6),
            new CellCoordinates(12, 6),
            new CellCoordinates(11, 6),
            new CellCoordinates(10, 6),
            new CellCoordinates(9, 6),

            // coordinates between yellow-green
            new CellCoordinates(8, 5),
            new CellCoordinates(8, 4),
            new CellCoordinates(8, 3),
            new CellCoordinates(8, 2),
            new CellCoordinates(8, 1),
            new CellCoordinates(8, 0),
            new CellCoordinates(7, 0),
            new CellCoordinates(6, 0),
            new CellCoordinates(6, 1),
            new CellCoordinates(6, 2),
            new CellCoordinates(6, 3),
            new CellCoordinates(6, 4),
            new CellCoordinates(6, 5),

            // coordinates between green-red
            new CellCoordinates(5, 6),
            new CellCoordinates(4, 6),
            new CellCoordinates(3, 6),
            new CellCoordinates(2, 6),
            new CellCoordinates(1, 6),
            new CellCoordinates(0, 6),
            new CellCoordinates(0, 7),
            new CellCoordinates(0, 8),

            // coordinates red finish
            new CellCoordinates(1, 7),
            new CellCoordinates(2, 7),
            new CellCoordinates(3, 7),
            new CellCoordinates(4, 7),
            new CellCoordinates(5, 7),
            new CellCoordinates(6, 7),

            //  coordinates blue finish
            new CellCoordinates(7, 13),
            new CellCoordinates(7, 12),
            new CellCoordinates(7, 11),
            new CellCoordinates(7, 10),
            new CellCoordinates(7, 9),
            new CellCoordinates(7, 8),

            // coordinates yellow finish
            new CellCoordinates(13, 7),
            new CellCoordinates(12, 7),
            new CellCoordinates(11, 7),
            new CellCoordinates(10, 7),
            new CellCoordinates(9, 7),
            new CellCoordinates(8, 7),

            // coordinates green finish
            new CellCoordinates(7, 1),
            new CellCoordinates(7, 2),
            new CellCoordinates(7, 3),
            new CellCoordinates(7, 4),
            new CellCoordinates(7, 5),
            new CellCoordinates(7, 6)
    };

    public static CellCoordinates ludoBoardGridToGUIGrid(int ludoBoardPosition){
        return cellCoordinates[ludoBoardPosition];
    }
}

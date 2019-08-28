module no.ntnu.imt3281.sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.sql;

    opens no.ntnu.imt3281.sudoku to javafx.fxml;
    exports no.ntnu.imt3281.sudoku;
}
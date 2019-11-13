package no.ntnu.imt3281.ludo.gui;

import javafx.scene.control.ListCell;

public class ListViewCell extends ListCell<String>
{
    @Override
    public void updateItem(String string, boolean empty)
    {
        super.updateItem(string,empty);
        if(string != null)
        {
            ListViewCellData data = new ListViewCellData();
            data.setInfo(string);
            setGraphic(data.getBox());
        }
    }
}
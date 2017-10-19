package badgegenerator.pdfeditor;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import badgegenerator.fxfieldssaver.FxFieldsSaver;
import javafx.scene.layout.Pane;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 19/10/2017.
 */
public class SavedFieldsLayouterTest {
    private double initCenterX;
    private double initCenterX_WH;
    private double initRightX;
    private double initRightX_WH;
    private String savePath;


    private void prepareSaves(String alignment) {
        FxField field = new SingleLineField("Example", 0, 200);
        FxField fieldWithHyp = new FieldWithHyphenation("Example words", 1, 200);
        List<FxField> fields = new ArrayList<>(2);
        fields.add(field);
        fields.add(fieldWithHyp);
        fields.forEach(f -> {
            f.setAlignment(alignment);
            f.setLayoutX(30);
        });
        if(alignment.equals("CENTER")) {
            initCenterX = field.getLayoutX() + field.getPrefWidth() / 2;
            initCenterX_WH = fieldWithHyp.getLayoutX()
                    + fieldWithHyp.getPrefWidth() / 2;
        } else {
            initRightX = field.getLayoutX() + field.getPrefWidth();
            initRightX_WH = fieldWithHyp.getLayoutX()
                    + fieldWithHyp.getPrefWidth();
        }

        String bundleName = "alignmentTest";
        FxFieldsSaver.createSave(fields, bundleName);
        savePath = SavesManager.getSavesFolder()
                + File.separator
                + bundleName;
    }

    @Test
    public void alignsCenterProperly() throws Exception {
        // Arrange
        prepareSaves("CENTER");

        // Act
        SavedFieldsLayouter layouter = new SavedFieldsLayouter(new Pane(),
                new Pane(),
                new Pane(),
                new String[]{"Short", "Shortestest words"},
                new String[]{"Short", "Shortestest words"},
                1,
                savePath);
        layouter.positionFields();
        FxField newField = layouter.getFxFields().get(0);
        FxField newFieldWH = layouter.getFxFields().get(1);
        double finalFieldCenterX = newField.getLayoutX() + newField.getPrefWidth() / 2;
        double finalFieldWHCenterX = newFieldWH.getLayoutX() + newFieldWH.getPrefWidth() / 2;

        // Assert
        assertThat("Single line", finalFieldCenterX, is(initCenterX));
        assertThat("Multi line", finalFieldWHCenterX, is(initCenterX_WH));
    }

    @Test
    public void alignsRightProperly() throws Exception {
        // Arrange
        prepareSaves("RIGHT");

        // Act
        SavedFieldsLayouter layouter = new SavedFieldsLayouter(new Pane(),
                new Pane(),
                new Pane(),
                new String[]{"Short", "Shortestest words"},
                new String[]{"Short", "Shortestest words"},
                1,
                savePath);
        layouter.positionFields();
        FxField newField = layouter.getFxFields().get(0);
        FxField newFieldWH = layouter.getFxFields().get(1);
        double finalFieldRightX = newField.getLayoutX() + newField.getPrefWidth();
        double finalFieldWHRightX = newFieldWH.getLayoutX() + newFieldWH.getPrefWidth();

        // Assert
        assertThat("Single line", finalFieldRightX, is(initRightX));
        assertThat("Multi line", finalFieldWHRightX, is(initRightX_WH));
    }
}
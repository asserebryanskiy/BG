package badgegenerator.custompanes;

import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.List;

public class TestBase extends ApplicationTest{
    MockApp app;
    FxField field;
    FxField fieldWithHyp;
    Bounds draggedFieldPos;
    Bounds targetFieldPos;
    List<FxField> fields;

    @Override
    public void start(Stage stage) throws Exception {
        app = new MockApp();
        app.start(stage);
        fieldWithHyp = app.getFields().get(0);
        field = app.getFields().get(1);
        fields = app.getFields();
    }

    @Before
    public void setUp() throws Exception {
        fieldWithHyp.setLayoutX(80);
        fieldWithHyp.setLayoutY(100);
        field.setLayoutX(100);
        field.setLayoutY(80);
        draggedFieldPos = bounds(fieldWithHyp).query();
        targetFieldPos = bounds(field).query();
    }

    @After
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}

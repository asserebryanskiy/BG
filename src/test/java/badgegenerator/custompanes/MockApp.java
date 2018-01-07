package badgegenerator.custompanes;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MockApp extends Application {
    private static final int ROOT_WIDTH = 300;
    private List<FxField> fields;
    private Line verticalGuide;
    private Line horizontalGuide;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FieldWithHyphenation fieldWithHyp =
                new FieldWithHyphenation("Example words words words", "Example", ROOT_WIDTH);
        SingleLineField field = new SingleLineField("Example", "Example2", ROOT_WIDTH);

        fields = new ArrayList<>();
        fields.add(fieldWithHyp);
        fields.add(field);
        Pane root = new Pane();
        root.setPadding(new Insets(20));
        root.getChildren().add(new Rectangle(ROOT_WIDTH, ROOT_WIDTH, Color.PINK));
        root.getChildren().addAll(fields);
        fields.forEach(f -> {
            try {
                f.addGuide(new Guide(f, Position.LEFT));
                f.addGuide(new Guide(f, Position.RIGHT));
            } catch (NoParentFoundException | NoIdFoundException e) {
                e.printStackTrace();
            }
        });
        root.getChildren().addAll(FxField.getGuides());
        verticalGuide = new Line(150, 1, 150, ROOT_WIDTH - 1);
        verticalGuide.setId("centralGuide");
        horizontalGuide = new Line(1, 150, ROOT_WIDTH - 1, 150);
        horizontalGuide.setManaged(false);
        horizontalGuide.setId("horizontalGuide");
//        root.getChildren().addAll(verticalGuide);
        root.getChildren().addAll(verticalGuide, horizontalGuide);
        FxField.setVerticalGuide(verticalGuide);
        FxField.setHorizontalGuide(horizontalGuide);
        root.setMaxSize(ROOT_WIDTH, ROOT_WIDTH);

        fieldWithHyp.addResizeableBorder(new ResizeableBorder(fieldWithHyp, Position.LEFT));
        fieldWithHyp.addResizeableBorder(new ResizeableBorder(fieldWithHyp, Position.RIGHT));
        root.getChildren().addAll(fieldWithHyp.getResizeableBorders());

        primaryStage.setScene(new Scene(root, ROOT_WIDTH, ROOT_WIDTH));
        primaryStage.show();
    }

    public List<FxField> getFields() {
        return fields;
    }

    public Line getVerticalGuide() {
        return verticalGuide;
    }

    public Line getHorizontalGuide() {
        return horizontalGuide;
    }
}

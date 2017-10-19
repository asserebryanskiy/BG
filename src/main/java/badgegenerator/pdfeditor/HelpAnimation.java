package badgegenerator.pdfeditor;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Animation visually demonstrating the use of binding fields technology.
 */
class HelpAnimation extends Pane{
    private final int startY = 12;
    private Text position;
    private Text name;
    private Text surname;
    private Text badgeNumber;

    HelpAnimation() {
        int fontSize = 10;

        position = new Text(12, startY, "Глава правления северного филиала Центрального региона по вопросам транспорта и логистики");
        position.setFont(new Font(fontSize));
        position.setWrappingWidth(180);
        name = new Text(12, 4*startY, "Андрей");
        name.setFont(new Font(fontSize));
        surname = new Text(12, 5*startY, "Георгиевский");
        surname.setFont(new Font(fontSize));
        badgeNumber = new Text(130, 80, "Бейдж 1");
        badgeNumber.setFont(new Font(((double) fontSize) * 1.5));
        badgeNumber.setFill(Color.GRAY);
        Rectangle background = new Rectangle(200,90,Color.WHITE);
        background.setArcWidth(20);
        background.setArcHeight(20);

        setPrefSize(200, 90);
        setManaged(false);
        getChildren().addAll(background, position, name, surname, badgeNumber);
        getChildren().forEach(node -> node.setManaged(false));
    }

    void playWithMotion() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                position.setText("Менеджер");
                name.setText("Сергей");
                surname.setText("Белобородский");
                badgeNumber.setText("Бейдж 2");

                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(700),
                        new KeyValue(name.yProperty(), 2*startY),
                        new KeyValue(surname.yProperty(), 3*startY)));
                timeline.play();
            }
        }, 1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                position.setText("Заместитель директора по вопросам безопансости");
                name.setText("Кирилл");
                surname.setText("Котельников");
                badgeNumber.setText("Бейдж 3");

                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(700),
                        new KeyValue(name.yProperty(), 3*startY),
                        new KeyValue(surname.yProperty(), 4*startY)));
                timeline.play();
            }
        }, 2200);
    }

    void play() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                position.setText("Менеджер");
                name.setText("Сергей");
                surname.setText("Белобородский");
                badgeNumber.setText("Бейдж 2");
            }
        }, 700);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                position.setText("Заместитель директора по вопросам безопансости");
                name.setText("Кирилл");
                surname.setText("Котельников");
                badgeNumber.setText("Бейдж 3");
            }
        }, 1400);
    }
}

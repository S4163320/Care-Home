package CareHome.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CareHomeApp extends Application {
    private static Stage primaryStage;

    public static final double LOGIN_W = 600, LOGIN_H = 400;
    public static final double DASHBOARD_W = 900, DASHBOARD_H = 700;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLoginPage();
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void showLoginPage() throws Exception {
        switchScene("/CareHome/view/LoginView.fxml", "Care Home - Login", LOGIN_W, LOGIN_H, false);
    }

    // Replaces the primary stage scene with the given FXML, title, size, and resizable flag
    public static void switchScene(String fxmlPath, String title,
                                   double width, double height, boolean resizable) throws Exception {
        FXMLLoader loader = new FXMLLoader(CareHomeApp.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setResizable(resizable);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // Opens a blocking modal window from FXML sized relative to the primary stage
    public static Stage openModalPercent(String fxmlPath, String title,
                                         double widthPct, double heightPct, boolean resizable) throws Exception {
        FXMLLoader loader = new FXMLLoader(CareHomeApp.class.getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.initOwner(getPrimaryStage());
        stage.initModality(Modality.WINDOW_MODAL);

        double w = getPrimaryStage().getWidth() * widthPct;
        double h = getPrimaryStage().getHeight() * heightPct;

        stage.setTitle(title);
        stage.setScene(new Scene(root, w, h));
        stage.setResizable(resizable);
        stage.setWidth(w);
        stage.setHeight(h);
        stage.centerOnScreen();
        stage.show();
        return stage;
    }

    public static void main(String[] args) { launch(); }
}

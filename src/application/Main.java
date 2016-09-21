package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/Main.fxml"));
			Parent root = loader.load();
			MainController controller = loader.getController();
			controller.setHostServices(getHostServices());

			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setTitle("JFX Konwerter");
			primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
			primaryStage.setMaxWidth(595);
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}

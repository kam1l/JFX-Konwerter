package com.gmail.kamiloleksik.jfxkonwerter;

import com.gmail.kamiloleksik.jfxkonwerter.controllers.MainController;

import javafx.application.Application;
import javafx.application.Platform;
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
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/com/gmail/kamiloleksik/jfxkonwerter/resources/view/Main.fxml"));
			Parent root = loader.load();
			MainController controller = loader.getController();
			controller.setHostServices(getHostServices());

			Scene scene = new Scene(root);
			scene.getStylesheets()
					.add(getClass().getResource("/com/gmail/kamiloleksik/jfxkonwerter/resources/css/application.css")
							.toExternalForm());

			primaryStage.setTitle("JFX Konwerter");
			primaryStage.getIcons().add(new Image(
					Main.class.getResourceAsStream("/com/gmail/kamiloleksik/jfxkonwerter/resources/images/icon.png")));
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(e ->
			{
				if (controller.canBeShutdown())
				{
					controller.closeConnection();
					controller.shutdownExecutor();
					Platform.exit();
				}
				else
				{
					e.consume();
				}
			});
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

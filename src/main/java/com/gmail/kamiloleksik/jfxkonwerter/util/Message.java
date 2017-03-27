package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.util.Optional;

import com.gmail.kamiloleksik.jfxkonwerter.Main;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Message
{
	public static void showMessage(String title, String content, AlertType alertType)
	{
		createAlert(title, content, alertType).showAndWait();
	}
	
	public static void showMessage(Stage stage, String title, String content, AlertType alertType)
	{
		createAlert(stage, title, content, alertType).showAndWait();
	}

	public static boolean showConfirmationMessage(Stage stage, String title, String content)
	{
		Optional<ButtonType> result = createAlert(stage, title, content, AlertType.CONFIRMATION).showAndWait();

		if (result.get() == ButtonType.OK)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private static Alert createAlert(String title, String content, AlertType alertType)
	{
		Alert alert = new Alert(alertType);
		
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		setIcon(alert);
		
		return alert;
	}
	
	private static Alert createAlert(Stage stage, String title, String content, AlertType alertType)
	{
		Alert alert = createAlert(title, content, alertType);
		alert.initOwner(stage);
		
		return alert;
	}
	
	private static void setIcon(Alert alert)
	{
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
	}
}

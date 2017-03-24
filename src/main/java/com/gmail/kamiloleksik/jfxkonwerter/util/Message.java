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
		Alert alert = new Alert(alertType);

		setIcon(alert);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);

		alert.showAndWait();
	}

	public static boolean showConfirmationMessage(String title, String content, AlertType alertType)
	{
		Alert alert = new Alert(alertType);

		setIcon(alert);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static void setIcon(Alert alert)
	{
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
	}
}

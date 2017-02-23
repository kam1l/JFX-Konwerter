package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class Message
{
	public static void showMessage(String title, String content, AlertType alertType)
	{
		Alert alert = new Alert(alertType);

		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);

		alert.showAndWait();
	}

	public static boolean showConfirmationMessage(String title, String content, AlertType alertType)
	{
		Alert alert = new Alert(alertType);

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
}

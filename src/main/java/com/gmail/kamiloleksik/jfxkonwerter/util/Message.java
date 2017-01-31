package com.gmail.kamiloleksik.jfxkonwerter.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

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
}

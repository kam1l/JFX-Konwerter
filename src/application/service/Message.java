package application.service;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Message
{
	public static final String ERROR_TITLE = "B³¹d";
	public static final String INFORMATION_TITLE = "Informacja";

	public static final String CRITICAL_ERROR_MESSAGE = "Wyst¹pi³ b³¹d podczas odczytu danych i aplikacja nie "
			+ "mo¿e kontynuowaæ dzia³ania.";
	public static final String UPDATE_ERROR_MESSAGE = "Wyst¹pi³ b³¹d podczas aktualizacji kursów walut.";
	public static final String UPDATE_SUCCESS_MESSAGE = "Aktualizacja kursów walut przebieg³a pomyœlnie.";
	public static final String SAVING_PREFERENCES_ERROR_MESSAGE = "Wyst¹pi³ b³¹d podczas zapisu ustawieñ.";
	public static final String INVALID_NUMBER_FORMAT_MESSAGE = "Nieprawid³owy format liczby.";
	public static final String INVALID_NUMBER_BASE_MESSAGE = "Nieprawid³owy format liczby o podstawie ";

	public void showMessage(String title, String content)
	{
		Alert alert;

		if (alertTypeIsError(title))
		{
			alert = new Alert(AlertType.ERROR);
			alert.setTitle(ERROR_TITLE);
		}
		else
		{
			alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(INFORMATION_TITLE);
		}

		alert.setHeaderText(null);
		alert.setContentText(content);

		alert.showAndWait();
	}

	private boolean alertTypeIsError(String title)
	{
		return title.equals(ERROR_TITLE);
	}
}

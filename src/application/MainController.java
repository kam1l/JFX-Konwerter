package application;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable
{
	private Model model = new Model();
	private Message message = new Message();
	private static Stage stage;
	private HostServices hostServices;

	private String userInput;
	private Map<String, Double> exchangeRatesAfterUpdate;
	public static BooleanProperty wasNumberOfDecimalPlacesChanged = new SimpleBooleanProperty();
	public static BooleanProperty wasDefaultSkinNameChanged = new SimpleBooleanProperty();

	@FXML
	private TextField valueTextField, resultTextField;

	@FXML
	private AnchorPane appInfoAnchorPane, updateInfoAnchorPane;

	@FXML
	private ComboBox<String> unitTypeComboBox, firstUnitComboBox, secondUnitComboBox;

	private EventHandler<ActionEvent> firstUnitComboBoxHandler, secondUnitComboBoxHandler;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		model.initializeRamDataStructures();
		setAppSkin();

		ObservableList<String> unitTypeNames = model.getAllUnitTypeNames();
		String currentUnitTypeName = Model.getCurrentUnitTypeName();
		SingleSelectionModel<String> unitTypeSel = unitTypeComboBox.getSelectionModel();

		unitTypeComboBox.setItems(unitTypeNames);
		unitTypeSel.select(currentUnitTypeName);

		ObservableList<String> unitNames = model.getCurrentMainWindowSetOfUnitNames();
		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
		String currentFirstUnitName = Model.getFirstCurrentUnitDisplayName();
		String currentSecondUnitName = Model.getSecondCurrentUnitDisplayName();

		firstUnitComboBox.setItems(unitNames);
		secondUnitComboBox.setItems(unitNames);
		firstUnitSel.select(currentFirstUnitName);
		secondUnitSel.select(currentSecondUnitName);

		valueTextField.setText("0");
		setResult();

		addEventHandlersToComboBoxes();
		addListenersToBooleanProperties();
	}

	private void addListenersToBooleanProperties()
	{
		wasNumberOfDecimalPlacesChanged.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (newValue == true)
				{
					setResult();
					wasNumberOfDecimalPlacesChanged.set(false);
				}
			}
		});

		wasDefaultSkinNameChanged.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (newValue == true)
				{
					setAppSkin();
					wasDefaultSkinNameChanged.set(false);
				}
			}
		});
	}

	private void addEventHandlersToComboBoxes()
	{
		firstUnitComboBoxHandler = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
				int firstUnitSelectedIndex = firstUnitSel.getSelectedIndex();

				model.changeFirstCurrentUnit(firstUnitSelectedIndex);

				setResult();
			}
		};

		secondUnitComboBoxHandler = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
				int secondUnitSelectedIndex = secondUnitSel.getSelectedIndex();

				model.changeSecondCurrentUnit(secondUnitSelectedIndex);

				setResult();
			}
		};

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);
	}

	public void processDigitAndSetResult(ActionEvent event)
	{
		getUserInputFromValueTextField();

		Object eventSource = event.getSource();
		Button eventSourceButton = (Button) eventSource;
		String clickedButtonValue = eventSourceButton.getText();

		if (userInput.equals("0"))
		{
			valueTextField.setText(clickedButtonValue);
		}
		else
		{
			valueTextField.setText(userInput + clickedButtonValue);
		}

		setResult();
	}

	public void processDecimalMark(ActionEvent event)
	{
		getUserInputFromValueTextField();

		if (!userInput.contains(".") && userInput.length() > 0)
		{
			valueTextField.setText(userInput + ".");
		}
	}

	public void processSignAndSetResult(ActionEvent event)
	{
		getUserInputFromValueTextField();

		if (userInput.length() > 0)
		{
			if (userInput.charAt(0) == '-')
			{
				valueTextField.setText(userInput.substring(1));
			}
			else if (userInput.length() > 1 || userInput.charAt(0) != '0')
			{
				valueTextField.setText("-" + userInput);
			}

			setResult();
		}
	}

	public void processDeletionKeyAndSetResult(ActionEvent event)
	{
		getUserInputFromValueTextField();

		Object eventSource = event.getSource();
		Button eventSourceButton = (Button) eventSource;
		String clickedButtonValue = eventSourceButton.getText();

		if (clickedButtonValue.equals("C"))
		{
			valueTextField.setText("0");
		}
		else
		{
			if (userInput.matches("-?[0-9]?|(-0\\.)"))
			{
				valueTextField.setText("0");
			}
			else
			{
				valueTextField.setText(userInput.substring(0, userInput.length() - 1));
			}
		}

		setResult();
	}

	public void processUserKeys(KeyEvent event)
	{
		setResult();
	}

	public void swapUnits(ActionEvent event)
	{
		firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
		int firstUnitIndex = firstUnitSel.getSelectedIndex();
		int secondUnitIndex = secondUnitSel.getSelectedIndex();

		firstUnitSel.select(secondUnitIndex);
		secondUnitSel.select(firstUnitIndex);

		model.changeFirstCurrentUnit(secondUnitIndex);
		model.changeSecondCurrentUnit(firstUnitIndex);

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		setResult();
	}

	public void runUpdateThread(ActionEvent event)
	{
		updateInfoAnchorPane.setVisible(true);

		Runnable updater = new Runnable()
		{
			@Override
			public void run()
			{
				boolean taskSucceeded = updateRates();
				Platform.runLater(() ->
				{
					if (taskSucceeded == false)
					{
						message.showMessage(Message.ERROR_TITLE, Message.UPDATE_ERROR_MESSAGE);
					}
					else
					{
						model.updateExchangeRatesInRam(exchangeRatesAfterUpdate);
						message.showMessage(Message.INFORMATION_TITLE, Message.UPDATE_SUCCESS_MESSAGE);

						setResult();
					}

					updateInfoAnchorPane.setVisible(false);
				});
			}
		};

		new Thread(updater).start();
	}

	public boolean updateRates()
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db
					.parse(new URL("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").openStream());
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Cube");
			exchangeRatesAfterUpdate = new HashMap<String, Double>();

			for (int tmp = 2; tmp < nList.getLength(); tmp++)
			{
				Node nNode = nList.item(tmp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					String currentCurrency = eElement.getAttribute("currency");
					double currentRate = 1.0 / Double.parseDouble(eElement.getAttribute("rate"));

					exchangeRatesAfterUpdate.put(currentCurrency, currentRate);

					if (!model.updateRateInDB(currentCurrency, currentRate))
					{
						return false;
					}
				}
			}

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public void showPreferences(ActionEvent event) throws IOException
	{
		stage = new Stage();
		stage.setMaxHeight(368);
		stage.setResizable(false);
		Parent root = FXMLLoader.load(getClass().getResource("/application/Preferences.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.getIcons().add(new Image(MainController.class.getResourceAsStream("icon.png")));
		stage.setScene(scene);
		stage.setTitle("Preferencje");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(appInfoAnchorPane.getScene().getWindow());
		stage.show();
	}

	public void changeUnitSet(ActionEvent event)
	{
		firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		firstUnitComboBox.getItems().clear();
		secondUnitComboBox.getItems().clear();

		SingleSelectionModel<String> unitTypeSel = unitTypeComboBox.getSelectionModel();
		int unitTypeIndex = unitTypeSel.getSelectedIndex();

		model.changeCurrentMainWindowSetOfUnits(unitTypeIndex);

		firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		ObservableList<String> unitNames = model.getCurrentMainWindowSetOfUnitNames();
		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();

		firstUnitComboBox.setItems(unitNames);
		firstUnitSel.select(0);
		model.changeFirstCurrentUnit(0);

		secondUnitComboBox.setItems(unitNames);
		secondUnitSel.select(0);
		model.changeSecondCurrentUnit(0);

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		setResult();
	}

	public void setResult()
	{
		getUserInputFromValueTextField();

		Value inputValue = new Value(userInput);
		String result = model.convertValue(inputValue);

		resultTextField.setText(result);
	}

	public void closeApp(ActionEvent event)
	{
		Platform.exit();
		System.exit(0);
	}

	public void showAppInfo(ActionEvent event)
	{
		appInfoAnchorPane.setVisible(true);
	}

	public void closeAppInfo(ActionEvent event)
	{
		appInfoAnchorPane.setVisible(false);
	}

	public void openEmailClient(ActionEvent event)
	{
		hostServices.showDocument("mailto:kamiloleksik@gmail.com");
	}

	public void openWebBrowser(ActionEvent event)
	{
		hostServices.showDocument("https://github.com/kam1l/JFX-Konwerter");
	}

	public void setAppSkin()
	{
		String skinName = Model.getPreferences().getDefaultSkinName();

		if (skinName.equals("Modena"))
		{
			Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
		}
		else
		{
			Application.setUserAgentStylesheet("/application/caspian.css");
		}
	}

	private void getUserInputFromValueTextField()
	{
		userInput = valueTextField.getText();
	}

	public static Stage getStage()
	{
		return stage;
	}

	public void setHostServices(HostServices hostServices)
	{
		this.hostServices = hostServices;
	}
}
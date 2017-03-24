package com.gmail.kamiloleksik.jfxkonwerter;

import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.DaoKey.*;
import static com.j256.ormlite.dao.DaoManager.createDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.gmail.kamiloleksik.jfxkonwerter.controller.MainController;
import com.gmail.kamiloleksik.jfxkonwerter.model.Model;
import com.gmail.kamiloleksik.jfxkonwerter.model.entity.*;
import com.gmail.kamiloleksik.jfxkonwerter.util.Message;
import com.gmail.kamiloleksik.jfxkonwerter.util.keys.*;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application
{
	private AnnotationConfigApplicationContext applicationContext;

	@Override
	public void start(Stage primaryStage)
	{
		applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.scan("com.gmail.kamiloleksik.jfxkonwerter");
		applicationContext.refresh();
		applicationContext.registerShutdownHook();
		applicationContext.start();

		applicationContext.getBean(App.class).start(primaryStage, this);
	}

	@Override
	public void stop()
	{
		applicationContext.getBean(ConnectionSource.class).closeQuietly();
		applicationContext.close();
	}

	public static void main(String[] args)
	{
		Application.launch(Main.class, args);
	}

	public static ResourceBundle getBundle(Model model)
	{
		String appLanguageName = model.getAppLanguageName();
		Locale locale;

		if (appLanguageName.equals("Polski"))
		{
			locale = new Locale("pl");
		}
		else
		{
			locale = new Locale("en");
		}

		return new ResourceBundle()
		{
			ResourceBundle rb = ResourceBundle.getBundle("bundles.lang", locale);

			@Override
			protected Object handleGetObject(String key)
			{
				if (key.equals("model"))
				{
					return model;
				}
				else
				{
					return rb.getString(key);
				}
			}

			@Override
			public Enumeration<String> getKeys()
			{
				return rb.getKeys();
			}
		};
	}

	@Configuration
	public static class Config
	{
		@Bean
		public App init(ResourceBundle resourceBundle)
		{
			return new App(resourceBundle);
		}

		@Bean
		public ResourceBundle resourceBundle(Model model)
		{
			return getBundle(model);
		}

		@Bean
		public ConnectionSource connection()
		{
			try
			{
				return new JdbcConnectionSource("jdbc:sqlite:resource.db");
			}
			catch (Exception e)
			{
				ResourceBundle resourceBundle = ResourceBundle.getBundle("bundles.lang", new Locale("en"));
				String errorTitle = resourceBundle.getString("errorTitle");
				String criticalErrorMessage = resourceBundle.getString("criticalErrorMessage");

				Message.showMessage(errorTitle, criticalErrorMessage, AlertType.ERROR);
				System.exit(-1);
			}

			return null;
		}

		@Bean
		public Map<DaoKey, Dao<?, ?>> daos(ConnectionSource connection)
		{
			Map<DaoKey, Dao<?, ?>> map = new EnumMap<DaoKey, Dao<?, ?>>(DaoKey.class);

			try
			{
				map.put(UNIT_TYPE_DAO, createDao(connection, UnitType.class));
				map.put(UNIT_TYPE_EN_DAO, createDao(connection, UnitType_en.class));
				map.put(UNIT_DAO, createDao(connection, Unit.class));
				map.put(UNIT_EN_DAO, createDao(connection, Unit_en.class));
				map.put(UNITS_LANGUAGE_DAO, createDao(connection, UnitsLanguage.class));
				map.put(APP_LANGUAGE_DAO, createDao(connection, AppLanguage.class));
				map.put(PREFERENCES_DAO, createDao(connection, Preferences.class));
				map.put(NUMBER_OF_DECIMAL_PLACES_DAO, createDao(connection, NumberOfDecimalPlaces.class));
				map.put(APP_SKIN_DAO, createDao(connection, AppSkin.class));

				return map;
			}
			catch (Exception e)
			{
				ResourceBundle resourceBundle = ResourceBundle.getBundle("bundles.lang", new Locale("en"));
				String errorTitle = resourceBundle.getString("errorTitle");
				String criticalErrorMessage = resourceBundle.getString("criticalErrorMessage");

				Message.showMessage(errorTitle, criticalErrorMessage, AlertType.ERROR);
				System.exit(-1);
			}

			return null;
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public List<UnitType> allUnitTypes()
		{
			return new ArrayList<UnitType>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public List<Unit> units()
		{
			return new ArrayList<Unit>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public List<AppLanguage> appLanguages()
		{
			return new ArrayList<AppLanguage>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public List<UnitsLanguage> unitsLanguages()
		{
			return new ArrayList<UnitsLanguage>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public List<AppSkin> appSkins()
		{
			return new ArrayList<AppSkin>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public List<NumberOfDecimalPlaces> numbersOfDecimalPlaces()
		{
			return new ArrayList<NumberOfDecimalPlaces>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public Map<String, BigDecimal> updatedExchangeRates()
		{
			return new HashMap<String, BigDecimal>();
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public Map<UnitTypeKey, UnitType> unitTypes()
		{
			return new EnumMap<UnitTypeKey, UnitType>(UnitTypeKey.class);
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public Map<UnitKey, Unit> unitsM()
		{
			return new EnumMap<UnitKey, Unit>(UnitKey.class);
		}

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public Map<NamesKey, ObservableList<String>> names()
		{
			return new EnumMap<NamesKey, ObservableList<String>>(NamesKey.class);
		}
	}

	public static class App
	{
		private static final String WINDOW_POSITION_X = "WinPosX";
		private static final String WINDOW_POSITION_Y = "WinPosY";
		private static final String WINDOW_POSITION_WAS_SAVED = "WinPosSaved";
		private ResourceBundle resourceBundle;

		public App(ResourceBundle resourceBundle)
		{
			this.resourceBundle = resourceBundle;
		}

		public void start(Stage primaryStage, Application main)
		{
			try
			{
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"), resourceBundle);
				Parent root = loader.load();
				MainController controller = loader.getController();
				controller.setHostServices(main.getHostServices());
				controller.setStage(primaryStage);
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

				java.util.prefs.Preferences savedPreferences = java.util.prefs.Preferences.userRoot();
				if (savedPreferences.getBoolean(WINDOW_POSITION_WAS_SAVED, false))
				{
					double x = savedPreferences.getDouble(WINDOW_POSITION_X, 400);
					double y = savedPreferences.getDouble(WINDOW_POSITION_Y, 400);

					primaryStage.setX(x);
					primaryStage.setY(y);
				}

				primaryStage.setTitle("JFX Konwerter");
				primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
				primaryStage.setResizable(false);
				primaryStage.sizeToScene();
				primaryStage.setScene(scene);
				primaryStage.setOnCloseRequest(e ->
				{
					if (controller.canBeShutdown())
					{
						java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userRoot();
						preferences.putDouble(WINDOW_POSITION_X, primaryStage.getX());
						preferences.putDouble(WINDOW_POSITION_Y, primaryStage.getY());
						preferences.putBoolean(WINDOW_POSITION_WAS_SAVED, true);
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
	}
}

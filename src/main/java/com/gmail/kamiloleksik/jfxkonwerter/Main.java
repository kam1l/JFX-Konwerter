package com.gmail.kamiloleksik.jfxkonwerter;

import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.DaoKey.*;
import static com.j256.ormlite.dao.DaoManager.createDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.gmail.kamiloleksik.jfxkonwerter.controllers.MainController;
import com.gmail.kamiloleksik.jfxkonwerter.model.Model;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.*;
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

	@Configuration
	public static class Config
	{
		@Bean
		public App init(ResourceBundle resourceBundle)
		{
			return new App(resourceBundle);
		}

		@Bean
		public Model model(Map<DaoKey, Dao<?, ?>> daos)
		{
			return new Model(daos);
		}

		@Bean
		public ResourceBundle resourceBundle(Model model)
		{
			return new ResourceBundle()
			{
				@Override
				protected Object handleGetObject(String key)
				{
					return model;
				}

				@Override
				public Enumeration<String> getKeys()
				{
					return null;
				}
			};
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
				Message.showMessage(Message.ERROR_TITLE, Message.CRITICAL_ERROR_MESSAGE);
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
				map.put(UNIT_DAO, createDao(connection, Unit.class));
				map.put(UNITS_LANGUAGE_DAO, createDao(connection, UnitsLanguage.class));
				map.put(APP_LANGUAGE_DAO, createDao(connection, AppLanguage.class));
				map.put(PREFERENCES_DAO, createDao(connection, Preferences.class));
				map.put(NUMBER_OF_DECIMAL_PLACES_DAO, createDao(connection, NumberOfDecimalPlaces.class));
				map.put(APP_SKIN_DAO, createDao(connection, AppSkin.class));

				return map;
			}
			catch (Exception e)
			{
				Message.showMessage(Message.ERROR_TITLE, Message.CRITICAL_ERROR_MESSAGE);
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
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

				primaryStage.setTitle("JFX Konwerter");
				primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
				primaryStage.setResizable(false);
				primaryStage.sizeToScene();
				primaryStage.setScene(scene);
				primaryStage.setOnCloseRequest(e ->
				{
					if (controller.canBeShutdown())
					{
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

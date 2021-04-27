package edu.kis.powp.jobs2d;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.kis.legacy.drawer.panel.DrawPanelController;
import edu.kis.legacy.drawer.shape.LineFactory;
import edu.kis.powp.appbase.Application;
import edu.kis.powp.jobs2d.command.gui.CommandManagerWindow;
import edu.kis.powp.jobs2d.command.gui.CommandManagerWindowCommandChangeObserver;
import edu.kis.powp.jobs2d.drivers.TransformationDriver;
import edu.kis.powp.jobs2d.drivers.adapter.LineDriverAdapter;

import edu.kis.powp.jobs2d.drivers.transformation.Rotate;
import edu.kis.powp.jobs2d.drivers.transformation.Scale;

import edu.kis.powp.jobs2d.events.*;

import edu.kis.powp.jobs2d.drivers.composite.DriverComposite;
import edu.kis.powp.jobs2d.drivers.composite.IDriverComposite;

import edu.kis.powp.jobs2d.events.SelectLoadSecretCommandOptionListener;
import edu.kis.powp.jobs2d.events.SelectRunCurrentCommandOptionListener;
import edu.kis.powp.jobs2d.events.SelectTestFigure2OptionListener;
import edu.kis.powp.jobs2d.events.SelectTestFigureOptionListener;

import edu.kis.powp.jobs2d.features.CommandsFeature;
import edu.kis.powp.jobs2d.features.DrawerFeature;
import edu.kis.powp.jobs2d.features.DriverFeature;

public class TestJobs2dApp {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Setup test concerning preset figures in context.
     *
     * @param application Application context.
     */
    private static void setupPresetTests(Application application) {
        SelectTestFigureOptionListener selectTestFigureOptionListener = new SelectTestFigureOptionListener(
                DriverFeature.getDriverManager());
        SelectTestFigure2OptionListener selectTestFigure2OptionListener = new SelectTestFigure2OptionListener(
                DriverFeature.getDriverManager());
        SelectTestMouseListener selectTestMouseListener = new SelectTestMouseListener(
                DriverFeature.getDriverManager(), application.getFreePanel());

        application.addTest("Figure Joe 1", selectTestFigureOptionListener);
        application.addTest("Figure Joe 2", selectTestFigure2OptionListener);
        application.addTest("Enable mouse controls", selectTestMouseListener);
    }

    /**
     * Setup test using driver commands in context.
     *
     * @param application Application context.
     */
    private static void setupCommandTests(Application application) {
        application.addTest("Load secret command", new SelectLoadSecretCommandOptionListener());
        application.addTest("Load triangle command", new SelectLoadTriangleCommandOptionListener());
        application.addTest("Run command", new SelectRunCurrentCommandOptionListener(DriverFeature.getDriverManager()));

    }

    /**
     * Setup driver manager, and set default Job2dDriver for application.
     *
     * @param application Application context.
     */
    private static void setupDrivers(Application application) {
        Job2dDriver loggerDriver = new LoggerDriver();
        DriverFeature driverFeature = new DriverFeature();
        driverFeature.addDriver("Logger driver", loggerDriver);

        DrawPanelController drawerController = DrawerFeature.getDrawerController();
        Job2dDriver driver = new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic");
        driverFeature.addDriver("Line Simulator", driver);
        DriverFeature.getDriverManager().setCurrentDriver(driver);

        driver = new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special");
        driverFeature.addDriver("Special line Simulator", driver);

        TransformationDriver scaleDriver = new TransformationDriver(new Scale(0.5d, 0.5d), new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic line"));
        driverFeature.addDriver("Scale (0.5x)", scaleDriver);
        TransformationDriver scaleDriver2 = new TransformationDriver(new Scale(1.5d, 1.5d), new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic line"));
        driverFeature.addDriver("Scale (1.5x)", scaleDriver2);


        TransformationDriver rotateDriver = new TransformationDriver(new Rotate(Math.PI/2), new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic line"));
        driverFeature.addDriver("Rotate (PI/2)", rotateDriver);
        TransformationDriver rotateDriver2 = new TransformationDriver(new Rotate(Math.PI/3), new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic line"));
        driverFeature.addDriver("Rotate (PI/3)", rotateDriver2);

        TransformationDriver rotateDriver3 = new TransformationDriver(new Scale(1d, -1d), new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic line"));
        driverFeature.addDriver("Flip (vertically)", rotateDriver3);
        TransformationDriver rotateDriver4 = new TransformationDriver(new Scale(-1d, 1d), new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic line"));
        driverFeature.addDriver("Flip (horizontally)", rotateDriver4);

        // DriverFeature.updateDriverInfo();

        driverFeature.update();

        IDriverComposite compositeDriver = new DriverComposite();
        compositeDriver.add(driver);
        compositeDriver.add(loggerDriver);

        driverFeature.addDriver("Composite Driver", compositeDriver);
    }

    private static void setupWindows(Application application) {

        CommandManagerWindow commandManager = new CommandManagerWindow(CommandsFeature.getDriverCommandManager());
        application.addWindowComponent("Command Manager", commandManager);

        CommandManagerWindowCommandChangeObserver windowObserver = new CommandManagerWindowCommandChangeObserver(
                commandManager);
        CommandsFeature.getDriverCommandManager().getChangePublisher().addSubscriber(windowObserver);
    }

    /**
     * Setup menu for adjusting logging settings.
     *
     * @param application Application context.
     */
    private static void setupLogger(Application application) {

        application.addComponentMenu(Logger.class, "Logger", 0);
        application.addComponentMenuElement(Logger.class, "Clear log",
                (ActionEvent e) -> application.flushLoggerOutput());
        application.addComponentMenuElement(Logger.class, "Fine level", (ActionEvent e) -> logger.setLevel(Level.FINE));
        application.addComponentMenuElement(Logger.class, "Info level", (ActionEvent e) -> logger.setLevel(Level.INFO));
        application.addComponentMenuElement(Logger.class, "Warning level",
                (ActionEvent e) -> logger.setLevel(Level.WARNING));
        application.addComponentMenuElement(Logger.class, "Severe level",
                (ActionEvent e) -> logger.setLevel(Level.SEVERE));
        application.addComponentMenuElement(Logger.class, "OFF logging", (ActionEvent e) -> logger.setLevel(Level.OFF));
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application app = new Application("Jobs 2D");
                DrawerFeature.setupDrawerPlugin(app);
                CommandsFeature.setupCommandManager();

                DriverFeature.setupDriverPlugin(app);
                setupDrivers(app);
                setupPresetTests(app);
                setupCommandTests(app);
                setupLogger(app);
                setupWindows(app);

                app.setVisibility(true);
            }
        });
    }

}

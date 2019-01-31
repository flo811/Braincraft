package braindraft.window;

import braindraft.dao.NetworkDAO;
import braindraft.model.Trainer;
import braindraft.model.network.Network;
import braindraft.window.frames.CreateFrame;
import braindraft.window.frames.EmptyFrame;
import braindraft.window.frames.NetworkFrame;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author flo
 */
public class Window {

    private final SimpleObjectProperty<Trainer> trainerProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Network> networkProperty = new SimpleObjectProperty<>();

    private final SimpleBooleanProperty modifiedProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty runningProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty pausedProperty = new SimpleBooleanProperty();

    private final FileChooser fileChooser = new FileChooser();
    private final MyMenu menu = new MyMenu(trainerProperty, networkProperty,
            modifiedProperty, runningProperty, pausedProperty);

    private final BorderPane root = new BorderPane(null, menu, null, null, null);

    public Window(final Stage stage) {
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> askForSave());

        fileChooser.setTitle("Choose a location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Brain files", "*.brain"));
        fileChooser.setInitialFileName("network.brain");

        displayEmptyFrame();

        menu.setNewItemAction(e -> {
            if (askForSave()) {
                displayCreateFrame();
            }
        });
        menu.setOpenItemAction(e -> {
            if (askForSave()) {
                openNetwork();
            }
        });
        menu.setSaveItemAction(e -> saveNetwork());
        menu.setCloseItemAction(e -> {
            if (askForSave()) {
                trainerProperty.set(null);
                networkProperty.unbind();
                networkProperty.set(null);
                modifiedProperty.set(false);
                displayEmptyFrame();
            }
        });
        menu.setQuitItemAction(e -> {
            if (askForSave()) {
                Platform.exit();
            }
        });
        menu.setTrainItemAction(e -> {
            trainerProperty.get().train();
            modifiedProperty.set(true);
            runningProperty.set(true);
        });
        menu.setContinueItemAction(e -> {
            trainerProperty.get().continuate();
            pausedProperty.set(false);
        });
        menu.setPauseItemAction(e -> {
            trainerProperty.get().pause();
            pausedProperty.set(true);
        });
        menu.setStopItemAction(e -> {
            trainerProperty.get().stop();
            runningProperty.set(false);
        });
        menu.setTestItemAction(e -> {
            trainerProperty.get().test();
            runningProperty.set(true);
        });
        menu.setPredictItemAction(e -> {

        });
        menu.setAboutItemAction(e -> new About());
    }

    private boolean askForSave() {
        if (modifiedProperty.get()) {
            final Alert alert = new Alert(AlertType.CONFIRMATION, "Save the current network ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.setHeaderText("The current network has not been saved");
            alert.initStyle(StageStyle.UNDECORATED);
            final ButtonType answer = alert.showAndWait().get();
            if (answer == ButtonType.CANCEL) {
                return false;
            }
            if (answer == ButtonType.YES) {
                return saveNetwork();
            }
        }

        return true;
    }

    private void openNetwork() {
        try {
            final Network network = NetworkDAO.open(fileChooser.showOpenDialog(root.getScene().getWindow()));
            if (network != null) {
                displayNetworkFrame(network);
            }
        } catch (final Exception ex) {
            final Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("An error occured :\n" + ex.getMessage());
            alert.showAndWait();
        }
    }

    private boolean saveNetwork() {
        try {
            final File file = fileChooser.showSaveDialog(root.getScene().getWindow());
            if (file != null) {
                NetworkDAO.save(networkProperty.get(), file);
                modifiedProperty.set(false);
                return true;
            }
        } catch (final Exception ex) {
            final Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("An error occured :\n" + ex.toString());
            ex.printStackTrace();
            alert.showAndWait();
        }

        return false;
    }

    private void displayCreateFrame() {
        try {
            final CreateFrame createFrame = new CreateFrame(this);
            root.setCenter(createFrame);

            networkProperty.bind(createFrame.getNetworkProperty());
            modifiedProperty.set(true);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayEmptyFrame() {
        root.setCenter(new EmptyFrame(menu.getOpenItemActionProperty(), menu.getNewItemActionProperty()));
    }

    public void displayNetworkFrame(final Network network) {
        root.setCenter(new NetworkFrame(network));

        networkProperty.unbind();
        networkProperty.set(network);
        trainerProperty.set(new Trainer(network, runningProperty));
    }
}

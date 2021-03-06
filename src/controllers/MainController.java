package controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import managers.FXMLScenes;
import managers.StageManager;
import methodclasses.SpectrumImageChart;
import pojo.MetadataMedia;
import pojo.Song;
import stages.Main;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {


    StageManager stageManager;
    Window window;

    @FXML
    TextFlow songTextFlow;
    @FXML
    TableView<MetadataMedia> songInfoTV;

    @FXML
    MenuItem guideMI;
    @FXML
    MenuItem aboutMI;
    @FXML
    MenuItem openMI;
    @FXML
    MenuItem closeMI;
    @FXML
    MenuItem saveMI;

    @FXML
    private VBox mainVBox;
    @FXML
    public ProgressBar progressBar;

    @FXML
    Button analyzeButton;
    private Song song;
    private Text songPrelude;
    private Timeline timeline;

    private Font font = Font.font("Palatino Linotype", FontWeight.BOLD, 17);
    private ObservableList<MetadataMedia> tableListMetadata = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        window = Main.window;
        setEffects();
        songInfoTV.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("key"));
        songInfoTV.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("value"));
        setTextFlow();
        openMI.setOnAction(event -> {
            try {
                openAction();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        });

        saveMI.setOnAction(event -> {
            if (song != null && song.getFftR() != null) {
                try {
                    song.getWaveSound().generateNewSoundFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        analyzeButton.setOnAction(event -> {
            if (song != null) {
                progressBar.progressProperty().bind(SpectrumImageChart.progress);
                analyzeButton.setStyle("");
                colorInNode(analyzeButton);
                SpectrumController.song = song;
                stageManager = new StageManager(FXMLScenes.SPECTRUM);
                try {
                    stageManager.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                stageManager = new StageManager(FXMLScenes.ALERT_DIALOG);
                try {
                    stageManager.dialogStart(FXMLScenes.ALERT_DIALOG, "No se ha seleccionado un archivo.");
                } catch (Exception e) {

                }
            }
        });

    }


    private void setEffects() {
        DropShadow dropShadow = new DropShadow(
                5.0, 5, 7, Color.color(0.4, 0.5, 0.5, 0.7));
        songInfoTV.setEffect(dropShadow);
        songTextFlow.setEffect(dropShadow);
    }



    private void setTextFlow() {
        songPrelude = new Text("No hay archivo de audio cargado");
        songPrelude.setFont(font);
        songTextFlow.setLineSpacing(5);
        songTextFlow.setTextAlignment(TextAlignment.CENTER);
        songTextFlow.getChildren().add(songPrelude);
    }

    @FXML
    private void openAction() throws IOException, UnsupportedAudioFileException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un Archivo de Audio");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Audio", "*.wav", "*.mp3"));
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            if (AudioSystem.getAudioFileFormat(selectedFile).getFormat().getChannels() == 2) {
                song = new Song(selectedFile.getPath());
                fadeInNode(songTextFlow);
                songPrelude.setFill(Color.CADETBLUE);
                songPrelude.setText(selectedFile.getPath());
                songInfoTV.getItems().clear();
                getMetadata(new Media(selectedFile.toURI().toString()));
                getAudioInputInfo(AudioSystem.getAudioFileFormat(selectedFile));
                fadeInNode(songInfoTV);
                songInfoTV.setItems(tableListMetadata);
            } else {
                JOptionPane.showMessageDialog(null,
                        "El archivo es monoaural. Debe ser" +
                        " un archivo de audio Estéreo");
            }
        } else
            JOptionPane.showMessageDialog(null, "No se seleccionó ningún archivo.");
    }

    private void getAudioInputInfo(AudioFileFormat audioFileFormat) {
        tableListMetadata.add(new MetadataMedia("Longitud en Bytes", audioFileFormat.getByteLength()));
        tableListMetadata.add(new MetadataMedia("Canales", audioFileFormat.getFormat().getChannels()));
        tableListMetadata.add(new MetadataMedia("Codificación", audioFileFormat.getFormat().getEncoding()));
        tableListMetadata.add(new MetadataMedia("Tamaño de Frame", audioFileFormat.getFormat().getFrameSize()));
        tableListMetadata.add(new MetadataMedia("Longitud en Frame", audioFileFormat.getFrameLength()));
        tableListMetadata.add(new MetadataMedia("Frecuencia de Muestreo", audioFileFormat.getFormat().getSampleRate()));
        tableListMetadata.add(new MetadataMedia("Frecuencia de Frames", audioFileFormat.getFormat().getFrameRate()));
        tableListMetadata.add(new MetadataMedia("Tamaño en Bits", audioFileFormat.getFormat().getSampleSizeInBits()));
    }

    private void fadeInNode(Node node) {
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
                new KeyFrame(new Duration(1200), new KeyValue(node.opacityProperty(), 1.0)));
        timeline.play();
    }

    private void colorInNode(Node node) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0);
        node.setEffect(colorAdjust);
        timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(colorAdjust.brightnessProperty(),
                colorAdjust.brightnessProperty().getValue(), Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(1), new KeyValue(colorAdjust.brightnessProperty(),
                        0, Interpolator.LINEAR)));
        timeline.play();
    }

    private void errorTextFlow() {
        Timeline timeline = new Timeline();
        KeyValue initK = new KeyValue(songTextFlow.backgroundProperty(),
                null);
        KeyValue endK = new KeyValue(songTextFlow.backgroundProperty(),
                new Background(new BackgroundFill(Color.RED, null, null)));
        KeyFrame initFrame = new KeyFrame(Duration.ZERO, initK),
                endFrame = new KeyFrame(Duration.millis(30), endK);
        timeline.getKeyFrames().addAll(initFrame, endFrame);
        timeline.setAutoReverse(true);
        timeline.play();
    }

    public void getMetadata(Media media) {
        ObservableMap<String, Object> metadata = media.getMetadata();
        if (metadata.size() != 0)
            metadata.forEach((s, o) -> generateNewText(s, o));
    }

    private void generateNewText(String key, Object valueAdded) {
        try {
            if (key.equalsIgnoreCase("image"))
                tableListMetadata.add(new MetadataMedia(key, (Image) valueAdded));
            else
                tableListMetadata.add(new MetadataMedia(key, valueAdded.toString()));
        } catch (Exception e) {
        }
    }
}

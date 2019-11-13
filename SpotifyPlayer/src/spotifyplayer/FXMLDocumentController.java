package spotifyplayer;

import com.sun.javafx.collections.ObservableListWrapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.imageio.ImageIO;

public class FXMLDocumentController implements Initializable 
{
    @FXML
    private Label artistLabel;
    
    @FXML
    private Label albumLabel;
    
    @FXML
    private ImageView albumCoverImageView;

    @FXML
    private TextField searchArtistTextField;
    
    @FXML
    private Button previousAlbumButton;
    
    @FXML
    private Button nextAlbumButton;
    
    @FXML
    private TableView tracksTableView;

    @FXML
    private Button playButton;
    
    @FXML
    private Slider trackSlider;
    
    @FXML
    private Label trackTimeLabel;
        
    @FXML
    private ProgressIndicator progress;
    
    @FXML
    private ImageView artistPictureImageView;
    
    MediaPlayer mediaPlayer = null;
    int currentAlbum = 0;
    ArrayList<Album> albums = null;
    boolean isSliderMoving = false;
    boolean isPlaying = false;
    double currentTime = 0.0;
    Album albumCopy;
    String secondStr;
    String artistPictureUrl;
    
    @FXML
    private void saveButtonPressed(ActionEvent event) 
    {
        FileChooser fileChooser = new FileChooser();
        
        fileChooser.setTitle("Save Album Cover");
        FileChooser.ExtensionFilter[] extFilter = {new FileChooser.ExtensionFilter("*.png", "*.png"), new FileChooser.ExtensionFilter("*.jpg", "*.jpg")};
        fileChooser.getExtensionFilters().addAll(extFilter);
        fileChooser.setInitialDirectory(new File("./images"));
        
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) 
        {
            try 
            {
                BufferedImage image = SwingFXUtils.fromFXImage(albumCoverImageView.getImage(), null);
                ImageIO.write((BufferedImage)image, "png", new File(file.getAbsolutePath()));
            } 
            catch (Exception e) 
            {
                System.err.println("Could not save image.");
            }
        }
    }
    
    
    @FXML
    private void exitButtonPressed(ActionEvent event) 
    {
        try
        {
            isSliderMoving = false;
            mediaPlayer.stop();
            mediaPlayer.dispose();
            Platform.exit();
        }
        catch(Exception e)
        {
            System.err.println("Could not close media.");
            Platform.exit();
        }
    }
    
    @FXML
    private void handlePlayButtonAction(ActionEvent event) 
    {
        if (playButton.getText().equals("Play"))
        {
            try
            {
                if(mediaPlayer.getMedia() != null && mediaPlayer != null)
                {
                    playButton.setText("Pause");
                    mediaPlayer.play();
                    isPlaying = true;
                    isSliderMoving = true;
                }
                else
                {
                    playButton.setText("Play");
                    isPlaying = false;
                    isSliderMoving = false;
                    mediaPlayer.pause();
                }
            }
            catch(Exception e)
            {
                System.err.println("Could not play song.");
                
                artistLabel.setTextFill(Color.web("#ff0000"));
                artistLabel.setText("Error!");
                
                albumLabel.setTextFill(Color.web("#ff0000"));
                albumLabel.setText("You must play a song from the list first.");
            }
        }
        else
        {
            try
            {
                if(mediaPlayer.getMedia() != null && mediaPlayer != null)
                {
                    playButton.setText("Play");
                    mediaPlayer.pause();
                    isPlaying = false;
                    isSliderMoving = false; 
                }
                else
                {
                    playButton.setText("Pause");
                }
            }
            catch(Exception e)
            {
                System.err.println("Could not pause song.");
            }
        }
    }

    @FXML
    private void handlePreviousButtonAction(ActionEvent event) 
    {
        artistLabel.setTextFill(Color.web("#000000"));
        albumLabel.setTextFill(Color.web("#000000"));
        
        currentTime = trackSlider.getValue();
        
        currentAlbum--;
        
        if(currentAlbum < 0)
        {
            currentAlbum = albums.size() - 1;
            displayAlbum(currentAlbum);
        }
        else
        {
            displayAlbum(currentAlbum);
        }
        
        int seconds;
        String currentTimeString = String.valueOf(Math.floor(currentTime));
        String currentTimeStringWithoutDecimal = "0:0" + currentTimeString.replace(".0", "");
        if(currentTimeStringWithoutDecimal.length() == 4)
        {
            seconds = Integer.parseInt(currentTimeStringWithoutDecimal.charAt(2) + "" + currentTimeStringWithoutDecimal.charAt(3));
        }
        else
        {
            seconds = Integer.parseInt(currentTimeStringWithoutDecimal.charAt(3) + "" + currentTimeStringWithoutDecimal.charAt(4));
        }   
        trackSlider.setValue(currentTime);
        secondStr = "0:" + ((seconds < 10) ? "0" + seconds : "" + seconds);
        trackTimeLabel.setText(secondStr + " / 0:30");
    }

    @FXML
    private void handleNextButtonAction(ActionEvent event) 
    {
        artistLabel.setTextFill(Color.web("#000000"));
        albumLabel.setTextFill(Color.web("#000000"));
        
        currentTime = trackSlider.getValue();
        
        currentAlbum++;
        
        if(currentAlbum > albums.size() - 1)
        {
            currentAlbum = 0;
            displayAlbum(currentAlbum);
        }
        else
        {
            displayAlbum(currentAlbum);
        }
        
        int seconds;
        String currentTimeString = String.valueOf(Math.floor(currentTime));
        String currentTimeStringWithoutDecimal = "0:0" + currentTimeString.replace(".0", "");
        if(currentTimeStringWithoutDecimal.length() == 4)
        {
            seconds = Integer.parseInt(currentTimeStringWithoutDecimal.charAt(2) + "" + currentTimeStringWithoutDecimal.charAt(3));
        }
        else
        {
            seconds = Integer.parseInt(currentTimeStringWithoutDecimal.charAt(3) + "" + currentTimeStringWithoutDecimal.charAt(4));
        }   
        trackSlider.setValue(currentTime);
        secondStr = "0:" + ((seconds < 10) ? "0" + seconds : "" + seconds);
        trackTimeLabel.setText(secondStr + " / 0:30");
    }
    
    @FXML
    private void handleSearchButtonAction(ActionEvent event) 
    {
        progress.setVisible(true);
        searchArtistTextField.setDisable(true);
        previousAlbumButton.setDisable(true);
        nextAlbumButton.setDisable(true);

        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.submit(new Task<Void>()
        {
            @Override
            protected Void call() throws Exception 
            {
                searchArtist(searchArtistTextField.getText());
                return null;
            }

            @Override
            protected void succeeded() 
            {
                progress.setVisible(false);
                searchArtistTextField.setDisable(false);
                
                if (albums.size() > 0)
                {
                    displayAlbum(0);
                }
                else if (albums.isEmpty())
                {
                    artistLabel.setTextFill(Color.web("#ff0000"));
                    albumLabel.setTextFill(Color.web("#ff0000"));
                    cancelled();
                }
            }

            @Override
            protected void cancelled() 
            { 
                progress.setVisible(false);
                searchArtistTextField.setDisable(false);

                artistLabel.setText("Error!");
                albumLabel.setText("Error retrieving " + searchArtistTextField.getText());
            }
        });
    }
    
    private void displayAlbum(int albumIndex)
    {
        if (albumIndex >=0 && albumIndex < albums.size())
        {
            Album album = albums.get(albumIndex);
            albumCopy = album;
            System.out.println(album);
            
            artistLabel.setTextFill(Color.web("#000000"));
            albumLabel.setTextFill(Color.web("#000000"));
            artistLabel.setText(album.getArtistName());
            albumLabel.setText(album.getAlbumName());
            
            ArrayList<TrackForTableView> tracks = new ArrayList<>();
            for (int i=0; i<album.getTracks().size(); ++i)
            {
                TrackForTableView trackForTable = new TrackForTableView();
                Track track = album.getTracks().get(i);
                trackForTable.setTrackNumber(track.getNumber());
                trackForTable.setTrackTitle(track.getTitle());
                trackForTable.setTrackPreviewUrl(track.getUrl());
                tracks.add(trackForTable);
            }
            tracksTableView.setItems(new ObservableListWrapper(tracks));
            
            if (albums.size() > 1)
            {
                previousAlbumButton.setDisable(false);
                nextAlbumButton.setDisable(false);
            }
            else
            {
                previousAlbumButton.setDisable(true);
                nextAlbumButton.setDisable(true);
            }
            
            Image coverImage = new Image(album.getImageURL());
            albumCoverImageView.setImage(coverImage);
            
            trackSlider.setValue(0.0);
            trackTimeLabel.setText("0:00 / 0:30");
        }
    }
    
    private void searchArtist(String artistName)
    {
        try
        {
            artistPictureUrl = SpotifyController.getArtistPicture(artistName);
            Image artistImage = new Image(artistPictureUrl);
            artistPictureImageView.setImage(artistImage);
        }
        catch(Exception e)
        {
            System.err.println("Could not load artist picture");
        }
        String artistId = SpotifyController.getArtistId(artistName);
        currentAlbum = 0;
        albums = SpotifyController.getAlbumDataFromArtist(artistId);
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        TableColumn<TrackForTableView, Number> trackNumberColumn = new TableColumn("#");
        trackNumberColumn.setCellValueFactory(new PropertyValueFactory("trackNumber"));
        
        TableColumn trackTitleColumn = new TableColumn("Title");
        trackTitleColumn.setCellValueFactory(new PropertyValueFactory("trackTitle"));
        trackTitleColumn.setPrefWidth(250);
        
        TableColumn playColumn = new TableColumn("Preview");
        playColumn.setCellValueFactory(new PropertyValueFactory("trackPreviewUrl"));
        Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>> cellFactory = new Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>>()
        {
            @Override
            public TableCell<TrackForTableView, String> call(TableColumn<TrackForTableView, String> param) 
            {
                final TableCell<TrackForTableView, String> cell = new TableCell<TrackForTableView, String>()
                {
                    final Button playButtonInTable = new Button("Play");

                    @Override
                    public void updateItem(String item, boolean empty)
                    {
                        if (item != null && item.equals("") == false)
                        {
                            playButtonInTable.setOnAction(event -> {
                                if (mediaPlayer != null)
                                {
                                    mediaPlayer.stop();                                    
                                }
                                
                                artistLabel.setTextFill(Color.web("#000000"));
                                artistLabel.setText(albumCopy.getArtistName());

                                albumLabel.setTextFill(Color.web("#000000"));
                                albumLabel.setText(albumCopy.getAlbumName());
                                
                                isPlaying = true;
                                isSliderMoving = true;
                                Media music = new Media(item);
                                mediaPlayer = new MediaPlayer(music);

                                mediaPlayer.play();
                                trackSlider.setValue(0);
                                playButton.setText("Pause");
                                trackTimeLabel.setText("0:00 / 0:30");
                            });
    
                            setGraphic(playButtonInTable);
                        }
                        else
                        {                        
                            setGraphic(null);
                        }

                        setText(null);
                    }
                };
                return cell;
            }
        };
        playColumn.setCellFactory(cellFactory);
        
        tracksTableView.getColumns().setAll(trackNumberColumn, trackTitleColumn, playColumn);
        
        searchArtist("pink floyd");
        displayAlbum(0);
        
        trackSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {                 
                try
                {
                    int seconds;
                    currentTime = trackSlider.getValue();
                    mediaPlayer.seek(Duration.seconds(currentTime));
                    
                    String currentTimeString = String.valueOf(Math.floor(currentTime));
                    String currentTimeStringWithoutDecimal = "0:0" + currentTimeString.replace(".0", "");
                    if(currentTimeStringWithoutDecimal.length() == 4)
                    {
                        seconds = Integer.parseInt(currentTimeStringWithoutDecimal.charAt(2) + "" + currentTimeStringWithoutDecimal.charAt(3));
                    }
                    else
                    {
                        seconds = Integer.parseInt(currentTimeStringWithoutDecimal.charAt(3) + "" + currentTimeStringWithoutDecimal.charAt(4));
                    }   
                    seconds++;
                    secondStr = "0:" + ((seconds < 10) ? "0" + seconds : "" + seconds);
                    trackTimeLabel.setText(secondStr + " / 0:30");
                    
                    playButton.setText("Pause");
                    isSliderMoving = true;
                }
                catch(Exception e)
                {
                    artistLabel.setTextFill(Color.web("#ff0000"));
                    artistLabel.setText("Error!");

                    albumLabel.setTextFill(Color.web("#ff0000"));
                    albumLabel.setText("You must play a song from the list first.");
                }
            }          
        });
            
        ScheduledExecutorService mainLoopExecutor = Executors.newSingleThreadScheduledExecutor();
        mainLoopExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() 
            {
                if(isSliderMoving)
                {
                    Platform.runLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            currentTime = trackSlider.getValue();
                            trackSlider.setValue(currentTime + 1.0);
                            
                            String timeString = trackTimeLabel.getText();
                            int seconds = Integer.parseInt(timeString.charAt(2) + "" + timeString.charAt(3));    
                            seconds++;
                            String secondStr = "0:" + ((seconds < 10) ? "0" + seconds : "" + seconds);
                            trackTimeLabel.setText(secondStr + " / 0:30");
                            
                            if(trackSlider.getValue() == 30.0)
                            {
                                trackSlider.setValue(0.0);
                                playButton.setText("Play");
                                isSliderMoving = false;
                                mediaPlayer.pause();
                                trackTimeLabel.setText("0:00 / 0:30");
                            }
                        }
                    });
                }
                else
                {
                    Platform.runLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            currentTime = trackSlider.getValue();
                            trackSlider.setValue(currentTime);

                            String timeString = trackTimeLabel.getText();
                            int seconds = Integer.parseInt(timeString.charAt(2) + "" + timeString.charAt(3));
                            secondStr = "0:" + ((seconds < 10) ? "0" + seconds : "" + seconds);

                            trackTimeLabel.setText(secondStr + " / 0:30");
                        }
                    });
                }
            }
        }
        , 0, 1, TimeUnit.SECONDS);
    }
}

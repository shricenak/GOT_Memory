package memory;

import java.nio.file.Paths;
import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * Plays specified song. Written browsing oracle's documentation and 
 * a few online tutorials.
 * 
 * @author Steven Hricenak
 */
public class SongPlayer extends Application{

    private Media song;
    private MediaPlayer mp;
    private int songID;
    
    public SongPlayer(int songID){
        new JFXPanel(); //initialize tools needed for audio playing
        this.songID = songID; 
    }
   
    /**
     * Stops the music.
     */
    public void pause(){
        mp.pause();
    }
    
    /**
     * Loads, plays, and loops the given song. 
     */
    @Override
    public void start(Stage primaryStage){
        song = new Media(Paths.get("audio/song" + songID + ".mp3").toUri().toString());
        mp = new MediaPlayer(song);
        mp.play();
        mp.setCycleCount(MediaPlayer.INDEFINITE);
    }
}

import greenfoot.*;
import greenfoot.GreenfootSound;

/**
 * SoundManager is a utility class for managing background music playback and volume.
 * It supports initialization, volume adjustment, and resetting the audio system.
 * 
 * This class uses a static GreenfootSound object to ensure that music plays globally
 * across different worlds and scenes in the game.
 * 
 * Call SoundManager.initialize() to start music.
 * Call SoundManager.setVolume(double) to change volume (0.0 to 1.0).
 * Call SoundManager.getVolume() to retrieve the current volume level.
 * Call SoundManager.reset() to stop and remove the music.
 * 
 * @author Charlie Cruz
 */
public class SoundManager {
    private static GreenfootSound music = null; // Global background music
    private static boolean initialized = false; // Ensures music is initialized once
    private static double volume = 0.5; // Volume stored as a value between 0 and 1

    /**
     * Initializes the background music if not already started.
     * Loads the "temporary.mp3" file and plays it in a loop.
     */
    public static void initialize() {
        if (!initialized) {
            music = new GreenfootSound("temporary.mp3"); // Load music file
            music.setVolume((int)(volume * 100)); // Use stored volume
            music.playLoop(); // Start looping playback
            initialized = true; // Mark as initialized
        }
    }

    /**
     * Sets the global music volume.
     * @param newVolume The volume level between 0.0 (mute) and 1.0 (full volume).
     */
    public static void setVolume(double newVolume) {
        if (newVolume < 0.0) newVolume = 0.0;
        if (newVolume > 1.0) newVolume = 1.0;
        volume = newVolume;
        if (music != null) {
            music.setVolume((int)(volume * 100)); // Convert to 0–100 range
        }
    }

    /**
     * Gets the current volume level.
     * @return The current volume as a double between 0.0 and 1.0.
     */
    public static double getVolume() {
        return volume; // Return the stored volume directly
    }

    /**
     * Stops the background music and resets the SoundManager.
     * Useful for changing tracks or stopping music entirely.
     */
    public static void reset() {
        if (music != null) {
            music.stop(); // Stop current music
            music = null; // Remove reference
        }
        initialized = false; // Allow re-initialization
    }
}
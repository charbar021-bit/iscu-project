import java.util.HashMap;
import java.util.Map;
/**
 * SettingsManager handles global volume 
 * 
 * @author Charlie Cruz
 */
public class SettingsManager {
    private static double volume = 0.5; // Default volume

    /**
     * Sets the master volume (0.0 to 1.0).
     * @param newVolume New volume level
     */
    public static void setVolume(double newVolume) {
        volume = Math.max(0.0, Math.min(1.0, newVolume));
    }

    /**
     * Retrieves the current master volume.
     * @return Volume as a double (0.0 to 1.0)
     */
    public static double getVolume() {
        return volume;
    }
}

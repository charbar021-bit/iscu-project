/**
 * Defines behavior logic for a weapon once its been spawned into the world as a weapon actor
 * Provides hooks for beginning and continuous updates to allow for targetting, animations, etc.
 * 
 * @author Noah
 */
public interface WeaponBehavior {
    /**
     * Intializies weapon behavior, called once its been created
     * 
     * @param actor Weapon actor this behaviors attached to
     * @param mouseX X coord of mouse once this was called
     * @param mouseY Y coord of mosue once this was called
     */
    void init(WeaponActor actor, int mouseX, int mouseY);

    /**
     * Called every frame to update weapons behavior
     * 
     * @param actor Weapon actor to update
     */
    void update(WeaponActor actor);
}
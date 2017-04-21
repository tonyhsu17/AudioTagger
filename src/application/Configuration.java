package application;

import support.Utilities.Tag;

public class Configuration
{
    /**
     * Singleton Initialization
     */
    private static Configuration self = new Configuration();
    
    private byte sessionSettings;
    public static byte PROPAGATE_SAVE_ARTIST_MASK = 0x01;
    public static byte PROPAGATE_SAVE_ALBUM_MASK = 0x02;
    public static byte PROPAGATE_SAVE_ALBUM_ARTIST_MASK = 0x04;
    public static byte PROPAGATE_SAVE_YEAR_MASK = 0x08;
    public static byte PROPAGATE_SAVE_GENRE_MASK = 0x10;
    public static byte PROPAGATE_SAVE_COMMENT_MASK = 0x20;
    public static byte PROPAGATE_SAVE_ALBUM_ART_MASK = 0x40;
    
    private Configuration()
    {
        sessionSettings = 0xF;
    }
    
    /** 
     * @return Singleton of Configuration
     */
    public static Configuration getInstance()
    {
        return self;
    }
    
    // quick turn off for now
    public void turnOffPropagateSave()
    {
        sessionSettings = 0;
    }
    
    /**
     * @param tag
     */
    public void togglePropagateSave(Tag tag)
    {
        switch(tag)
        {
            case Album:
                sessionSettings ^= PROPAGATE_SAVE_ALBUM_MASK;
                break;
            case AlbumArt:
                sessionSettings ^= PROPAGATE_SAVE_ALBUM_ART_MASK;
                break;
            case AlbumArtist:
                sessionSettings ^= PROPAGATE_SAVE_ALBUM_ARTIST_MASK;
                break;
            case Artist:
                sessionSettings ^= PROPAGATE_SAVE_ARTIST_MASK;
                break;
            case Comment:
                sessionSettings ^= PROPAGATE_SAVE_COMMENT_MASK;
                break;
            case Genre:
                sessionSettings ^= PROPAGATE_SAVE_GENRE_MASK;
                break;
            case Year:
                sessionSettings ^= PROPAGATE_SAVE_YEAR_MASK;
                break;
            default:
                break;
        }
    }
    
    public boolean isAnyPropagateSaveOn()
    {
        return sessionSettings > 0 ? true : false;
    }
    
    public boolean isPropagateSaveOn(Tag tag)
    {
        boolean flag = false;
        switch(tag)
        {
            case Album:
                flag = (sessionSettings & PROPAGATE_SAVE_ALBUM_MASK) == PROPAGATE_SAVE_ALBUM_MASK ? true : false;
                break;
            case AlbumArt:
                flag = (sessionSettings & PROPAGATE_SAVE_ALBUM_ART_MASK) == PROPAGATE_SAVE_ALBUM_ART_MASK ? true : false;
                break;
            case AlbumArtist:
                flag = (sessionSettings & PROPAGATE_SAVE_ALBUM_ARTIST_MASK) == PROPAGATE_SAVE_ALBUM_ARTIST_MASK ? true : false;
                break;
            case Artist:
                flag = (sessionSettings & PROPAGATE_SAVE_ARTIST_MASK) == PROPAGATE_SAVE_ARTIST_MASK ? true : false;
                break;
            case Comment:
                flag = (sessionSettings & PROPAGATE_SAVE_COMMENT_MASK) == PROPAGATE_SAVE_COMMENT_MASK ? true : false;
                break;
            case Genre:
                flag = (sessionSettings & PROPAGATE_SAVE_GENRE_MASK) == PROPAGATE_SAVE_GENRE_MASK ? true : false;
                break;
            case Year:
                flag = (sessionSettings & PROPAGATE_SAVE_YEAR_MASK) == PROPAGATE_SAVE_YEAR_MASK ? true : false;
                break;
            default:
                break;
        }
//        System.out.println("Got PropagateSave: " + tag + " " + flag);
        return flag;
    }
}

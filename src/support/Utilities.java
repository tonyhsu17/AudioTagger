package support;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.beans.property.adapter.JavaBeanBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;


public class Utilities
{
    public static final String KEYWORD_DIFF_VALUE = "<Different Values>";
    public static final String HEADER_ALBUM = "-- Folder: ";
        
    
    public static enum Tag {
        FileName, Title, Artist, Album, AlbumArtist, Track, Year, Genre, Comment, AlbumArt, AlbumArtMeta
    }
    
    public static int findIntValue(String str)
    {
        int endIndex = -1;
        if(((endIndex = str.toLowerCase().indexOf("th")) != -1) ||
            ((endIndex = str.toLowerCase().indexOf("st")) != -1) ||
            ((endIndex = str.toLowerCase().indexOf("nd")) != -1) ||
            ((endIndex = str.toLowerCase().indexOf("rd")) != -1))
        {
            str = str.substring(0, endIndex);
            String[] splitStr = str.split(" ");
            for(int i = splitStr.length - 1; i >= 0; i--)
            {
                try
                {
                    return Integer.valueOf(splitStr[i]);
                }
                catch(NumberFormatException e)
                {
                    
                } 
            }
        }
        return -1;
    }
    
    public static String[] splitName(String fullName)
    {
        String[] splitName = {"", ""};
        if(fullName == null || fullName.isEmpty())
        {
            return splitName;
        }
        splitName = fullName.split(" ");
        String lastName = "";
        String firstName = "";
        if(splitName.length > 1)
        {
            lastName = splitName[splitName.length - 1]; // only last part is last name
            firstName = splitName[0];
            for(int i = 1; i < splitName.length - 1; i++) // skip last part as thats last name
            {
                firstName += " " + splitName[i];
            }
        }
        else
        {
            firstName = splitName[0];
        }
        
        return new String[] {firstName, lastName};
    }
    
    public static String[] splitBySeparators(String string)
    {
        String[] splitArtists = string.split("(, )|( & )");
        // TODO get feat and etc to split by too
        return splitArtists;
    }
    
    public static String createQuestionMarks(int num)
    {
        if(num == 0)
        {
            return "";
        }
        else if(num == 1)
        {
            return "?";
        }
        else if(num == 2)
        {
            return "?, ?";
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(; i < num - 1; i++)
        {
            sb.append("?, ");
        }
        sb.append("?");
        return sb.toString();
    }
    
    /**
     * @return Same given value or "Different"
     */
    public static String getComparedName(String s1, String s2)
    {
        if(s1 == null || s2 == null || !s1.equals(s2))
        {
            return KEYWORD_DIFF_VALUE;
        }
        else
        {
            return s1;
        }
    }
    
    public static boolean isKeyword(String str)
    {
        if(str.equals(KEYWORD_DIFF_VALUE))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * @return Same given value or "Different"
     */
    public static Image getComparedImage(Image i1, Image i2)
    {
        BufferedImage diffImage = null;
        try
        {
            diffImage = ImageIO.read(new File("Resources/differentImage.jpg"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        
        if(i1 == null && i2 == null)
        {
            return null;
        }
        else if((i1 != null && i2 == null) || (i1 == null && i2 != null))
        {
            return SwingFXUtils.toFXImage(diffImage, null);
        }
        else if(i1.getHeight() == i2.getHeight() && i1.getWidth() == i2.getWidth())
        {
            for(int i = 0; i < i1.getWidth(); i++)
            {
                for(int j = 0; j < i1.getHeight(); j++)
                {
                    if (i1.getPixelReader().getArgb(i, j) != i2.getPixelReader().getArgb(i, j))
                    {
                        return SwingFXUtils.toFXImage(diffImage, null);
                    }
                }
            }
        }
        
        return i1;
    }
    
    public static boolean isKeyword(Image i)
    {
        if(i == null)
        {
            return false;
        }
        else
        {
            BufferedImage buffImage;
            try
            {
                buffImage = ImageIO.read(new File("Resources/differentImage.jpg"));
                Image keyword = SwingFXUtils.toFXImage(buffImage, null);
                
                if(i.getHeight() == buffImage.getHeight() && i.getWidth() == keyword.getWidth())
                {
                    for(int h = 0; h < i.getHeight(); h++)
                    {
                        for(int w = 0; w < i.getWidth(); w++)
                        {
                            if (i.getPixelReader().getArgb(h, w) != keyword.getPixelReader().getArgb(h, w))
                            {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                else
                {
                    return false;
                }
                
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
    }
    
    public static Image scaleImage(Image source, int targetWidth, int targetHeight, boolean preserveRatio) {
        ImageView imageView = new ImageView(source);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(preserveRatio);
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
        return imageView.snapshot(null, null);
    }
    
    public static File saveImage(Image img)
    {
        // SwingFXUtils.fromFXImage(img, null) is bugged, as it is including an alpha channel for jpg.
        // so instead, manually make a new copy
        try
        {
            BufferedImage newImage = new BufferedImage((int)img.getWidth(), (int)img.getHeight(), BufferedImage.TYPE_INT_RGB);
            PixelReader pr = img.getPixelReader();
            for(int x = 0; x < img.getWidth(); x++)
            {
                for(int y = 0; y < img.getHeight(); y++)
                {
                    Color c = pr.getColor(x, y);
                    java.awt.Color awtColor = new java.awt.Color((float)c.getRed(), (float)c.getGreen(), (float)c.getBlue(), 0);
                    newImage.setRGB(x, y, awtColor.getRGB());
                }
            }
            File temp = new File("temp.jpg");
            ImageIO.write(newImage, "jpg", temp);
            return temp;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getCommaSeparatedStringWithAnd(List<String> list)
    {
        if(list.isEmpty())
        {
            return "";
        }
        else if(list.size() == 1)
        {
            return (String)list.get(0);
        }
        else if(list.size() == 2)
        {
            return list.get(0) + " & " + list.get(1);
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(; i < list.size() - 2; i++)
        {
            sb.append(list.get(i) + ", ");
        }
        sb.append(list.get(i) + " & " + list.get(i + 1));
        return sb.toString();
    }
    
    public static boolean convertToBoolean(String str)
    {
        if(str.toLowerCase().startsWith("y") || str.toLowerCase().startsWith("t"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

package support.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class ImageUtil
{

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
}

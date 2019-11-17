package no.ntnu.imt3281.ludo.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageManager {

    /**
     * Check if path to a file is an image
     * @param path the path to the file in the system
     * @return true if filepath is image, else false
     */
    public static boolean isImage(String path){
        System.out.println(path);
        BufferedImage image;
        try{
            // imageIO.read returns "null" if image could not be read
            image = ImageIO.read(new File(path));
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }

        // if is not image
        if(image == null){
            return false;
        }

        return true;
    }

    public static byte[] convertImageToBytes(String path){
        BufferedImage image;
        byte[] newFileBytes;

        // first we read image
        try{
            // imageIO.read returns "null" if image could not be read
            image = ImageIO.read(new File(path));
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }

        /*
        // if image is more than 256x256 pixels, we resize to fit these bounds
        if(image.getWidth() > 256 || image.getHeight() > 256){
            int width = image.getWidth();
            int height = image.getHeight();

            // we create a copy, resize it and then copy it back to the other image variable
            BufferedImage copyImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(256.0 / width, 256.0 / height);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            image = scaleOp.filter(image, copyImage);
        }

         */

        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            newFileBytes = bos.toByteArray();
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }

        return newFileBytes;
    }

    public static javafx.scene.image.Image convertBytesToImage(byte[] imageData){
        // we decode string into bytes[]
        //byte[] imageBytes = Base64.getDecoder().decode(base64);
        /*
        BufferedImage img;

        try{
            img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
        */

        return new javafx.scene.image.Image(new ByteArrayInputStream(imageData));
    }
}

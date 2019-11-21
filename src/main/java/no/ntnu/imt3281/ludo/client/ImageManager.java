package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.Exceptions.InvalidImageException;

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

    /**
     * Convert an Image to a byte array
     * @param path the full path to the image file
     * @return the image in []byte
     * @throws InvalidImageException if the file is larger than 2MB or the image could not be read
     */
    public static byte[] convertImageToBytes(String path) throws InvalidImageException {
        BufferedImage image;
        byte[] newFileBytes;

        // Image size can't exceed 2MB
        if(new File(path).length() > 2000000){
            throw new InvalidImageException("File size cannot exceed 2MB");
        }

        // first we read image
        try{
            // imageIO.read returns "null" if image could not be read
            image = ImageIO.read(new File(path));
        } catch(IOException e){
            e.printStackTrace();
            throw new InvalidImageException("Something went wrong trying to read the image");
        }

        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            newFileBytes = bos.toByteArray();
        } catch(IOException e){
            e.printStackTrace();
            throw new InvalidImageException("Something went wrong trying to read the image");
        }

        return newFileBytes;
    }

    /**
     * Convert an array of bytes to a JavaFX image object
     * @param imageData the byte array to decode
     * @return the image
     */
    public static javafx.scene.image.Image convertBytesToImage(byte[] imageData){
        return new javafx.scene.image.Image(new ByteArrayInputStream(imageData));
    }
}

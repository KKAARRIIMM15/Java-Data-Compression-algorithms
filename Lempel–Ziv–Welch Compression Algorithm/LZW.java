/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lzw;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author kareem
 */
public class LZW {
    public static void main(String[] args) throws IOException {

        BufferedImage image = ImageIO.read(new File("random.png"));

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];    int k =0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF; // Extract the blue channel, which represents intensity in grayscale
                pixels[k++] = pixel;
            }
        }

        List<Integer> compressed = compress(pixels);
        System.out.println("\n Compressed Size= "+ compressed.size() + "  Orginal Size= " + pixels.length + "\n");
  
        serialize(compressed , "compressed.ser");
        serialize(pixels , "real.ser");
        
         System.out.println("First 10 pixel intensities: ");
        for (int i = 0; i < 10; i++) {
            System.out.print(pixels[i] + " ");
        }
        System.out.println("\nFirst 10 encoded values: ");
        for (int i = 0; i < 10 && i < compressed.size(); i++) {
            System.out.print(compressed.get(i) + " ");
        }
        
        
          compressed = deSerialize("compressed.ser");
           int[] decompressedPixels = decompress(compressed);
        
        saveImage(decompressedPixels, width, height, "compressed.png");
        System.out.println("compression ratio:  "  +  (pixels.length/compressed.size()) );

    }

    
    /***********************************************************/
    
    
    public static List<Integer> compress(int[] pixels) {
        
        HashMap<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char)i), i);
        }

        String current = "";
        List<Integer> compressed = new ArrayList<>();
        int dictSize = 256;

        for (int pixel : pixels) 
        {
            char next = (char) pixel;
            String currentPlusNext = current + next;
            
            if (dictionary.containsKey(currentPlusNext)) 
                current = currentPlusNext;
            
            else {
                int output = dictionary.get(current);
                compressed.add( output );
                dictionary.put(currentPlusNext, dictSize++);
                current = String.valueOf( next );
            }
        }


        if (!current.isEmpty()) {
            compressed.add(dictionary.get(current));
        }

        return compressed;
    }
    
    
    
    public static int[] decompress(List<Integer> compressed) {

        HashMap<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, String.valueOf((char) i));     //System.out.println( (char)i);
        }

        int dictSize = 256;
        String current = String.valueOf((char) (int) compressed.remove(0));
        StringBuilder decompressed = new StringBuilder(current);
        

        for (int code : compressed) {
            String entry;
            
            if (dictionary.containsKey(code))
            {
                entry = dictionary.get(code);
            } 
            
            else if (!dictionary.containsKey(code))
            {
                entry = current + current.charAt(0);
            } 
            
            else {
                throw new IllegalArgumentException("Bad compressed code: " + code);
            }

            decompressed.append(entry);

            // Add current + entry[0] to the dictionary
            dictionary.put(dictSize++, (current + entry.charAt(0) ) );

            current = entry;
        }


        int[] decompressedPixels = new int[decompressed.length()];
        for (int i = 0; i < decompressed.length(); i++) {
            decompressedPixels[i] = decompressed.charAt(i);
        }

        return decompressedPixels;
    }
    
    public static void saveImage(int[] pixels, int width, int height, String outputPath) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = pixels[y * width + x];
                int rgbValue = pixelValue | (pixelValue << 8) | (pixelValue << 16); // Set RGB to same value (grayscale)
                image.setRGB(x, y, rgbValue);
            }
        }

        File outputFile = new File(outputPath);
        ImageIO.write(image, "png", outputFile);
        System.out.println("\n\nDecompressed image saved as " + outputPath);
    }
   
    
    static void serialize( Object compressed , String path )
    {
        try{
        FileOutputStream fileOutputStream = new FileOutputStream(path);
ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
objectOutputStream.writeObject(compressed);
        } catch(Exception ex ){}
    }
    
    
     static List<Integer> deSerialize(  String filePath )
    {
        List<Integer> deserialized = null;
        try{
        FileInputStream fileInputStream = new FileInputStream(filePath);
ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
 deserialized =  (List<Integer>)objectInputStream.readObject();
        } catch(Exception ex ){}
        return deserialized;
    }
    
}

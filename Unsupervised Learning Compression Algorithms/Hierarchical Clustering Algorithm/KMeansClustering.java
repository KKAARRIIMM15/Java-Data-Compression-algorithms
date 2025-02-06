
package com.mycompany.a_kmeans;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.util.Random;



public class KMeansClustering
{
    static class Pixel
    {
        int r,g,b,w,h;

        public Pixel(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
        
        
        public Pixel(int r, int g, int b, int w, int h) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.w = w;
            this.h = h;
        }
        
    }
    
    static class Centroid
    {
        int r,g,b;
        ArrayList<Pixel> points = new ArrayList<Pixel>();
    }
    
    public static int euclideanDistance(Pixel p1, Pixel p2) 
    {
        int dr = p1.r - p2.r;
        int dg = p1.g - p2.g;
        int db = p1.b - p2.b;
        return (int) Math.sqrt(dr * dr + dg * dg + db * db);
    }
    
    
    public static void update_all_centroids(Centroid[] allCentroids) {
        for (int i = 0; i < allCentroids.length; i++) {
            Centroid centroid = allCentroids[i];
            int centroid_size = centroid.points.size();

            if (centroid_size == 0) {
                continue; // Avoid division by zero
            }

            int r = 0, g = 0, b = 0;

            for (int j = 0; j < centroid_size; j++) {
                r += centroid.points.get(j).r;
                g += centroid.points.get(j).g;
                b += centroid.points.get(j).b;
            }

            centroid.r = r / centroid_size;
            centroid.g = g / centroid_size;
            centroid.b = b / centroid_size;

            allCentroids[i] = centroid;
        }
    }
    
    
    static int get_smallest_distance_index( Centroid all_centroids[ ] , Pixel pxl )
    {
        int[] all_distnce = new int[ all_centroids.length]; 
        for( int i =0; i < all_centroids.length; i++ )
        {
            Centroid cntroid = all_centroids[i];
            Pixel cntroid_Pixel = new Pixel( cntroid.r, cntroid.g, cntroid.b);
           int distance = euclideanDistance(pxl, cntroid_Pixel);
           all_distnce[i] = distance;
        }
        
        int smallest = all_distnce[0];
        int smallestIndex = 0;

        for (int i = 0; i < all_distnce.length; i++)
        {
            if (all_distnce[i] < smallest) 
            {
                smallest = all_distnce[i];
                smallestIndex = i;
            }
        }

        return smallestIndex;
        
    }
    
     static int[][] deserialize_Image(String filePath) {
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (int[][]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
     
     
     static Map<String, int[]> deserialize_codeBook(String filePath) {
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (Map<String, int[]>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void saveImage(BufferedImage image, String filePath, String format) {
        try {
            File file = new File(filePath);
            if (!ImageIO.write(image, format, file)) {
                System.err.println("Unsupported image format: " + format);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    static void deCompress( String image_Path , String codebook_path  , String outputPath, String format )
    {
        int[][] deserialized_img = deserialize_Image(image_Path);
         Map<String, int[]> codeBook  = deserialize_codeBook(codebook_path);
                 
        int height = deserialized_img.length;
        int width = deserialized_img[0].length;
         BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         
          for (int y = 0; y < height; y++) 
            for (int x = 0; x < width; x++) {
                String cmprssedPixel = String.valueOf(deserialized_img[y][x]);
                int rgb[] = codeBook.get(cmprssedPixel);
                int color = new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    
                newImg.setRGB(x, y, color);
            }
saveImage(newImg, outputPath, format);
    }
    
    
    
    static void compress( int k, String img_path ) throws IOException
    {
      
        BufferedImage image = ImageIO.read( new File( img_path ) );
        int width = image.getWidth();       int height = image.getHeight();
        
        Random random = new Random();
        Centroid all_centroids[ ] = new Centroid[k];
        
        // intialize random centoids
        for( int i = 0; i<k; i++)
        {
         int x = random.nextInt(width);
        int y = random.nextInt(height);
        
        int rgb = image.getRGB(x, y);
        Color color = new Color(rgb);
        
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();
            
            Centroid centroid_pixel = new Centroid();
            centroid_pixel.r = red;
            centroid_pixel.g = green;
            centroid_pixel.b = blue;
            
        all_centroids[i] = centroid_pixel;
        
        }
        
        for( int y = 0; y < height; y++ )
            for( int x = 0; x < width; x++ )
            {
                 int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
        
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                
                Pixel pxl = new Pixel(red, green, blue, x , y);
                int indx = get_smallest_distance_index(all_centroids, pxl);
                all_centroids[indx].points.add(pxl);
            }
        
        update_all_centroids(all_centroids);
        
        int[][] array = new int[height][width];

  
        Map<String, int[]> codebook = new HashMap<>();

        for (int i = 0; i < all_centroids.length; i++) {
            Centroid centroid = all_centroids[i];
            int centroidSize = centroid.points.size();

            for (int j = 0; j < centroidSize; j++) {
                int x = centroid.points.get(j).w;
                int y = centroid.points.get(j).h;
                array[y][x] = i; // Assign index value
            }

            codebook.put(String.valueOf(i), new int[]{centroid.r, centroid.g, centroid.b});
        }

    
        serialize(array, "serializedCompressed_data.ser");
        serialize(codebook, "codebook.ser");
        
    }
    
    static void serialize(Object object, String filePath) 
    {
        try (FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) 
        {
            out.writeObject(object);
        } catch (IOException e) {e.printStackTrace();  }
    }
    
    public static void main(String[] args) throws IOException 
    {
        String compressed_img_path = "serializedCompressed_data.ser";
        String codeBook_path = "codebook.ser";
        String format = "jpeg";
        
        compress(999, "example2.jpeg");        
        deCompress(compressed_img_path , codeBook_path , "output.jpeg", format);
    }
    
}

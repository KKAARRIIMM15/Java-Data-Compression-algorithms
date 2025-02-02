
package lzw;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import static lzw.Hierarchical_Clustering.createCompressedImage;
import static lzw.Hierarchical_Clustering.decompressImage;
import static lzw.Hierarchical_Clustering.extractPixels;
import static lzw.Hierarchical_Clustering.hierarchicalClustering;


public class Hierarchical_Clustering 
{
    
     static List<PixelCluster> extractPixels(BufferedImage image) 
     {
        int width = image.getWidth();
        int height = image.getHeight();
        List<PixelCluster> clusters = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                clusters.add(new PixelCluster(x, y, color.getRed(), color.getGreen(), color.getBlue()));
            }
        }
        return clusters;
    }


     static List<PixelCluster> hierarchicalClustering(List<PixelCluster> clusters, int numClusters) 
     {
        while (clusters.size() > numClusters) {
            double minDistance = Double.MAX_VALUE;
            int clusterA = -1, clusterB = -1;

            // Find the closest pair of clusters
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = clusters.get(i).distanceTo(clusters.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        clusterA = i;
                        clusterB = j;
                    }
                }
            }

            // Merge the two closest clusters
            if (clusterA != -1 && clusterB != -1) {
                clusters.get(clusterA).merge(clusters.get(clusterB));
                clusters.remove(clusterB);  // Remove merged cluster
            }
        }
        return clusters;
    }

     static BufferedImage createCompressedImage(List<PixelCluster> clusters, int width, int height)
     {
        BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (PixelCluster cluster : clusters) {
            Color clusterColor = new Color(cluster.red, cluster.green, cluster.blue);
            for (Pixel pixel : cluster.pixels) {
                compressedImage.setRGB(pixel.x, pixel.y, clusterColor.getRGB());
            }
        }
        return compressedImage;
    }

    // Decompress the image (restore from clustered data)
     static BufferedImage decompressImage(List<PixelCluster> clusters, int width, int height)
     {
        BufferedImage decompressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (PixelCluster cluster : clusters) {
            Color clusterColor = new Color(cluster.red, cluster.green, cluster.blue);
            for (Pixel pixel : cluster.pixels) {
                decompressedImage.setRGB(pixel.x, pixel.y, clusterColor.getRGB());
            }
        }
        return decompressedImage;
    }
}


class Pixel {
    int x, y;

    public Pixel(int x, int y) {
        this.x = x;
        this.y = y;
    }
}


class PixelCluster {
    int red, green, blue;
    List<Pixel> pixels;

    public PixelCluster(int x, int y, int red, int green, int blue) {
        this.pixels = new ArrayList<>();
        this.pixels.add(new Pixel(x, y));
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

  
    public double distanceTo(PixelCluster other) {
        return Math.sqrt(Math.pow(this.red - other.red, 2) +
                         Math.pow(this.green - other.green, 2) +
                         Math.pow(this.blue - other.blue, 2));
    }

   
    public void merge(PixelCluster other) {
        int totalPixels = this.pixels.size() + other.pixels.size();
  
        this.red = (this.red * this.pixels.size() + other.red * other.pixels.size()) / totalPixels;
        this.green = (this.green * this.pixels.size() + other.green * other.pixels.size()) / totalPixels;
        this.blue = (this.blue * this.pixels.size() + other.blue * other.pixels.size()) / totalPixels;
        
 
        this.pixels.addAll(other.pixels);
    }

    
    /************************* main  **************************************/
    
   public static void main(String[] args) throws IOException {
        String imagePath = "checker.png";  
        BufferedImage image = ImageIO.read(new File(imagePath));
        int width = image.getWidth();
        int height = image.getHeight();
        
        List<PixelCluster> clusters = extractPixels(image);

        int numClusters = 16; 
        List<PixelCluster> finalClusters = hierarchicalClustering(clusters, numClusters);

        //  Replace pixels with cluster centroids (Compression)
        BufferedImage compressedImage = createCompressedImage(finalClusters, width, height);
        ImageIO.write(compressedImage, "jpg", new File("compressed_image.jpg"));

        // Decompress (Restore from clustered data)
        BufferedImage decompressedImage = decompressImage(finalClusters, width, height);
        ImageIO.write(decompressedImage, "jpg", new File("decompressed_image.jpg"));

        System.out.println("Compression & Decompression Completed.");
    }

 }
    


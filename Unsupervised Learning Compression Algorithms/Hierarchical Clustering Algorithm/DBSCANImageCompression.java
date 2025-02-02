
package lzw;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;

public class DBSCANImageCompression {
    
    

     static List<Pixel> extractPixels(BufferedImage image) 
     {
        int width = image.getWidth();
        int height = image.getHeight();
        List<Pixel> pixels = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                pixels.add(new Pixel(x, y, color.getRed(), color.getGreen(), color.getBlue()));
            }
        }
        return pixels;
    }


     static Map<Pixel, Integer> dbscan(List<Pixel> pixels, double eps, int minPts)
     {
        Map<Pixel, Integer> clusterMap = new HashMap<>();
        int clusterId = 0;

        for (Pixel pixel : pixels) {
            if (clusterMap.containsKey(pixel)) continue; 
            
            List<Pixel> neighbors = getNeighbors(pixel, pixels, eps);
            if (neighbors.size() < minPts) {
                clusterMap.put(pixel, -1);  
            } else {
                clusterId++;
                expandCluster(pixel, neighbors, clusterId, pixels, clusterMap, eps, minPts);
            }
        }
        return clusterMap;
    }

     static void expandCluster(Pixel pixel, List<Pixel> neighbors, int clusterId,
                                      List<Pixel> pixels, Map<Pixel, Integer> clusterMap,
                                      double eps, int minPts) {
        clusterMap.put(pixel, clusterId);

        List<Pixel> queue = new ArrayList<>(neighbors);
        while (!queue.isEmpty()) {
            Pixel current = queue.remove(0);
            if (!clusterMap.containsKey(current)) {
                clusterMap.put(current, clusterId);
                List<Pixel> newNeighbors = getNeighbors(current, pixels, eps);
                if (newNeighbors.size() >= minPts) {
                    queue.addAll(newNeighbors);
                }
            }
        }
    }

  
     static List<Pixel> getNeighbors(Pixel pixel, List<Pixel> pixels, double eps)
     {
        List<Pixel> neighbors = new ArrayList<>();
        for (Pixel other : pixels) {
            if (pixel.distanceTo(other) <= eps) {
                neighbors.add(other);
            }
        }
        return neighbors;
    }

     static BufferedImage createCompressedImage(Map<Pixel, Integer> clusteredPixels, int width, int height)
     {
        BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Map<Integer, Color> clusterColors = new HashMap<>();

        Map<Integer, List<Pixel>> clusterGroups = new HashMap<>();
        for (Map.Entry<Pixel, Integer> entry : clusteredPixels.entrySet()) {
            int clusterId = entry.getValue();
            clusterGroups.putIfAbsent(clusterId, new ArrayList<>());
            clusterGroups.get(clusterId).add(entry.getKey());
        }

        for (Map.Entry<Integer, List<Pixel>> entry : clusterGroups.entrySet()) {
            int clusterId = entry.getKey();
            List<Pixel> pixels = entry.getValue();

            int avgR = 0, avgG = 0, avgB = 0;
            for (Pixel p : pixels) {
                avgR += p.red;
                avgG += p.green;
                avgB += p.blue;
            }
            avgR /= pixels.size();
            avgG /= pixels.size();
            avgB /= pixels.size();

            clusterColors.put(clusterId, new Color(avgR, avgG, avgB));
        }

       
        for (Map.Entry<Pixel, Integer> entry : clusteredPixels.entrySet()) {
            Pixel p = entry.getKey();
            int clusterId = entry.getValue();
            Color clusterColor = clusterColors.getOrDefault(clusterId, Color.BLACK);
            compressedImage.setRGB(p.x, p.y, clusterColor.getRGB());
        }

        return compressedImage;
    }


     static BufferedImage decompressImage(Map<Pixel, Integer> clusteredPixels, int width, int height)
     {
        BufferedImage decompressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (Map.Entry<Pixel, Integer> entry : clusteredPixels.entrySet()) {
            Pixel p = entry.getKey();
            decompressedImage.setRGB(p.x, p.y, new Color(p.red, p.green, p.blue).getRGB());
        }

        return decompressedImage;
    }


    static class Pixel {
        int x, y, red, green, blue;

        public Pixel(int x, int y, int red, int green, int blue) {
            this.x = x;
            this.y = y;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }


        public double distanceTo(Pixel other) {
            return Math.sqrt(Math.pow(this.red - other.red, 2) +
                             Math.pow(this.green - other.green, 2) +
                             Math.pow(this.blue - other.blue, 2));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Pixel pixel = (Pixel) obj;
            return x == pixel.x && y == pixel.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    
    /***************************  main  ******************************/

    public static void main(String[] args) throws IOException {
        String imagePath = "checker.png";  // Input image file
        BufferedImage image = ImageIO.read(new File(imagePath));
        int width = image.getWidth();
        int height = image.getHeight();


        List<Pixel> pixels = extractPixels(image);

        double eps = 30.0;  
        int minPts = 5;     
        Map<Pixel, Integer> clusteredPixels = dbscan(pixels, eps, minPts);


        BufferedImage compressedImage = createCompressedImage(clusteredPixels, width, height);
        ImageIO.write(compressedImage, "jpg", new File("compressed_dbscan.jpg"));


        BufferedImage decompressedImage = decompressImage(clusteredPixels, width, height);
        ImageIO.write(decompressedImage, "jpg", new File("decompressed_dbscan.jpg"));

        System.out.println("Compression & Decompression Completed.");
    }

}


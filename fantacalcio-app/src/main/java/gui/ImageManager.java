package gui;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * encapsulates retrieval and scaling of raster image resources, allowing for
 * optimization of disk accesses and separation of concerns in graphical
 * components.
 * 
 * @implNote the flavor of {@code Singleton} employed here is carefully devised
 *           so as to
 *           <ul>
 *           <li>relieve clients of the need to declare any <b>checked
 *           exceptions</b>
 *           <li>be <i>fully compatible with <b>WindowBuilder</b></i>
 *           </ul>
 */
public class ImageManager {
	
	// 1. dedicated public Enum for image lookup
	
	/**
	 * a public {@code Enum} for the raster images available in the system
	 */
	public static enum ImageKey {
	    PLAYER_FIGURE("/gui_images/player_figure_120x225.png"),
	    RONALDO_HEAD("/gui_images/ronaldo_head_120x225.png"),
		SOCCER_FIELD("/gui_images/raster_field.png");

	    private final String path;

	    ImageKey(String path) {
	        this.path = path;
	    }

	    public String getPath() {
	        return path;
	    }
	}

    private final Map<ImageKey, BufferedImage> imageCache = new HashMap<>();
    
    private void loadImages() throws IOException {
    	for (ImageKey key : ImageKey.values()) {
    		try {
    			BufferedImage image = ImageIO.read(getClass().getResourceAsStream(key.getPath()));
    			imageCache.put(key, image);
    		} catch (IOException e) {
    			// Handle or rethrow a more specific application exception
    			throw new IOException("Failed to load image: " + key.getPath(), e);
    		}
    	}
    }

    // 2. singleton logic that MIRACULOUSLY works with WB
    
    private static ImageManager INSTANCE = null;
    
    private ImageManager() {
    	try {
    		loadImages();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to instantiate ImageManager");
		}
    }

    public static ImageManager getInstance() {
    	return INSTANCE == null ? INSTANCE = new ImageManager() : INSTANCE;
    }
    
    // 3. client image lookup & scaling APIs

	/**
	 * @param key the {@link ImageKey} value representing the image being looked up
	 * @return an {@link ImageIcon} instance for the image being looked up
	 * @implNote uses caching to avoid redundant instantiations
	 */
    public ImageIcon get(ImageKey key) {
        return new ImageIcon(imageCache.get(key));
    }    
    
 	/**
 	 * @param key             the {@link ImageKey} value representing the image
 	 *                        being looked up
 	 * @param availableWindow a {@link Dimension} setting a maximum window for the
 	 *                        image
 	 * @return a {@link Dimension} instance for the size of what would be returned
 	 *         by {@link ImageManager#getScaledToFit(key, vailableWindow)}
 	 */
     public Dimension whenScaledToFit(ImageKey key, Dimension availableWindow) {
     	BufferedImage origImage = imageCache.get(key);
 		int ow = origImage.getWidth(), oh = origImage.getHeight();
 		double scale = Math.min(availableWindow.width / (double) ow, availableWindow.height / (double) oh);
 		int tw = (int) (ow * scale), th = (int) (oh * scale);
 		return new Dimension(tw, th);
     }
    
	/**
	 * 
	 * @param key             the {@link ImageKey} value representing the image
	 *                        being looked up
	 * @param availableWindow a {@link Dimension} setting a maximum window for the
	 *                        image
	 * @return a {@link ImageIcon} instance for the image being looked up, scaled so
	 *         as to fit inside {@code availableWindow} while retaining its original
	 *         aspect ratio
	 */
	public ImageIcon getScaledToFit(ImageKey key, Dimension availableWindow) {
		BufferedImage origImage = imageCache.get(key);
		Dimension whenScaledToFit = whenScaledToFit(key, availableWindow);
		return new ImageIcon(
				origImage.getScaledInstance(whenScaledToFit.width, whenScaledToFit.height, Image.SCALE_SMOOTH));
	}
}

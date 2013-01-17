package cinnamon.image

/**
 * Container which holds the raw byte image data as well as metadata (x,y) and 
 * allowed encoding to base64.
 */
class ImageMeta {
    
    byte[] imageData

    /**
     * Original width before scaling
     */
    Integer width

    /**
     * Original height before scaling
     */
    Integer height
    
    /**
     * Width after scaling
     */
    Integer scaledWidth

    /**
     * Height after scaling
     */
    Integer scaledHeight
    
    /**
     * Encode image to base64 (for use in image data URLs).
     * @return image data encoded as a base64 string
     */
    String imageAsBase64(){
        return imageData.encodeBase64()
    }
}


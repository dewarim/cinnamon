package cinnamon

import cinnamon.global.Constants
import cinnamon.global.ConfThreadLocal
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import cinnamon.utils.ParamParser
import java.awt.Image
import javax.imageio.stream.ImageOutputStream
import java.awt.image.RenderedImage
import java.awt.Graphics
import org.dom4j.Element

class ImageService {

    /**
     * Create or retrieve a base64 encoded thumbnail version of an image,
     * which can be embedded in a HTML page. 
     * If requested, the thumbnail data will be stored in the object's custom metadata
     * in a metaset of type thumbnail.
     * @param osd the OSD which contains the source image.
     * @param repositoryName the repository where the OSD object is stored (used to determine file system path)
     * @param longestSide the image will be scaled to have no side larger than longestSide 
     * @param storeInMetaset if true, store the generated thumbnail in a metaset of type thumbnail.
     * @return a base64 encoded String with the scaled image (or the source image, if longestSize is larger
     * than the actual dimension)
     */
    def fetchThumbnail(ObjectSystemData osd, String repositoryName, Integer longestSide, Boolean storeInMetaset) {
        // load thumbnail metaset
        def metaset = osd.fetchMetaset(Constants.METASET_THUMBNAIL)
        def imageData
        if (!metaset) {
            // create thumbnail
            log.debug("Thumbnail for ${osd.id} does not exist yet.")
            def image = loadImage(repositoryName, osd.contentPath)

            if (image.height <= longestSide && image.width <= longestSide) {
                // image is already small enough: return image
                return imageToBase64(image)
            }
            else {
                def baseImage = imageToBase64(GraphicsUtilities.createThumbnail(image, longestSide))
                if (storeInMetaset) {
                    addToMetaset(osd, baseImage, longestSide)
                }
                return baseImage
            }
        }
        else {
            def xml = ParamParser.parseXmlToDocument(metaset.content)
            def thumbnailNode = xml.selectSingleNode("thumbnail[@longestSide='${longestSide}']")
            if (thumbnailNode == null) {
                def image = loadImage(repositoryName, osd.contentPath)
                def baseImage = imageToBase64(GraphicsUtilities.createThumbnail(image, longestSide))
                if (storeInMetaset) {
                    addToMetaset(osd, baseImage, longestSide)
                }
                return baseImage
            }
            else {
                return thumbnailNode.text
            }
        }
    }

    /**
     * Convert a BufferedImage to a base64 encoded String
     * @param image
     * @return the image in jpg format, encoded as unchunked base64 String. 
     */
    String imageToBase64(BufferedImage image) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ImageIO.write(image, 'jpg', bos)
        return bos.toByteArray().encodeBase64()
    }

    BufferedImage loadImage(String repositoryName, String contentPath) {
        def file = new File(ConfThreadLocal.conf.getDataRoot() + File.separator + repositoryName, contentPath)
        log.debug("looking at image file: ${file.absolutePath}")
        return ImageIO.read(file)
    }

    def addToMetaset(ObjectSystemData osd, String imageData, Integer longestSide) {
        def metaset = osd.fetchMetaset(Constants.METASET_THUMBNAIL)
        def metasetRoot
        def addMetasetToOsd = false
        if (metaset) {
            metasetRoot = ParamParser.parseXmlToDocument(metaset.content)
        }
        else {
            metaset = new Metaset(null, MetasetType.findByName(Constants.METASET_THUMBNAIL))
            metasetRoot = ParamParser.parseXmlToDocument("<metaset />")
            addMetasetToOsd = true
        }
        Element thumbnail = metasetRoot.rootElement.addElement('thumbnail')
        thumbnail.addAttribute('longestSide', longestSide.toString())
        thumbnail.addText(imageData)
        metaset.setContent(metasetRoot.asXML())
        metaset.save()
        if(addMetasetToOsd){
            osd.addMetaset(metaset)
            osd.save()
        }      
    }
}
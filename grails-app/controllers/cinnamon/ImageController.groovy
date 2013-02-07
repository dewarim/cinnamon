package cinnamon

import cinnamon.global.Conf
import cinnamon.global.ConfThreadLocal
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class ImageController extends BaseController {

    def imageService

    /**
     * Load an image OSD from the repository and return its dimensions.
     * This function is used when a specific image's dimension are required, for example:
     * A hundred images from a folder are listed in the folder view. Instead of
     * supplying each object with image specific metadata, this is only loaded when required.
     * (saves going through a hundred images' metadata / image file data).
     * @return
     */
    def fetchImageMeta() {
        // Alternative implementation would be: store image.meta in a metaset.
        // The question is: is it better (speed,memory) to load an image and determine w/h or
        // to fetch the metaset from the database, parse the XML and extract the w/h fields?
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            if (osd.format.contenttype.startsWith('image/')){
            def image = imageService.loadImage(repositoryName, osd.contentPath)
            def result = [imageId: osd.id,
                    width: image.width,
                    height: image.height,
                    size:osd.contentSize
            ]
            render result as JSON
            }
            else{
                render(status: 500, text: message(code: 'error.not.an.image'))
            }
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def imageLoader() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.id)
            if (!osd.format?.contenttype?.startsWith('image/')) {
                return render(status: 503, text: message(code: 'error.wrong.format'))
            }
            response.setContentType(osd.format.contenttype)
            File image = new File(osd.getFullContentPath(repositoryName))
            if (!image.exists()) {
                log.debug("could not find: ${image.absolutePath}")
                return render(status: 503, text: message(code: 'error.image.not.found'))
            }
            if(params.longestSide){
                // do not store thumbnail, as this could lead to malicious people creating 1000 thumbnails and
                // causing a denial of service event.
                def imageData = imageService.fetchScaledImage(osd, repositoryName, Integer.parseInt(params.longestSide))
                response.outputStream << imageData
            }
            else{
                response.outputStream << image.readBytes()
            }

            response.outputStream.close()
            return null
        }
        catch (Exception e) {
            log.debug("imageLoader fail:",e)
            renderException(e)
        }
    }
}

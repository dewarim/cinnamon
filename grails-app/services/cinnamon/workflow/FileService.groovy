package cinnamon.workflow

import cinnamon.config.DataTransferConfig
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient
import org.apache.commons.net.util.TrustManagerUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service class to transfer Files to (and possibly from) remote locations.
 */
class FileService {

    Logger log = LoggerFactory.getLogger(this.class)

    Boolean ftpUpload(File file, DataTransferConfig config) {
        def client = new FTPClient()
        try {
            client.connect(config.serverName, config.port)
            def reply = client.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                throw new RuntimeException('fail.ftp.connection.refused')
            }
            if (!client.login(config.username, config.password)) {
                throw new RuntimeException('fail.ftp.login')
            }
            client.setFileType(FTP.BINARY_FILE_TYPE)
            def fis = new FileInputStream(file)
            def name = config.filename ?: file.name
            if (!client.storeFile(config.remotePath + config.remotePathSeparator + name, fis)) {
                throw new RuntimeException('fail.ftp.store.file')
            }
            return true
        }
        catch (Exception e) {
            log.warn("Failed to upload ftp file", e)
            throw e
        }
        finally {
            if (client?.isConnected()) {
                try {
                    client.disconnect();
                }
                catch (IOException e) {
                    log.warn("Failed to disconnect from server", e)
                }
            }
        }
    }

    File ftpDownload(String name, DataTransferConfig config) {
        def client = new FTPClient()
        try {
            client.connect(config.serverName, config.port)
            def reply = client.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                throw new RuntimeException('fail.ftp.connection.refused')
            }
            if (!client.login(config.username, config.password)) {
                throw new RuntimeException('fail.ftp.login')
            }
            client.setFileType(FTP.BINARY_FILE_TYPE)
            def tempFile = File.createTempFile('cinnamon-ftp-download-', '.tmp')
            def fos = new FileOutputStream(tempFile)
            if (!client.retrieveFile(config.remotePath + config.remotePathSeparator + name, fos)) {
                throw new RuntimeException('fail.ftp.download.file')
            }
            tempFile
        }
        catch (Exception e) {
            log.warn("Failed to upload ftp file", e)
            throw e
        }
        finally {
            if (client?.isConnected()) {
                try {
                    client.disconnect();
                }
                catch (IOException e) {
                    log.warn("Failed to disconnect from server", e)
                }
            }
        }
    }

    Boolean ftpsUpload(File file, DataTransferConfig config) {
        def client = new FTPSClient('SSL')
        try {
            client.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            client.connect(config.serverName, config.port)
            def reply = client.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                throw new RuntimeException('fail.ftp.connection.refused')
            }

            if (!client.login(config.username, config.password)) {
                throw new RuntimeException('fail.ftp.login')
            }
            client.setFileType(FTP.BINARY_FILE_TYPE)
            def fis = new FileInputStream(file)
            client.execPROT('P')
            def name = config.filename ?: file.name
            if (!client.storeFile(config.remotePath + config.remotePathSeparator + name, fis)) {
                throw new RuntimeException('fail.ftp.store.file')
            }
            return true
        }
        catch (Exception e) {
            log.warn("Failed to upload ftp file", e)
            throw e
        }
        finally {
            if (client?.isConnected()) {
                try {
                    client.disconnect();
                }
                catch (IOException e) {
                    log.warn("Failed to disconnect from server", e)
                }
            }
        }
    }

    File ftpsDownload(String name, DataTransferConfig config) {
        def client = new FTPSClient('SSL')
        try {
            client.connect(config.serverName, config.port)
            def reply = client.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                throw new RuntimeException('fail.ftp.connection.refused')
            }
            if (!client.login(config.username, config.password)) {
                throw new RuntimeException('fail.ftp.login')
            }
            client.setFileType(FTP.BINARY_FILE_TYPE)
            client.execPROT('P')
            def tempFile = File.createTempFile('cinnamon-ftp-download-', '.tmp')
            def fos = new FileOutputStream(tempFile)
            if (!client.retrieveFile(config.remotePath + config.remotePathSeparator + name, fos)) {
                throw new RuntimeException('fail.ftp.download.file')
            }
            tempFile
        }
        catch (Exception e) {
            log.warn("Failed to upload ftp file", e)
            throw e
        }
        finally {
            if (client?.isConnected()) {
                try {
                    client.disconnect();
                }
                catch (IOException e) {
                    log.warn("Failed to disconnect from server", e)
                }
            }
        }
    }

}

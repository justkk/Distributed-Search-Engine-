package edu.upenn.cis.cis455.m1.server.implementations;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.util.HttpParsing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/***
 * Its a util class for handling file fetch and look up.
 * It has a static folder path and default look up file.
 */

public class FileRequestHandler {


    static final Logger logger = LogManager.getLogger(FileRequestHandler.class);

    private String staticFolderLocation;
    private final String READ_MODE = "r";
    private final String DEFAULT_FILE_PATH = "index.html";
    private final String MOD_HEADER = "if-modified-since";
    private final String UMOD_HEADER = "if-unmodified-since";
    private final SimpleDateFormat formatter1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    private final SimpleDateFormat formatter2 = new SimpleDateFormat("EEEEE, dd-MMM-yy HH:mm:ss zzz");
    private final SimpleDateFormat formatter3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    private List<SimpleDateFormat> simpleDateFormatList = Arrays.asList(formatter1, formatter2, formatter3);



    public void setStaticFolderLocation(String staticFolderLocation) {
        this.staticFolderLocation = staticFolderLocation;
    }

    public FileRequestHandler(String staticFolderLocation) {
        this.staticFolderLocation = staticFolderLocation;
        modifyFormatters();

    }

    private void modifyFormatters() {
        Date currentDate = new Date();
        LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        localDateTime = localDateTime.minusYears(50);
        Date modified = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        for(SimpleDateFormat simpleDateFormat: simpleDateFormatList) {
            simpleDateFormat.set2DigitYearStart(modified);
        }
    }


    public void handleFetch(Request request, Response response) throws HaltException {


        logger.info("Fetching file");
        String uri = request.uri();
        logger.info("Validating the path");
        String filePath = getFileCompletePath(uri);


        if(request.headers(MOD_HEADER)!= null && "HTTP/1.1".equals(request.protocol())) {
            String value = request.headers(MOD_HEADER);
            try {
                Date d = formatDate(value);
                Date lastModified = getFileLastModifiedTime(filePath);
                if(d.getTime() > lastModified.getTime()) {
                    response.status(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }

            } catch (ParseException e) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }

        if(request.headers(UMOD_HEADER)!=null && "HTTP/1.1".equals(request.protocol())) {
            String value = request.headers(UMOD_HEADER);
            try {
                Date d = formatDate(value);
                Date lastModified = getFileLastModifiedTime(filePath);
                if(d.getTime() < lastModified.getTime()) {
                    throw new HaltException(HttpServletResponse.SC_PRECONDITION_FAILED);
                }

            } catch (ParseException e) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }

        }


        logger.info("Reading bytes from file");
        response.bodyRaw(getFileByteBuffer(uri, filePath));
        logger.info("Fetching the Mime type");
        response.type(HttpParsing.getMimeType(filePath));
        logger.info("Setting response status to 200");
        response.status(HttpServletResponse.SC_OK);

    }

    public void handleLookUp(Request request, Response response) throws HaltException {


        logger.info("Looking up for the file");

        String uri = request.uri();

        logger.info("Validating the path");
        String filePath = getFileCompletePath(uri);
//        response.bodyRaw(getFileByteBuffer(uri, filePath));
//        response.type(HttpParsing.getMimeType(filePath));
        response.status(HttpServletResponse.SC_OK);

    }

    private String getFileCompletePath(String uri) {
        String folderAbsolutePath = Paths.get(staticFolderLocation).toAbsolutePath().toString();
        String fileRelativePath = getFileRelativePath(uri);
        String completePath = null;
        try {
            completePath = Paths.get(folderAbsolutePath, fileRelativePath).toRealPath().toAbsolutePath().toString();
        } catch (IOException e) {
            logger.error("unable to find the file", e);
            throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "The requested URL " + uri + "was not found on this server." + uri);
        }
        if (!checkPrefix(folderAbsolutePath, completePath)) {
            logger.error("path and static folder prefix are different");
            throw new HaltException(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access " + uri);
        }
        File file = new File(completePath);
        if (!file.exists()) {
            logger.error("file doesn't exist");
            throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "The requested URL " + uri + "was not found on this server.");
        } else if (!file.isFile()) {
            logger.error("requested path is folder");
            String defaultFilePath = Paths.get(completePath, DEFAULT_FILE_PATH).toString();
            File indexFile = new File(defaultFilePath);
            if (indexFile.exists() && indexFile.isFile()) {
                return defaultFilePath;
            } else {
                throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "The requested URL " + uri + "was not found on this server.");
            }
        }
        return completePath;
    }

    private byte[] getFileByteBuffer(String uri, String completePath) {

        try {
            logger.info(" reading file");
            RandomAccessFile file = new RandomAccessFile(completePath, READ_MODE);
            FileChannel fileChannel = file.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            byte[] arr = new byte[buffer.remaining()];
            buffer.get(arr);
            fileChannel.close();
            buffer.clear();
            return arr;

        } catch (FileNotFoundException e) {
            logger.error(" File not found exception", e);
            throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "The requested URL " + uri + "was not found on this server.");
        } catch (IOException e) {
            logger.error(e);
            throw new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private String getFileRelativePath(String uri) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uri);
        String fileRelativePath = stringBuilder.toString();
        return fileRelativePath;
    }

    private Date getFileLastModifiedTime(String filePath) {
        File file = new File(filePath);
        Date d = new Date(file.lastModified());
        return d;
    }

    private Date formatDate(String date) throws ParseException {

        for(SimpleDateFormat formatter: simpleDateFormatList) {
            try {
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date d = formatter.parse(date);
                return d;
            } catch (ParseException e) {
                continue;
            }
        }
        throw new ParseException("Date Parse issue", 0);
    }


    private boolean checkPrefix(String folderLocation, String fileLocation) {
        logger.info("Checking the prefix of the path");
        int n1 = folderLocation.length(), n2 = fileLocation.length();
        if (n1 > n2)
            return false;
        for (int i = 0; i < n1; i++)
            if (folderLocation.charAt(i) != fileLocation.charAt(i))
                return false;
        return true;
    }


}

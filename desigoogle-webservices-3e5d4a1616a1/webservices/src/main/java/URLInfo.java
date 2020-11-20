
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class URLInfo implements Serializable {
    private String hostName;
    private int portNo;
    private String filePath;

    private boolean isSecure = false;

    private boolean isValid = false;

    private URL url;

    /**
     * Constructor called with raw URL as input - parses URL to obtain host name and file path
     */
    public URLInfo(String docURL) {
        if (docURL == null || docURL.equals(""))
            return;
        docURL = docURL.trim();

        try {
            url = new URL(docURL);
        } catch (MalformedURLException e) {
            return;
        }

        if (docURL.startsWith("https://")) {
            this.isSecure = true;
            docURL = docURL.replaceFirst("https:", "http:");
        }

        if (!docURL.startsWith("http://") || docURL.length() < 8)
            return;
        // Stripping off 'http://'
        docURL = docURL.substring(7);
		/*If starting with 'www.' , stripping that off too
		if(docURL.startsWith("www."))
			docURL = docURL.substring(4);*/
        int i = 0;
        while (i < docURL.length()) {
            char c = docURL.charAt(i);
            if (c == '/')
                break;
            i++;
        }
        String address = docURL.substring(0, i);
        if (i == docURL.length())
            filePath = "/";
        else
            filePath = docURL.substring(i); //starts with '/'


        if (address.equals("/") || address.equals(""))
            return;
        if (address.indexOf(':') != -1) {
            String[] comp = address.split(":", 2);
            hostName = comp[0].trim();
            try {
                portNo = Integer.parseInt(comp[1].trim());
            } catch (NumberFormatException nfe) {
                if(isSecure) {
                    portNo = 443;
                } else {
                    portNo = 80;
                }
            }
        } else {
            hostName = address;
            if(isSecure) {
                portNo = 443;
            } else {
                portNo = 80;
            }
        }
        isValid = true;
    }

    public URLInfo(String hostName, String filePath) {
        this.hostName = hostName;
        this.filePath = filePath;
        this.portNo = 80;
    }

    public URLInfo(String hostName, int portNo, String filePath) {
        this.hostName = hostName;
        this.portNo = portNo;
        this.filePath = filePath;
    }

    public String getHostName() {
        String[] split = hostName.split("\\?", 2);
        return split[0];
    }

    public void setHostName(String s) {
        hostName = s;
    }

    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int p) {
        portNo = p;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String fp) {
        filePath = fp;
    }

    public boolean isSecure() {
        return this.isSecure;
    }

    public void setSecure(boolean sec) {
        this.isSecure = sec;
    }

    public boolean isValid() {
        return isValid;
    }

    public static void main(String[] args) {
        URLInfo urlInfo = new URLInfo("http://www.google.com/");
        //System.out.println(urlInfo.filePath);
    }


    public boolean equals(Object obj) {
        if(obj instanceof URLInfo) {
            URLInfo instance = (URLInfo) obj;
            if(((this.getHostName() == null && instance.getHostName() == null)
                    || (this.getHostName()!=null && this.getHostName().equals(instance.getHostName())))
                    && this.getPortNo() == instance.getPortNo()
                    &&
                    ((this.getFilePath() == null && instance.getFilePath() == null)
                            || (this.getFilePath()!=null && this.getFilePath().equals(instance.getFilePath())))
                    && this.isSecure == instance.isSecure) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        String protocol = isSecure() ? "https" : "http";
        String output = protocol + "://" + this.hostName + ":" + this.portNo + "/"+ this.filePath;
        return output.hashCode();
    }

    public URL getUrl() {
        return url;
    }
}

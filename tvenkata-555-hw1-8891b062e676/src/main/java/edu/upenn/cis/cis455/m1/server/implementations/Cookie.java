package edu.upenn.cis.cis455.m1.server.implementations;

import edu.upenn.cis.cis455.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Cookie {

    private String name;
    private String value;
    private String path;
    private String domain;
    private int version = 0;
    private boolean secure = false;
    private String comment;
    private boolean httpOnly = false;
    private int maxAge = Constants.getInstance().getCOOKIE_AGE();
    private boolean isValid = true;
    private static SimpleDateFormat formatter1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    static {
        formatter1.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Cookie() {
    }


    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(String name, String value, int maxAge) {
        this.name = name;
        this.value = value;
        this.maxAge = maxAge;
    }

    public Cookie(String name, String value, boolean secure, int maxAge) {
        this.name = name;
        this.value = value;
        this.secure = secure;
        this.maxAge = maxAge;
    }

    public Cookie(String name, String value, boolean secure, boolean httpOnly, int maxAge) {
        this.name = name;
        this.value = value;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.maxAge = maxAge;
    }


    public Cookie(String name, String value, String path) {
        this.name = name;
        this.value = value;
        this.path = path;
    }

    public Cookie(String name, String value, String path, int maxAge) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.maxAge = maxAge;
    }

    public Cookie(String name, String value, String path, boolean secure, int maxAge) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.secure = secure;
        this.maxAge = maxAge;
    }

    public Cookie(String name, String value, String path, boolean secure, boolean httpOnly, int maxAge) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.maxAge = maxAge;
    }

    public Cookie clone() {
        Cookie c = new Cookie();
        c.name = this.name;
        c.value = this.value;
        c.path = this.path;
        c.domain = this.domain;
        c.version = this.version;
        c.secure = this.secure;
        c.comment = this.comment;
        c.maxAge = this.maxAge;
        c.httpOnly = this.httpOnly;
        return c;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void inValidate() {
        this.isValid = false;
    }

    public String getSetCookieString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Set-Cookie");
        stringBuilder.append(":");
        stringBuilder.append(name);
        stringBuilder.append("=");
        stringBuilder.append(value);

        if (path != null) {
            stringBuilder.append(";");
            stringBuilder.append("path");
            stringBuilder.append("=");
            stringBuilder.append(path);
        }

        if(!isValid) {
            maxAge = 0;
        }

        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime() + maxAge*1000);

        stringBuilder.append(";");
        stringBuilder.append("Expires");
        stringBuilder.append("=");
        stringBuilder.append(formatter1.format(expiryDate));

        stringBuilder.append(";");
        stringBuilder.append("expires");
        stringBuilder.append("=");
        stringBuilder.append(formatter1.format(expiryDate));



        stringBuilder.append(";");
        stringBuilder.append("Max-Age");
        stringBuilder.append("=");
        stringBuilder.append(String.valueOf(maxAge));

        stringBuilder.append(";");
        stringBuilder.append("maxAge");
        stringBuilder.append("=");
        stringBuilder.append(String.valueOf(maxAge));

        stringBuilder.append(";");
        stringBuilder.append("SameSite");
        stringBuilder.append("=");
        stringBuilder.append(String.valueOf("Strict"));


        if (httpOnly) {
            stringBuilder.append(";");
            stringBuilder.append("HttpOnly");
        }

        if (secure) {
            stringBuilder.append(";");
            stringBuilder.append("Secure");
        }

        if (domain != null) {
            stringBuilder.append(";");
            stringBuilder.append("Domain");
            stringBuilder.append("=");
            stringBuilder.append(domain);
        }

        if (comment != null) {
            stringBuilder.append(";");
            stringBuilder.append("Comment");
            stringBuilder.append("=");
            stringBuilder.append(comment);
        }

        return stringBuilder.toString();
    }
}

package br.bireme.scl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heitor Barbieri
 * date 20130715
 */
public class CheckUrl {
    private static final String HTTP_REG_EXP = "HTTP/\\d\\.\\d\\s+(\\d+)";    
    
    private static final int CONNECT_TIMEOUT = 5000; //60000; // connect timeout (miliseconds)
    private static final int SO_TIMEOUT = 5000; //60000; // read timeout (miliseconds)
    private static final int SO_LINGER = 10; // close timeout (seconds)
    
    public static final int UNKNOWN = 1000;
    public static final int IO_EXCEPTION = 1001;
    
    private static final Pattern pat = Pattern.compile(HTTP_REG_EXP);
    
    public static int check(final String urlStr) {
        if (urlStr == null) {
            throw new NullPointerException("urlStr");
        }
        return check(urlStr, 0);
    }
    
    public static int check(final String urlStr,
                            final int times) {
        if (urlStr == null) {
            throw new NullPointerException("urlStr");
        }
        if (times < 0) {
            throw new IllegalArgumentException("times[" + times + "] < 0");
        }
        if (times > 2) {            
            return 301;  // MOVED_PERMANENTLY
        }
        
        final URL url;
        final int port;
        
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader inReader = null;
        int respCode;

        try {
            url = new URL(urlStr);
            final String host = url.getHost();
            final String path = url.getPath();
            final String query = url.getQuery();
            final String mess = "HEAD " + (path.isEmpty() ? "/" : path)
              + ((query == null) ? "" : ("?" + query))
              + " HTTP/1.0\r\n"
              + "User-Agent: CERN-LineMode/2.15 libwww/2.17b3\r\n"
              + "Host: " + host + "\r\n"
              + "Connection: close\r\n\r\n";
                                    
//System.out.println("mess=[" + mess + "]\n");
            port = url.getPort() == -1 ? 80 : url.getPort();
            socket = new Socket();
            socket.setKeepAlive(false);
            socket.setSoTimeout(SO_TIMEOUT);            
            //socket.setSoLinger(true, SO_LINGER);
            //socket. setReuseAddress(true);
                        
            final InetSocketAddress isa = new InetSocketAddress(host, port);
            socket.connect(isa, CONNECT_TIMEOUT);
            out = new PrintWriter(socket.getOutputStream(), true);
            inReader = new BufferedReader(new InputStreamReader(
                                                    socket.getInputStream()));
            out.println(mess);
            final String line = inReader.readLine();
            if (line == null) {
                respCode = IO_EXCEPTION;
            } else {
                respCode = getRespCode(line);
                if ((respCode == 301) || (respCode == 302) 
                                      || (respCode == 307)) {
                    respCode = movedUrl(urlStr, inReader, respCode, times);
                }
                /*else if (respCode > 0) {
                    System.out.println(line);
                    while(true) {                        
                        final String line2 = inReader.readLine();
                        if (line2 == null) {
                            break;
                        }
                        System.out.println(line2);
                    }
                }*/
            }
        } catch (Exception ex) {
//ex.printStackTrace();            
            respCode = IO_EXCEPTION;
        } finally {
            try {
                if (inReader != null) {
                    inReader.close();
                }
                if (out != null) {
                    out.close();
                }
                if ((socket != null) && (!socket.isClosed())) {
                    socket.close();
                }
            } catch (IOException ioe) {
ioe.printStackTrace();
                respCode = IO_EXCEPTION;
            }
        }
//if (times >= 0) {
    System.out.println("times=" + times + " url=[" + urlStr + "] retCode=" + respCode + "  isBroken=" + isBroken(respCode));
//}
        return respCode;
    }
    
    public static boolean isBroken(final int code) {
        if (code < 0) {
            throw new IllegalArgumentException("code[" + code + "] < 0");
        }
        boolean ret = true;
        
        if ((code == 200) || (code == 401) || 
            (code == 402) || (code == 407)) {
            ret = false;
        }
                
        return ret;
    }
    
    private static int getRespCode(final String response) {
        assert response != null;

        final Matcher mat = pat.matcher(response.trim());

        return mat.find() ? Integer.parseInt(mat.group(1)) : UNKNOWN;
    }
    
    private static int movedUrl(final String urlStr,
                                final BufferedReader inReader,
                                final int checkCode,
                                final int times) throws IOException { 
        assert urlStr != null;
        assert inReader != null;
        assert checkCode >= 300;
        assert times >= 0;
                
        int ret = IO_EXCEPTION;
        final URL url = new URL(urlStr);
        final String oldHost = url.getHost();
        final String oldProtocol = url.getProtocol();
        
        while (true) {
            String line = inReader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.startsWith("Location:")) {
                String newUrl = line.substring(9).trim();
                if (!shouldFollow(urlStr, newUrl)) {
                    ret = checkCode;
                    break;
                } else if (newUrl.startsWith("www")) {
                    // do nothing
                } else if (newUrl.startsWith("http")) {
                    if (newUrl.contains("://localhost")) {
                        final int idx = newUrl.indexOf('/', 15);
                        final int port = url.getPort();
                        final StringBuilder builder = new StringBuilder();
                        
                        builder.append(url.getProtocol());
                        builder.append("://");
                        builder.append(oldHost);
                        if (port != -1) {
                            builder.append(":");
                            builder.append(port);
                        }                        
                        builder.append(newUrl.substring(idx));
                        newUrl = builder.toString();
                    }                 
                } else if (newUrl.charAt(0) == '/') {                    
                    newUrl = oldProtocol + "://" + oldHost + newUrl;
                } else if (newUrl.startsWith("./")) {
                    newUrl = oldProtocol + "://" +oldHost + newUrl.substring(1);
                } else {
                    newUrl = oldProtocol + "://" + oldHost + "/" + newUrl;
                }
                ret = check(newUrl, times + 1); 
                break;
            }            
        }
        
        return ret;
    }
    
    private static boolean shouldFollow (final String oldUrl,
                                         final String newUrl) {
        assert oldUrl != null;
        assert newUrl != null;
        
        final String old1 = oldUrl.startsWith("http://") ? oldUrl.substring(7) : oldUrl;
        final String old2 = old1.endsWith("/") ? old1.substring(0, old1.length() - 1) : old1;
        final String new1 = newUrl.startsWith("http://") ? newUrl.substring(7) : newUrl;
        final String new2 = new1.endsWith("/") ? new1.substring(0, new1.length() - 1) : new1;
                               
        final String[] splitOld = old2.split("/");
        final String[] splitNew = new2.split("/");
        boolean ret;
        
        if (splitOld.length == 1) {
            ret = true;
        } else if (splitNew.length == 1) {
            ret = false;
        } else if (splitOld[splitOld.length - 1].equals(splitNew[splitNew.length - 1])) {
            ret = true;
        } else {
            ret = false;
        }
        
        return ret;
    }
    
    private static void usage() {
        System.err.println("usage: CheckUrl <url>");
        System.exit(-1);
    }
    
    public static void main(final String[] args) {
        if (args.length != 1) {
             usage();
        }
        
        System.out.println("URL=[" + args[0] + "]\n");
        
        System.out.println(CheckUrl.check(args[0]));
    }
}

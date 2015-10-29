/*=========================================================================

    Copyright © 2013 BIREME/PAHO/WHO

    This file is part of Social Check Links.

    Social Check Links is free software: you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 2.1 of
    the License, or (at your option) any later version.

    Social Check Links is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Social Check Links. If not, see
    <http://www.gnu.org/licenses/>.

=========================================================================*/

package br.bireme.scl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Heitor Barbieri
 * date 20130715
 */
public class CheckUrl {
    private static final int CONNECT_TIMEOUT = 5000; //60000; // connect timeout (miliseconds)
    private static final int SO_TIMEOUT = 20000; //5000; //60000; // read timeout (miliseconds)
    private static final int MAX_REDIRECTS = 3;

    public static final int CONTINUE = 100;
    public static final int SWITCHING_PROTOCOLS = 101;
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int NO_CONTENT = 204;
    public static final int RESET_CONTENT = 205;
    public static final int PARTIAL_CONTENT = 206;
    public static final int MULTIPLE_CHOICE = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int USE_PROXY = 305;
    public static final int UNUSED = 306;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;
    public static final int INTERNAL_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    public static final int CONNECTION_REFUSED = 1000;
    public static final int CONNECTION_TIMED_OUT = 1001;
    public static final int UNKNOWN_HOST_EXCEPTION = 1002;
    public static final int MALFORMED_URL = 1003;
    public static final int SSL_EXCEPTION = 1004;
    public static final int NO_ROUTE_TO_HOST_EXCEPTION = 1005;
    public static final int SOCKET_EXCEPTION = 1006;
    public static final int FILE_NOT_FOUND_EXCEPTION = 1007;
    public static final int IO_EXCEPTION = 1008;
    public static final int CONNECTION_RESET_BY_PEER = 1009;
    public static final int ILLEGAL_URL = 1010;
    public static final int BIND_EXCEPTION = 1011;
    public static final int PORT_UNREACHABLE_EXCEPTION = 1012;
    public static final int HTTP_EXCEPTION = 1013;
    public static final int UNKNOWN = 1100;
    
    private static final RequestConfig CONFIG = RequestConfig
                                             .custom()
                                             .setCircularRedirectsAllowed(false)
                                             .setConnectTimeout(CONNECT_TIMEOUT)
                                             .setMaxRedirects(MAX_REDIRECTS)
                                             .setSocketTimeout(SO_TIMEOUT)
                                             .build();
    
    public static int check(final String url) throws IOException {
        if (url == null) {
            throw new NullPointerException();
        }
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        int responseCode = -1;
                
        try {            
            final HttpHead httphead = new HttpHead(fixUrl(url));
            httphead.setConfig(CONFIG);

            // Create a custom response handler
            final ResponseHandler<Integer> responseHandler = 
                                               new ResponseHandler<Integer>() {

                @Override
                public Integer handleResponse(
                        final HttpResponse response) 
                                   throws ClientProtocolException, IOException {
                    return response.getStatusLine().getStatusCode();                    
                }

            };
            responseCode = httpclient.execute(httphead, responseHandler);
        } catch (Exception ex) {
            responseCode = getExceptionCode(ex);
        } finally {
            httpclient.close();
        }
        return responseCode;
    }
    
    private static int getExceptionCode(final Exception exc) {
        assert exc != null;
        
        final int code;

        String emess = exc.getMessage();
        emess = (emess == null) ? "" : emess;

        if (exc instanceof ConnectException) {
            if (emess.endsWith("refused: connect")) {
                code = CONNECTION_REFUSED;
            } else {
                code = CONNECTION_TIMED_OUT;
            }
        } else if (exc instanceof UnknownHostException) {
            code = UNKNOWN_HOST_EXCEPTION;
        } else if (exc instanceof MalformedURLException) {
            code = MALFORMED_URL;
        } else if (exc instanceof SSLException) {
            code = SSL_EXCEPTION;
        } else if (exc instanceof NoRouteToHostException) {
            code = NO_ROUTE_TO_HOST_EXCEPTION;
        } else if (exc instanceof SocketTimeoutException) {
            code = CONNECTION_TIMED_OUT;
        } else if (exc instanceof BindException) {
/*
u have not answered whther u use windows xp...
Windows XP only will make outbound TCP/IP connections using ports 1024-5000,
and takes up to 4 minutes to recycle them.
Therefore, if you do a lot of connections inReader a short amount of time, you
can easily eat that port range up .
The range can be adjusted via a registry setting
http://support.microsoft.com/default.aspx?scid=kb;en-us;196271
             */
            code = BIND_EXCEPTION;
        } else if (exc instanceof PortUnreachableException) {
            code = PORT_UNREACHABLE_EXCEPTION;
        } else if (exc instanceof FileNotFoundException) {
            code = FILE_NOT_FOUND_EXCEPTION;
        } else if (exc instanceof RuntimeException) {
            code = ILLEGAL_URL;
        } else if (exc instanceof SocketException) {            
            if (emess.contains("Connection reset")) {
                code = CONNECTION_RESET_BY_PEER;
            } else if (emess.contains("Connection timed out")) {
                code = CONNECTION_TIMED_OUT;
            } else if (emess.contains("Connection refused")) {
                code = CONNECTION_REFUSED;
            } else if (emess.contains("No route to host")) {
                code = NO_ROUTE_TO_HOST_EXCEPTION;
            } else if (emess.contains("Too many open files")) {
                code = IO_EXCEPTION;
            } else {
                code = SOCKET_EXCEPTION;
            }
        } else if (exc instanceof HttpException) {
            code = HTTP_EXCEPTION;
        } else if (exc instanceof IOException) {
            if (emess.contains("Connection timed out")) {
                code = CONNECTION_TIMED_OUT;
            } else if (emess.contains("Connection refused")) {
                code = CONNECTION_REFUSED;
            } else if (emess.contains("No route to host")) {
                code = NO_ROUTE_TO_HOST_EXCEPTION;
            } else {
                code = IO_EXCEPTION;
            }
        } else {
            code = UNKNOWN;
        }
        return code;
    }
    
    private static String fixUrl(final String url) {
        assert url != null;
        
        final String ret = url.trim().replaceAll(" ", "%20");
        
        return ret;
    }
    
    public static boolean isBroken(final int code) {
        if (code < 0) {
            throw new IllegalArgumentException("code[" + code + "] < 0");
        }
        final boolean ret;

        if ((code == 200) || (code == 401) ||
            (code == 402) || (code == 407)) {
            ret = false;
        } else {
            ret = true;
        }

        return ret;
    }

    private static void usage() {
        System.err.println("usage: CheckUrl <url>");
        System.exit(-1);
    }

    public static void main(final String[] args) throws IOException {
        //final String url =  "http://citrus.uspnet.usp.br/eef/uploads/arquivo/v17 n1 artigo1.pdf";     // 404 error code
        //final String url = "http://citrus.uspnet.usp.br/eef/uploads/arquivo/v17%20n1%20artigo1.pdf"; // 200 error code 
        //final String url = "http://www.scielo.org.co/scielo.php?script=sci_arttext&pid=S0120-548X2006000200005&lng=pt&nrm=iso&tlng=es";
        //final String url = "http://www.scielo.org.co/scielo.php?script=sci_arttext&pid=S0120-548X2006000200001&lng=en&nrm=iso&tlng=es";
        //final String url = " http://www2.alasbimnjournal.cl/alasbimn/CDA/sec_a/0,1205,SCID=686&PRT=0,00.html";
        //final String url = "http://www2.alasbimnjournal.cl/alasbimn/CDA/sec_c/0,1222,SCID=686&PRT=693,00.html";
        //final String url = "http://www.profamilia.org.co/004_servicios/medios/Feminicidio.pdf?categoria_id=2&PHPSESSID=2e8e18e7b29cae6e0e93b6c7bf9f9e86";
        //final String url = "http://www.profamilia.com/images/stories/afiches/libros/libros/feminicidio.pdf";
        final String url = "http://publicaciones.ops.org.ar/publicaciones/otras pub/Drogas.pdf";
                
        /*if (args.length != 1) {
             usage();
        }*/

        System.out.println();
        System.out.println("URL=[" + url + "] ");
        System.out.println("fURL=[" + fixUrl(url) + "] ");
        System.out.println("ErrCode=" + CheckUrl.check(url));
        //System.out.print("URL=[" + args[0] + "] ");
        //System.out.println("fURL=[" + fixUrl(args[0]) + "] ");
        //System.out.println("ErrCode=" + CheckUrl.check(args[0]));
    }
}

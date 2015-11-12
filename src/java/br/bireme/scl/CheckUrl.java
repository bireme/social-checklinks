/*=========================================================================

    Copyright © 2013 BIREME/PAHO/WHO

    This file is part of Check Links.

    Check Links is free software: you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 2.1 of
    the License, or (at your option) any later version.

    Check Links is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Check Links. If not, see
    <http://www.gnu.org/licenses/>.

=========================================================================*/

package br.bireme.scl;

import java.io.IOException;
import java.net.*;
import javax.net.ssl.SSLProtocolException;
import org.apache.http.*;
import org.apache.http.conn.*;
import org.apache.http.client.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.execchain.RequestAbortedException;

import org.apache.http.message.BasicHeader;

/**
 *
 * @author Heitor Barbieri
 * date 20130715
 */
public class CheckUrl {
    private static final int CONNECT_TIMEOUT = 180000; // connect timeout (miliseconds)
    private static final int SO_TIMEOUT = 180000; //5000; //60000; // read timeout (miliseconds)
    private static final int MAX_REDIRECTS = 5;

    public static final int CLIENT_PROTOCOL_EXCEPTION = 1000;
    public static final int CONNECTION_POOL_TIMEOUT_EXCEPTION = 1001;
    public static final int CONNECTION_CLOSED_EXCEPTION = 1002;
    public static final int CONNECT_TIMEOUT_EXCEPTION = 1003;
    public static final int HTTP_HOST_CONNECT_EXCEPTION = 1004;
    public static final int REQUEST_ABORTED_EXCEPTION = 1005;
    public static final int UNSUPPORTED_SCHEME_EXCEPTION = 1006;
    public static final int ILLEGAL_CHARACTER_EXCEPTION = 1007;
    public static final int UNKNOWN_HOST_EXCEPTION = 1008;
    public static final int SOCKET_EXCEPTION = 1009;
    public static final int SOCKET_TIMEOUT_EXCEPTION = 1010;
    public static final int TRUNCATED_CHUNK_EXCEPTION = 1011;
    public static final int SSL_PROTOCOL_EXCEPTION = 1012;

    public static final int UNKNOWN = 1100;

    private static final RequestConfig CONFIG = RequestConfig
                                           .custom()
                                           .setCircularRedirectsAllowed(true)
                                           .setConnectTimeout(CONNECT_TIMEOUT)
                                           .setMaxRedirects(MAX_REDIRECTS)
                                           .setSocketTimeout(SO_TIMEOUT)
                                           .build();

    public static int check(final String url) {
        if (url == null) {
            throw new NullPointerException();
        }

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        int responseCode = -1;

        try {
            //final HttpHead httpX = new HttpHead(fixUrl(url)); // Some servers return 500
            final HttpGet httpX = new HttpGet(fixUrl(url));
            httpX.setConfig(CONFIG);
            httpX.setHeader(new BasicHeader("User-Agent", "Wget/1.16.1 (linux-gnu)"));
            httpX.setHeader(new BasicHeader("Accept", "*/*"));
            httpX.setHeader(new BasicHeader("Accept-Encoding", "identity"));
            httpX.setHeader(new BasicHeader("Connection", "Keep-Alive"));

            // Create a custom response handler
            final ResponseHandler<Integer> responseHandler =
                                               new ResponseHandler<Integer>() {

                @Override
                public Integer handleResponse(final HttpResponse response)
                                  throws ClientProtocolException, IOException {
                    return response.getStatusLine().getStatusCode();
                }
            };
            responseCode = httpclient.execute(httpX, responseHandler);
        } catch (Exception ex) {
            responseCode = getExceptionCode(ex);
        } finally {
        	try {
        		httpclient.close();
        	} catch (Exception ioe) {
        	}
        }
        return responseCode;
    }

    private static int getExceptionCode(final Exception ex) {
        assert ex != null;

        final int code;

        if (ex instanceof ClientProtocolException) {
        	code = CLIENT_PROTOCOL_EXCEPTION;
        } else if (ex instanceof ConnectionPoolTimeoutException) {
        	code = CONNECTION_POOL_TIMEOUT_EXCEPTION;
        } else if (ex instanceof ConnectionClosedException) {
          code = CONNECTION_CLOSED_EXCEPTION;
        } else if (ex instanceof ConnectTimeoutException) {
        	code = CONNECT_TIMEOUT_EXCEPTION;
        } else if (ex instanceof HttpHostConnectException) {
        	code = HTTP_HOST_CONNECT_EXCEPTION;
        } else if (ex instanceof HttpResponseException) {
        	code = ((HttpResponseException)ex).getStatusCode();
        } else if (ex instanceof RequestAbortedException) {
        	code = REQUEST_ABORTED_EXCEPTION;
        } else if (ex instanceof UnsupportedSchemeException) {
        	code = UNSUPPORTED_SCHEME_EXCEPTION;
        } else if (ex instanceof IllegalArgumentException) {
          final String msg = ex.getMessage();
          final String lmsg = (msg == null) ? "" : msg.toLowerCase();
          if (lmsg.contains("illegal character")) {
            code = ILLEGAL_CHARACTER_EXCEPTION;
          } else {
            System.out.println("unknown -> class:" + ex.getClass().getName() +
                                          " msg: " + lmsg);
            code = UNKNOWN;
          }
        } else if (ex instanceof UnknownHostException) {
          code = UNKNOWN_HOST_EXCEPTION;
        } else if (ex instanceof SocketException) {
          code = SOCKET_EXCEPTION;
        } else if (ex instanceof SocketTimeoutException) {
          code = SOCKET_TIMEOUT_EXCEPTION;
        } else if (ex instanceof TruncatedChunkException) {
          code = TRUNCATED_CHUNK_EXCEPTION;
        } else if (ex instanceof SSLProtocolException) {
          code = SSL_PROTOCOL_EXCEPTION;
        } else {
          final String msg = ex.getMessage();
          final String lmsg = (msg == null) ? "" : msg.toLowerCase();
          System.out.println("unknown2 -> class:" + ex.getClass().getName() +
                                         " msg: " + lmsg);
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
        //final String url = "http://publicaciones.ops.org.ar/publicaciones/otras pub/Drogas.pdf";

        if (args.length != 1) {
             usage();
        }
        final String url = args[0];

        System.out.println();
        System.out.println("URL=[" + url + "] ");
        //System.out.println("fURL=[" + fixUrl(url) + "] ");
        System.out.println("ErrCode=" + CheckUrl.check(url));
    }
}

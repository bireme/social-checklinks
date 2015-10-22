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

import java.io.IOException;
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
        } finally {
            httpclient.close();
        }
        return responseCode;
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
                
        if (args.length != 1) {
             usage();
        }

        System.out.println();
        //System.out.println("URL=[" + url + "] ");
        //System.out.println("fURL=[" + fixUrl(url) + "] ");
        //System.out.println("ErrCode=" + CheckUrl.check(url));
        System.out.print("URL=[" + args[0] + "] ");
        System.out.println("fURL=[" + fixUrl(args[0]) + "] ");
        System.out.println("ErrCode=" + CheckUrl.check(args[0]));
    }
}

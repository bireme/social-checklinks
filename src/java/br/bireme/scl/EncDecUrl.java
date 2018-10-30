/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 * @author Heitor Barbieri
 * date: 20150527
 */
public class EncDecUrl {
    public static String decodeUrl(final String in) throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        final String deco_UTF_8;
        String ret;

        try {
            deco_UTF_8 = URLDecoder.decode(in, "UTF-8");
            if (deco_UTF_8.contains("\uFFFD")) { // '\uFFFD', the Unicode replacement character - <?>
                final String deco_ISO8859_1 =
                            URLDecoder.decode(Tools.badUrlFix(in), "ISO8859-1");

                if (deco_ISO8859_1.contains("\uFFFD")) {
                    throw new IOException("Invalid decoding string [" + in + "]");
                } else {
                    ret = deco_ISO8859_1;
                }
            } else {
                ret = deco_UTF_8;
            }
        } catch (Exception ex) {
            throw new IOException("Invalid decoding string [" + in + "]");
        }

        return ret;
    }

    public static String encodeUrl(final String in,
                                   final String charset,
                                   final boolean all) throws IOException {

        if (in == null) {
            throw new NullPointerException("in");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        final String in2 = in.trim();
        final String ret;

        try {
            if (all) {
                ret = URLEncoder.encode(decodeUrl(in2), charset);
            } else {
                /*
                final URL url = new URL(decodeUrl(in2));
                String info = url.getUserInfo();
                info = (info == null) ? null : URLEncoder.encode(info, charset);
                String host = url.getHost();
                host = (host == null) ? null : URLEncoder.encode(host, charset);
                String path = url.getPath();
                path = (path == null) ? null : URLEncoder.encode(path, charset);
                String query = url.getQuery();
                query = (query == null) ? null : URLEncoder.encode(query, charset);
                String ref = url.getRef();
                ref = (ref == null) ? null : URLEncoder.encode(ref, charset);

                final URI uri = new URI(url.getProtocol(), info, host,
                                        url.getPort(), path, query, ref);
                ret = uri.toString();
                */
                ret = in.replaceAll(" ", "%20");
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return ret;
    }
}

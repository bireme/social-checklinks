/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heitor Barbieri
 * date: 20130729
 */
public class Tools {
    static String[] commonPreSuffix(final String brokenUrl,
                                    final String fixedUrl) {
        assert brokenUrl != null;
        assert fixedUrl != null;

        final int blen = brokenUrl.length();
        final int flen = fixedUrl.length();
        final int min = Math.min(blen, flen);
        final String prefix;
        final String suffix;
        int idx = 0;
        int idx2 = flen - 1;

        for (; idx < min; idx++) {
            if (brokenUrl.charAt(idx) != fixedUrl.charAt(idx)) {
                break;
            }
        }
        prefix = brokenUrl.substring(0, idx);
        idx = blen - 1;

        for (; idx >= 0; idx--) {
            if (brokenUrl.charAt(idx) != fixedUrl.charAt(idx2)) {
                idx++;
                break;
            }
            idx2--;
            if (idx2 < 0) {
                break;
            }
        }
        idx = (idx < 0) ? 0 : idx;
        suffix = brokenUrl.substring(idx);

        return new String[] { prefix, suffix };
    }

    static String escapeChars(final String inStr) {
        assert inStr != null;

        final String out = inStr.replaceAll(
                    "([\\^\\$\\\\?\\+\\*\\{\\}\\(\\)\\[\\]\\|\\&\\-\\%])",
                                                                      "\\\\$1");
        return out;
    }

    /**
     * Gera padroes para substituicao de strings nas urls
     * @param brokenUrl ulr quebrada
     * @param fixedUrl  url boa
     * @return retorna o padrao a ser procurado nas urls e o padrao a ser
     *         substituido.
     */
    static String[] getPatterns(final String brokenUrl,
                                final String fixedUrl) {
        assert brokenUrl != null;
        assert fixedUrl != null;

        final String[] presuf = commonPreSuffix(brokenUrl, fixedUrl);
        final String prefix = presuf[0];
        final String suffix = presuf[1];
        final String from;
        final String to;

        if (suffix.isEmpty()) {
            if (prefix.isEmpty()) {
                from = "^" + escapeChars(brokenUrl) + "$";
                to = escapeChars(fixedUrl);
            } else {
                final int len = prefix.length();
                final int pos1 = brokenUrl.indexOf(prefix) + len;
                final int pos2 = fixedUrl.indexOf(prefix) + len;
                final String brok = brokenUrl.substring(pos1);

                if (brok.trim().isEmpty()) {
                    from = "^" + escapeChars(brokenUrl) + "$";
                    to = escapeChars(fixedUrl);
                } else {
                    from = escapeChars(brok) + "$";
                    to = escapeChars(fixedUrl.substring(pos2));
                }
            }
        } else {
            final int pos1 = brokenUrl.lastIndexOf(suffix);
            final int pos2 = fixedUrl.lastIndexOf(suffix);
            from = "^" + escapeChars(brokenUrl.substring(0, pos1));
            to = escapeChars(fixedUrl.substring(0, pos2));
        }

        return new String[] { from, to };
    }

    static Set<IdUrl> getConvertedUrls(final Set<IdUrl> inSet,
                                       final String oldPattern,
                                       final String newPattern) {
        assert inSet != null;
        assert oldPattern != null;
        assert newPattern != null;

        final Set<IdUrl> outSet = new HashSet<IdUrl>();
        for (IdUrl iu : inSet) {
            final String newUrl = iu.url.replaceFirst(oldPattern, newPattern);
            outSet.add(new IdUrl(iu.id, newUrl, iu.ccs, iu.since, iu.mst));
        }
        return outSet;
    }

    public static Set<IdUrl> convertUrls(final Set<IdUrl> inSet,
                                         final String brokenUrl,
                                         final String fixedUrl) {
        if (inSet == null) {
            throw new NullPointerException("inSet");
        }
        if (brokenUrl == null) {
            throw new NullPointerException("brokenUrl");
        }
        if (fixedUrl == null) {
            throw new NullPointerException("fixedUrl");
        }
        final String[] patterns = getPatterns(brokenUrl, fixedUrl);
        final Set<IdUrl> outSet = getConvertedUrls(inSet, patterns[0],
                                                                   patterns[1]);

        return outSet;
    }

    public static ResourceBundle getMessages(final String lang) {
        final Locale currentLocale;

        if (lang == null) {
            currentLocale = new Locale("en", "US");
        } else if (lang.equals("pt")) {
            currentLocale = new Locale("pt", "BR");
        } else if (lang.equals("es")) {
            currentLocale = new Locale("es", "ES");
        } else if (lang.equals("fr")) {
            currentLocale = new Locale("fr", "FR");
        } else  {
            currentLocale = new Locale("en", "US");
        }

        return ResourceBundle.getBundle("i18n.MessagesBundle", currentLocale);
    }

    public static Set<String> getTitles(final String surl) throws IOException {
        if (surl == null) {
            throw new NullPointerException("surl");
        }
        final URL url = new URL(surl);
        final HttpURLConnection connection =
                                        (HttpURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.connect();

        final StringBuilder builder = new StringBuilder();
        final int respCode = connection.getResponseCode();
        final boolean respCodeOk = (respCode == 200);
        final BufferedReader reader;

        if (respCodeOk) {
            reader = new BufferedReader(new InputStreamReader(
                                                  connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(
                                                  connection.getErrorStream()));
        }
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            builder.append(line);
            builder.append("\n");
        }
        reader.close();

        if (!respCodeOk) {
            throw new IOException(builder.toString());
        }

        final String pattern = "\\<\\!-- title --\\>\\s+\\<h3\\>(.*?)\\</h3\\>";
        final Matcher mat = Pattern.compile(pattern).matcher(builder.toString());

        if (!mat.find()) {
            throw new IOException("Title not found");
        }
        final Set<String> set  = new HashSet<String>();
        final String[] titles = mat.group(1).split("/");
        for (String title : titles) {
            set.add(title.trim());
        }

        return set;
    }

    public static String limitString(final String in,
                                     final int len) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        if (len <= 0) {
            throw new IllegalArgumentException("len[" + len + "] <= 0");
        }
        final int strLen = in.length();
        final String out;

        if (strLen <= len) {
            out = in;
        } else {
            out = in.substring(0, len) + "...";
        }
        return out;
    }

    public static boolean isDomain(final String surl)
                                                  throws MalformedURLException {
        if (surl == null) {
            throw new NullPointerException("surl");
        }
        final Matcher mat = Pattern.compile("([a-zA-Z]{3,10}://)?\\w+/.+")
                                                          .matcher(surl.trim());
        //final URL url = new URL(surl);

        return (!mat.find());
    }

    /**
     *
     * @param urls list of input urls
     * @param id if some urls are only domain, return only that with this id
     * @return return a set of url that are not only domais except one.
     * @throws java.net.MalformedURLException
     */
    public static Set<IdUrl> filterDomains(final Set<IdUrl> urls,
                                           final String id)
                                                  throws MalformedURLException {
        if (urls == null) {
            throw new NullPointerException("urls");
        }
        if (id == null) {
            throw new NullPointerException("id");
        }
        final Set<IdUrl> ret = new HashSet<IdUrl>();

        for (IdUrl url : urls) {
            if (isDomain(url.url)) {
                if (url.id.equals(id)) {
                    ret.add(url);
                }
            } else {
                ret.add(url);
            }
        }

        return ret;
    }

    /**
     * Removes from url patterns %xx not allowed by enc/decoding rules
     * @param in input string
     * @return string with bad patterns removed from original string
     */
    public static String badUrlFix(final String in) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        final Matcher mat = Pattern.compile(
                    "(%([^0-9a-fA-F]|[0-9a-fA-F][^0-9a-fA-F]))").matcher(in);
        final StringBuffer sb = new StringBuffer();

        while (mat.find()) {
            mat.appendReplacement(sb, "");
        }
        mat.appendTail(sb);

        return sb.toString();
    }

    public static void main(final String[] args) {
        String[] result;

        String oldStr = "abacate";
        String newStr = "cate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "cate";
        newStr = "abacate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "xxxxcate";
        newStr = "yyacate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "abacate";
        newStr = "abacate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "aba";
        newStr = "abacate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "abacate";
        newStr = "aba";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "abaxxxx";
        newStr = "abay";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "melancia";
        newStr = "abacate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");

        oldStr = "abaxxyyte";
        newStr = "abacate";

        result = commonPreSuffix(oldStr, newStr);
        System.out.println("[" + result[0] + "]   [" + result[1] + "]");
    }
}

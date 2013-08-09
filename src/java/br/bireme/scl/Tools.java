package br.bireme.scl;

import java.util.HashSet;
import java.util.Set;

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
                    "([\\^\\$\\\\?\\+\\*\\{\\}\\(\\)\\|\\&])", "\\\\$1");
        return out;
    }
    
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
                from = "^" + brokenUrl + "$";
                to = fixedUrl;
            } else {
                final int len = prefix.length();
                final int pos1 = brokenUrl.indexOf(prefix) + len;
                final int pos2 = fixedUrl.indexOf(prefix) + len;
                from = escapeChars(brokenUrl.substring(pos1)) + "$";
                to = escapeChars(fixedUrl.substring(pos2));         
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
            outSet.add(new IdUrl(iu.id, newUrl));
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

/*=========================================================================

    Copyright © 2013 BIREME/PAHO/WHO

    This file is part of SocialCheckLinks.

    SocialCheckLinks is free software: you can redistribute it and/or 
    modify it under the terms of the GNU Lesser General Public License as 
    published by the Free Software Foundation, either version 2.1 of 
    the License, or (at your option) any later version.

    SocialCheckLinks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public 
    License along with SocialCheckLinks. If not, see 
    <http://www.gnu.org/licenses/>.

=========================================================================*/

package br.bireme.scl;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heitor Barbieri
 * date: 20130802
 */
public class CheckUrlArray {
    class CheckUrlThread extends Thread {
        final String url;
        int retCode;
        
        public CheckUrlThread(final String url) {            
            this.url = url;
            this.retCode = -1;
        }
        
        @Override
        public void run() {
            retCode = CheckUrl.check(url);
//System.out.println("retCode=" + retCode);                        
        }
    }
    
    public int[] check(final String[] purls) {
        if (purls == null) {
            throw new NullPointerException("purls");
        }
        final int len = purls.length;
        final CheckUrlThread[] threads = new CheckUrlThread[len];
        final int[] errorCodes = new int[len];
        
        for (int idx = 0; idx < len; idx++) {
            final String url = purls[idx];
            
            if (url == null) {
                throw new NullPointerException("null url");
            }
            threads[idx] = new CheckUrlThread(url);
            threads[idx].start();
        }
        int total = 0;
        
        while (true) {            
            for (int idx = 0; idx < len; idx++) {
                if ((errorCodes[idx] == 0) && (threads[idx].retCode != -1)) {
                    errorCodes[idx] = threads[idx].retCode;
                    total++;
                }
            }
//System.out.println("total=" + total);
            if (total == len) {
                break;
            }
            try {
                Thread.sleep(250);
//System.out.println("sleeping...total=" + total + " len=" + len);
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckUrlArray.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//System.out.println("total=" + total);
        return errorCodes;
    }        
}

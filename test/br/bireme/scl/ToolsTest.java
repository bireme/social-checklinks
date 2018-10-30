/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Heitor Barbieri
 * date: 20150610
 */
public class ToolsTest {

    public ToolsTest() {
    }

    /**
     * Test of badUrlFix method, of class Tools.
     */
    @Test
    public void testBadUrlFix() {
        System.out.println("badUrlFix");
        String in;
        String expResult;
        String result;

        in = "http://xxxxxx%asdf";
        expResult = "http://xxxxxxdf";
        result = Tools.badUrlFix(in);
        assertEquals(expResult, result);

        in = "http://xxxxxx%0sdf";
        expResult = "http://xxxxxxdf";
        result = Tools.badUrlFix(in);
        assertEquals(expResult, result);

        in = "http://xxxxxx%psdf";
        expResult = "http://xxxxxxsdf";
        result = Tools.badUrlFix(in);
        assertEquals(expResult, result);

        in = "http://xxxxxx%C9df";
        expResult = "http://xxxxxx%C9df";
        result = Tools.badUrlFix(in);
        assertEquals(expResult, result);

        in = "http://xxxxxx%c0df";
        expResult = "http://xxxxxx%c0df";
        result = Tools.badUrlFix(in);
        assertEquals(expResult, result);
    }

}

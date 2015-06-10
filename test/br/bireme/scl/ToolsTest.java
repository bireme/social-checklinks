/*=========================================================================

    Copyright © 2015 BIREME/PAHO/WHO

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

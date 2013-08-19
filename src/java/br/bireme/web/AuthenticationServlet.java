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

package br.bireme.web;

import br.bireme.accounts.Authentication;
import static br.bireme.scl.BrokenLinks.HISTORY_MONGO_COL;
import static br.bireme.scl.BrokenLinks.MONGO_COL;
import static br.bireme.scl.BrokenLinks.MONGO_DB;
import br.bireme.scl.IdUrl;
import br.bireme.scl.MongoOperations;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;

/**
 *
 * @author Heitor Barbieri
 * date: 20130731
 */
public class AuthenticationServlet extends HttpServlet {
    @Override
    public void init() {
        try {
            final ServletContext context = getServletContext();
            final String host = context.getInitParameter("Host"); 
            final MongoClient mongoClient = new MongoClient(host);
            final DB db = mongoClient.getDB(MONGO_DB);
            final DBCollection coll = db.getCollection(MONGO_COL);
            final DBCollection hcoll = db.getCollection(HISTORY_MONGO_COL);            
            
            context.setAttribute("collection", coll);
            context.setAttribute("historycoll", hcoll);
        } catch (UnknownHostException ex) {
            Logger.getLogger(AuthenticationServlet.class.getName())
                                                   .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(final HttpServletRequest request, 
                                  final HttpServletResponse response)
                                          throws ServletException, IOException {
        
        final String username = request.getParameter("email");
        final String password = request.getParameter("password");
                
        boolean isAccountsWorking = true;        
        
        if (isAccountsWorking) {
            if ((username == null) || (username.isEmpty()) || 
                (password == null) || (password.isEmpty())) {
                response.sendRedirect("index.html");
                return;
            }
            
            try {            
                final Authentication auth = new Authentication();
                final JSONObject user = auth.getUser(username, password);
                final String centerId = auth.getCenterId(user);
                final HttpSession session = request.getSession();
                
                if (auth.isAuthenticated(user) && (centerId != null)) {                    
                    session.setAttribute("user", username); // Login user.
                    session.setAttribute("centerId", centerId);                    
                    //session.setAttribute("group", 0);
                    response.sendRedirect("list.jsp?group=0"); // Redirect to user home page.
                } else {
                    session.removeAttribute("user");
                    session.removeAttribute("centerId");
                    //session.removeAttribute("centerUrls");
                    //session.removeAttribute("group");
                    response.sendRedirect("index.html");
                }            
            } catch(Exception ex) {
//ex.printStackTrace();
                response.sendRedirect("index.html");
            }
        } else {
            final ServletContext context = getServletContext();
            final DBCollection coll = 
                               (DBCollection)context.getAttribute("collection");
            final List<IdUrl> centerUrls = MongoOperations.getCenterUrls(coll, "PE1.1");
            final HttpSession session = request.getSession();
            
            session.setAttribute("user", username); // Login user.
            //session.setAttribute("centerId", "BR1.1");
            session.setAttribute("centerId", "PE1.1");
            session.setAttribute("centerUrls", centerUrls);
            //session.setAttribute("group", 0);
            response.sendRedirect("list.jsp?group=0");
            //response.sendRedirect("list.jsp");            
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

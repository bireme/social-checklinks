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

package br.bireme.web;

import br.bireme.accounts.Authentication;
import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import br.bireme.scl.MongoOperations;
import br.bireme.scl.Tools;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
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
    private static final String CODEC = "UTF-8";
    
    @Override
    public void init() {
        try {
            final ServletContext context = getServletContext();
            final String host = context.getInitParameter("host");
            final int port = Integer.parseInt(context.getInitParameter("port")); 
            final String user = context.getInitParameter("username"); 
            final String password = context.getInitParameter("password"); 
            final MongoClient mongoClient = new MongoClient(host, port);
            final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);
                                    
            if (! user.trim().isEmpty()) {
                final boolean auth = 
                                  db.authenticate(user, password.toCharArray());        
                if (!auth) {
                    throw new IllegalArgumentException("invalid user/password");
                }
            }
            
            final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);
            final DBCollection hcoll = db.getCollection(HISTORY_COL);            
            final Set<String> databases = 
                                      MongoOperations.getDatabases(coll);
            
            context.setAttribute("userEmail", user.trim());
            context.setAttribute("collection", coll);
            context.setAttribute("historycoll", hcoll);
            context.setAttribute("readOnlyMode", false);
            context.setAttribute("databases", databases);            
        } catch (Exception ex) {
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
        
        request.setCharacterEncoding(CODEC);
        
        final String username = request.getParameter("email");
        final String password = request.getParameter("password");
        final String lang = request.getParameter("lang");                        
        final ServletContext context = getServletContext();
        final HttpSession session = request.getSession();          
        final ResourceBundle messages = Tools.getMessages(lang);
        
        boolean isAccountsWorking = true;    
        RequestDispatcher dispatcher;
        
        session.removeAttribute("collCenter");
        session.removeAttribute("user");
        
        if (isAccountsWorking) {
            if ((username == null) || (username.isEmpty()) || 
                (password == null) || (password.isEmpty())) {
                response.sendRedirect("index.jsp?lang=" + lang 
                        + "&errMsg=" + messages.getString("login_is_required"));
                return;
            }                        
            
            try {            
                final Authentication auth = new Authentication(
                                     context.getInitParameter("accounts_host"));
                final JSONObject user = auth.getUser(username, password);                
                                
                if (auth.isAuthenticated(user)) {
                    final String centerId = auth.getColCenter(user);                    
                    final DBCollection coll = (DBCollection)context
                                                    .getAttribute("collection");
                    final Set<String> centerIds;
                    if (centerId.equals("BR1.1")) { 
                        centerIds = MongoOperations.getCenters(coll);
                    } else {
                        final Set<String> auxCenterIds = auth.getCenterIds(user);
                        // cc may not belong to a net (it not appear in centerIds)
                        auxCenterIds.add(auth.getColCenter(user)); 
                        centerIds = MongoOperations.filterCenterFields(coll, 
                                                                  auxCenterIds);
                    }
                    session.setAttribute("user", username); // Login user.
                    session.setAttribute("cc", centerId);
                    session.setAttribute("centerIds", centerIds);   
                    dispatcher = context.getRequestDispatcher(
                                   "/CenterFilterServlet?lang=" + lang);
                } else {
                    session.removeAttribute("user");
                    session.removeAttribute("cc");
                    session.removeAttribute("centerIds");
                    dispatcher = context.getRequestDispatcher(
                                                  "/index.jsp?lang=" + lang 
                    + "&errMsg=" + messages.getString("authentication_failed"));
                }     
                dispatcher.forward(request, response);
            } catch(Exception ex) {
                dispatcher = context.getRequestDispatcher(
                                                    "/index.jsp?lang=" + lang
                          + "&errMsg=" + messages.getString("exception_found")
                          + "<br/><br/>" + ex.getMessage());                
                dispatcher.forward(request, response);
            }
        } else {            
            final Set<String> ccs = new HashSet<String>();
            ccs.add("PE1.1");
            ccs.add("BR1.1");
            dispatcher = context.getRequestDispatcher(
                                   "/CenterFilterServlet?lang=" + lang);             
            session.setAttribute("user", username); // Login user.
            session.setAttribute("cc", "BR1.1");
            session.setAttribute("centerIds", ccs);
            dispatcher.forward(request, response);
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

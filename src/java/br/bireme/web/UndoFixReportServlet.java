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

package br.bireme.web;

import br.bireme.scl.MongoOperations;
import br.bireme.scl.Tools;
import com.mongodb.DBCollection;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Heitor Barbieri
 * date: 20150414
 */
public class UndoFixReportServlet extends HttpServlet {
    private static final String CODEC = "UTF-8";
    
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

        String lang = request.getParameter("lang");
        if (lang == null) {
            lang = "en";
        }
        ResourceBundle messages = Tools.getMessages(lang);
        if (messages == null) {
            lang = "en";
            messages = Tools.getMessages(lang);
        }

        final HttpSession session = request.getSession();
        final String user = (String)session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
            return;
        }

        final ServletContext context = getServletContext();
        final DBCollection coll = (DBCollection)context
                                                    .getAttribute("collection");           
        final DBCollection hcoll = (DBCollection)context
                                                   .getAttribute("historycoll");

        //--------------- List Broken Links paramenters ----------------------------

        String dbFilter = request.getParameter("dbFilter");
        dbFilter = "null".equals(dbFilter) ? null : dbFilter;

        String idFilter = request.getParameter("idFilter");
        idFilter = "null".equals(idFilter) ? null : idFilter;

        String urlFilter = request.getParameter("urlFilter");
        urlFilter = "null".equals(urlFilter) ? null : urlFilter;

        String collCenterFilter = request.getParameter("collCenterFilter");
        collCenterFilter = "null".equals(collCenterFilter) ? null 
                                                           : collCenterFilter;

        String sgroup = request.getParameter("group");
        int group = ((sgroup == null ) || "null".equals(sgroup)) ? 0 
                                                     : Integer.parseInt(sgroup);

        String dateFilter = request.getParameter("dateFilter");
        dateFilter = "null".equals(dateFilter) ? null : dateFilter;

        String order = request.getParameter("order");
        order = "null".equals(order) ? "descending" : order;

        //--------------- Report Broken Links parameters ---------------------------

        String r_dbFilter = request.getParameter("r_dbFilter");
        r_dbFilter = "null".equals(r_dbFilter) ? null : r_dbFilter;

        String r_idFilter = request.getParameter("r_idFilter");
        r_idFilter = "null".equals(r_idFilter) ? null : r_idFilter;

        String r_urlFilter = request.getParameter("r_urlFilter");
        r_urlFilter = "null".equals(r_urlFilter) ? null : r_urlFilter;

        String r_collCenterFilter = request.getParameter("r_collCenterFilter");
        r_collCenterFilter = "null".equals(r_collCenterFilter) ? null 
                                                           : r_collCenterFilter;

        String r_sgroup = request.getParameter("r_group");
        int r_group = ((r_sgroup == null) || "null".equals(r_sgroup)) ? 0 
                                                   : Integer.parseInt(r_sgroup);

        String r_dateFilter = request.getParameter("r_dateFilter");
        r_dateFilter = "null".equals(r_dateFilter) ? null : r_dateFilter;

        String r_userFilter = request.getParameter("r_userFilter");
        r_userFilter = "null".equals(r_userFilter) ? null : r_userFilter;

       //-----------------------------------------------------------------------
        
        String id = request.getParameter("id");
        id = "null".equals(id) ? null : id;
        
        if (! MongoOperations.undoUpdateDocument(coll, hcoll, id, true)) {
           throw new IOException("Undo operation failed.");
        }

        response.sendRedirect(response.encodeRedirectURL("report.jsp?lang=" + 
                lang + "&group=" + group + "&dbFilter=" + dbFilter + 
                "&idFilter=" + idFilter + "&urlFilter=" + urlFilter + 
                "&collCenterFilter=" + collCenterFilter + "&order=" + order + 
                "&r_dbFilter=" + r_dbFilter + "&r_idFilter=" + r_idFilter + 
                "&r_urlFilter=" + r_urlFilter + "&r_collCenterFilter=" + 
                r_collCenterFilter + "&r_group=" + r_group + "&r_dateFilter=" + 
                r_dateFilter + "&r_userFilter=" + r_userFilter));
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
    protected void doGet(final HttpServletRequest request,
                          final HttpServletResponse response)
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
    protected void doPost(final HttpServletRequest request,
                           final HttpServletResponse response)
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

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

import br.bireme.scl.CheckUrl;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Check if an URL is broken or not.
 * @author Heitor Barbieri
 * 20130719
 */
public class CheckOneLinkServlet extends HttpServlet {
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
        
        final String id = request.getParameter("id");
        final String url = request.getParameter("url");
        final String url_E = URLEncoder.encode(url, CODEC);        
        final String furl = request.getParameter("furl");
        final String furl_E = URLEncoder.encode(furl, CODEC);        
        final String lang = request.getParameter("lang");
        final String group = request.getParameter("group");
        final String collCenterFilter = request.getParameter("collCenterFilter");
        
        final String sorder = request.getParameter("order");
        final String order = "null".equals(sorder) ? "descending" : sorder;
        final String sdbFilter = request.getParameter("dbFilter");
        final String dbFilter = "null".equals(sdbFilter) ? null : sdbFilter;    
    
        final int errCode = CheckUrl.check(furl);
        final boolean isBroken = CheckUrl.isBroken(errCode);
        final ServletContext context = getServletContext();
        final RequestDispatcher dispatcher = context.getRequestDispatcher(
                                   "/editRecord.jsp?id=" + id + "&url=" + url_E
                      + "&furl=" + furl_E + "&status=" + (isBroken ? 1 : 0)
                      + "&lang=" + lang + "&group=" + group 
                      + "&dbFilter=" + dbFilter                     
                      + "&collCenterFilter=" + collCenterFilter 
                      + "&order=" + order);
        dispatcher.forward(request, response);
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

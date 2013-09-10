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

import br.bireme.scl.IdUrl;
import br.bireme.scl.MongoOperations;
import com.mongodb.DBCollection;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Heitor Barbieri
 * date: 20130806
 */
public class CheckManyLinksServlet extends HttpServlet {

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
        final ServletContext context = getServletContext();
        final DBCollection coll =
                               (DBCollection)context.getAttribute("collection");
        final DBCollection hcoll =
                              (DBCollection)context.getAttribute("historycoll");
        final HttpSession session = request.getSession();
        final String user = (String)session.getAttribute("user");
        final String collCenterFilter = 
                               (String)session.getAttribute("collFilterCenter");
        final Set<String> centerIds = (Set<String>)request.getSession()
                                                     .getAttribute("centerIds");         
        final String brokenUrl = (String)request.getParameter("url");
        final String brokenUrl2 = brokenUrl.replaceAll("<<amp;>>", "&");
        final String fixedUrl = (String)request.getParameter("furl");
        final String fixedUrl2 = fixedUrl.replaceAll("<<amp;>>", "&");
        final String lang = (String)request.getParameter("lang");
        final Set<IdUrl> fixed = MongoOperations.fixRelatedUrls(coll, hcoll,
                      user, centerIds, collCenterFilter, brokenUrl2, fixedUrl2);
        final RequestDispatcher dispatcher = context.getRequestDispatcher(
                                    "/showFixedUrls.jsp?group=0&lang=" + lang);

        session.setAttribute("url", fixedUrl2);
        session.setAttribute("IdUrls", fixed);
        //request.("group", "0");
        //request.setAttribute("lang", lang);
        dispatcher.forward(request, response);
        //response.sendRedirect("showFixedUrls.jsp?group=0&lang=" + lang);
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

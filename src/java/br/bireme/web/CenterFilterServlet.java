/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Heitor Barbieri
 * date 20130906
 */
public class CenterFilterServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(final HttpServletRequest request,
                                  final HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        final String lang = request.getParameter("lang");
        final String order = request.getParameter("order");
        final String collCenterFilter = request.getParameter("collCenterFilter");
        final String dbFilter = request.getParameter("dbFilter");
        final String idFilter = request.getParameter("idFilter");
        final String urlFilter = request.getParameter("urlFilter");

        /*final HttpSession session = request.getSession();
        final String collFilterCenter = (String)
                                     session.getAttribute("collFilterCenter");*/
        final ServletContext context = getServletContext();
        final RequestDispatcher dispatcher = context.getRequestDispatcher(
           "/list.jsp?group=0&lang=" + lang + "&order=" + order +
            (collCenterFilter == null ? "" : "&collCenterFilter=" + collCenterFilter) +
            (dbFilter == null ? "" : "&dbFilter=" + dbFilter) +
            (idFilter == null ? "" : "&idFilter=" + idFilter) +
            (urlFilter == null ? "" : "&urlFilter=" + urlFilter));
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

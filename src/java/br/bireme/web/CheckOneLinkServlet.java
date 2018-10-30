/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.web;

import br.bireme.scl.CheckUrl;
import br.bireme.scl.EncDecUrl;
import java.io.IOException;
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
        final String url_E = EncDecUrl.encodeUrl(request.getParameter("url"),
                                                                   CODEC, true);
        final String furl = request.getParameter("furl").trim();
        final String furl_E1 = EncDecUrl.encodeUrl(furl, CODEC, false);
        final String furl_E2 = EncDecUrl.encodeUrl(furl, CODEC, true);
        final String lang = request.getParameter("lang");
        final String group = request.getParameter("group");
        final String collCenterFilter = request.getParameter("collCenterFilter");

        final String sorder = request.getParameter("order");
        final String order = "null".equals(sorder) ? "descending" : sorder;
        final String sdbFilter = request.getParameter("dbFilter");
        final String dbFilter = "null".equals(sdbFilter) ? null : sdbFilter;
        final String sisNew = request.getParameter("new");
        final String isNew = "null".equals(sisNew) ? "0" : sisNew;

        final int errCode = CheckUrl.check(furl_E1);
        final String errMsg = CheckUrl.getMessage(errCode);
        final boolean isBroken = CheckUrl.isBroken(errCode);
        final ServletContext context = getServletContext();
        final RequestDispatcher dispatcher = context.getRequestDispatcher(
                                   "/editRecord.jsp?id=" + id + "&url=" + url_E
                      + "&furl=" + furl_E2 + "&status=" + (isBroken ? 1 : 0)
                      + "&lang=" + lang + "&group=" + group
                      + "&dbFilter=" + dbFilter
                      + "&collCenterFilter=" + collCenterFilter
                      + "&order=" + order
                      + "&new=" + isNew
                      + "&errCode=" + errCode
                      + "&errMsg=" + errMsg);
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

/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.web;

import br.bireme.scl.Tools;
import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Heitor Barbieri
 * date 20131121
 */
public class GoogleSearchServlet extends HttpServlet {
    private static final String CODEC = "UTF-8";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
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

        final String url = request.getParameter("url");
        final Set<String> exprSet = Tools.getTitles(url);
        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String expr : exprSet) {
            final String term = expr.trim().replaceAll(" +", "+");

            if (first) {
                first = false;
            } else {
                builder.append("+OR+");
            }
            builder.append("%22");
            builder.append(term);
            builder.append("%22");
        }

        final String gurl =
                //"https://www.google.com.br/?ei=8PyNUoWpOMyNkAel9IDIAg#q=" +
                "https://www.google.com/search?q=" +
                builder.toString();
        response.sendRedirect(gurl);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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

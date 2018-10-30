/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.web;

import br.bireme.scl.IdUrl;
import br.bireme.scl.MongoOperations;
import com.mongodb.DBCollection;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Heitor Barbieri
 * date 20130822
 */
public class UndoFixServlet extends HttpServlet {
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

        final ServletContext context = getServletContext();
        final DBCollection coll =
                               (DBCollection)context.getAttribute("collection");
        final DBCollection hcoll =
                              (DBCollection)context.getAttribute("historycoll");
        final HttpSession session = request.getSession();
        final Set<IdUrl> fixed = (Set<IdUrl>)session.getAttribute("IdUrls");
        final Set<IdUrl> nfixed = new HashSet<IdUrl>();
        final String undoUrl = request.getParameter("undoUrl");
        final String group = request.getParameter("group");
        final String lgroup = request.getParameter("lgroup");
        final String lang = request.getParameter("lang");
        final String id = request.getParameter("id");
        final String brokenUrl = request.getParameter("brokenUrl");
        final String url = request.getParameter("url");
        final String dbFilter = request.getParameter("dbFilter");
        final String collCenterFilter = request.getParameter("collCenterFilter");
        final String order = request.getParameter("order");

        for (IdUrl iu : fixed) {
            if (iu.id.equals(id)) {
                if (! MongoOperations.undoUpdateDocument(coll, hcoll, iu.id,
                                                                        true)) {
                   throw new IOException("Undo operation failed.");
                }
            } else {
                nfixed.add(iu);
            }
        }
        session.setAttribute("IdUrls", nfixed);
        response.sendRedirect(response.encodeRedirectURL(
                   "showFixedUrls.jsp?group=" + group + "&lgroup=" + lgroup
                  + "&lang=" + lang + "&id=" + id + "&brokenUrl=" + brokenUrl
                  + "&url=" + url + "&undoUrl=" + undoUrl + "&dbFilter="
                  + dbFilter + "&collCenterFilter=" + collCenterFilter
                  + "&order=" + order + "&undo=true"));

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.bireme.web;

import br.bireme.scl.IdUrl;
import br.bireme.scl.MongoOperations;
import com.mongodb.DBCollection;
import java.io.IOException;
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
        final String centerId = (String)session.getAttribute("centerId");
        final String brokenUrl = (String)request.getParameter("url");
        final String brokenUrl2 = brokenUrl.replaceAll("<<amp;>>", "&");
        final String fixedUrl = (String)request.getParameter("furl");
        final String fixedUrl2 = fixedUrl.replaceAll("<<amp;>>", "&");
        final String docId = (String)request.getParameter("id");
        final Set<IdUrl> fixed = MongoOperations.fixRelatedUrls(coll, hcoll, 
                                         user, centerId, brokenUrl2, fixedUrl2);
        
        /*if (!MongoOperations.updateDocument(coll, hcoll, docId, fixedUrl2, user, 
                                                                       false)) {
            throw new IOException("update url failded");
        }*/
                                         
        session.setAttribute("url", fixedUrl2);
        session.setAttribute("IdUrls", fixed);
        session.setAttribute("group", 0);
        response.sendRedirect("showFixedUrls.jsp");
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

package br.bireme.web;

import br.bireme.accounts.Authentication;
import static br.bireme.accounts.Authentication.DEFAULT_HOST;
import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Heitor Barbieri
 * date 20140523
 */
@WebServlet(name = "CheckServlet", urlPatterns = {"/check"})
public class CheckServlet extends HttpServlet {
    private final String USER = "barbieri@paho.org";
    private final String PASSWORD = "heitor";
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.json.simple.parser.ParseException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Check Servlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Check Servlet</h1>");
            out.println("<h2>Accounts:</h2>");
            out.println("<h3>Production:</h3>");
            try {
                out.println(checkAccounts(DEFAULT_HOST));
            } catch(Exception ex) {
                out.println("Accounts failed:" + ex.toString());
            }            
            out.println("<h3>Test:</h3>");
            try {
                out.println(checkAccounts("accounts.teste.bireme.org"));
            } catch(Exception ex) {
                out.println("Accounts failed:" + ex.toString());
            }
            out.println("<h2>MongoDB:</h2>");
            out.println("<h3>Production:</h3>");
            out.println(checkMongoDB("mongodb.bireme.br"));
            out.println("<h3>Test:</h3>");
            out.println(checkMongoDB("ts01vm.bireme.br"));
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }
    
    private String checkAccounts(final String host) throws IOException, 
                                                           ParseException {
        assert host != null;
        
        final Authentication aut = new Authentication(host);

        final JSONObject response = aut.getUser(USER, PASSWORD);
        
        return response.toJSONString().replaceAll("\n", "<br/");
    }

    private String checkMongoDB(final String host) {
        assert host != null;
        
        String ret;
        
        try {
            final MongoClient mongoClient = new MongoClient(host, 
                                                            DEFAULT_PORT);
            final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);
            final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);

            final BasicDBObject bobj = (BasicDBObject)coll.findOne();
            ret = bobj.toString().replaceAll("\n", "<br/>");
        } catch(Exception ex) {
            ret = ex.getMessage();
        }
        
        return ret;
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
    protected void doGet(final HttpServletRequest request, 
                         final HttpServletResponse response)
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
        return "Checks Bireme Accounts and MongoDB access";
    }// </editor-fold>

}

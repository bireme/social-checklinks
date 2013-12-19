<%--

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

--%>

<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.*,com.mongodb.DBCollection,br.bireme.scl.*,br.bireme.scl.MongoOperations" %>
<%@page contentType="text/html;charset=UTF-8"%>

<% 
    String lang = (String)request.getParameter("lang");
    if (lang == null) {
        lang = "en";
    }
    final ResourceBundle messages = Tools.getMessages(lang);
    final String user = (String)session.getAttribute("user");
    
    if (user == null) {
        response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
        return;
    }
        
    final String collCenterFilter = 
                               (String)session.getAttribute("collFilterCenter");
    final ServletContext context = getServletContext();
    final boolean readOnlyMode = (Boolean)context.getAttribute("readOnlyMode");
    final DBCollection coll = (DBCollection)context.getAttribute("collection");
    final Set<String> centerIds = (Set<String>)request.getSession()
                                                     .getAttribute("centerIds");
    final String dbFilter = request.getParameter("dbFilter");
    final String idFilter = request.getParameter("idFilter");
    final String urlFilter = request.getParameter("urlFilter");
    final int group = Integer.parseInt(request.getParameter("group"));
    final int groupSize = 17;
    final List<IdUrl> lst;
    final int maxUrls;
    
    String order = request.getParameter("order");
    order = (order == null) ? "descending" : order;

    if (dbFilter != null) {
        lst = MongoOperations.getDocMaster(coll, dbFilter,
                                           (group * groupSize) + 1, groupSize);
        maxUrls = MongoOperations.getDocMasterNum(coll, dbFilter);
    } else if (idFilter != null) {
        lst = MongoOperations.getDocId(coll, idFilter);
        maxUrls = lst.size();
    } else if (urlFilter != null) {
        lst = MongoOperations.getDocUrl(coll, urlFilter);
        maxUrls = lst.size();
    } else {
        lst = MongoOperations.getCenterUrls(coll, centerIds, 
               collCenterFilter, (group * groupSize) + 1, groupSize,
               order.equals("ascending"));
        maxUrls = MongoOperations.getCentersUrlsNum(coll, centerIds,             
                                                              collCenterFilter);
    }
    final int mod = (maxUrls % groupSize);
    int lastGroup = (maxUrls / groupSize);
    lastGroup = ((maxUrls > 0) && (mod == 0)) ? lastGroup - 1 : lastGroup;
    final int initGroup = (group <= 1) ? 0 : (group >= lastGroup - 2) 
                                                ? (lastGroup - 4) : (group - 2);
    final int from = (group * groupSize);        
    final boolean showCenters = (centerIds.size() > 1);
%>

<!-- ================================================== -->

<!doctype html>
<html>
    <head>
	<title><%=messages.getString("bireme_social_checklinks")%></title>
	<meta charset="utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="HandheldFriendly" content="true"/>
	<link href="css/bootstrap.css" rel="stylesheet"/>
	<link href="css/bootstrap-responsive.css" rel="stylesheet"/>
	<link href="css/styles.css" rel="stylesheet"/>
	<style type="text/css">
            html, body {
                height: 98.3%; /* The html and body elements cannot have any padding or margin. */
                padding-top: 15px;
            }
        </style>

	<!--[if (lt IE 9)&(!IEMobile)]>
	<link rel="stylesheet" type="text/css" href="css/ie.css" />
	<![endif]-->
	<script type="text/javascript" src="js/modernizr.js"></script>        
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
        <script LANGUAGE="JavaScript" TYPE="text/javascript">        
            
        function postToUrl(path, params) {
            var form = document.createElement("form");
            form.setAttribute("method", "post");
            form.setAttribute("action", path);

            for(var key in params) {
                if (params.hasOwnProperty(key)) {
                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", key);
                    hiddenField.setAttribute("value", params[key]);

                    form.appendChild(hiddenField);
                 }
            }

            document.body.appendChild(form);
            form.submit();
        }
        function isNumber(n) {
            return !isNaN(parseFloat(n)) && isFinite(n);
        }
        function gotoPage(elemId) {
            var goto = document.getElementById(elemId).value;
            
            if (goto && isNumber(goto)) {
                postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:(goto - 1),lang:'<%=lang%>',order:'<%=order%>'});
            } else {
                postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=lastGroup%>',lang:'<%=lang%>',order:'<%=order%>'});
            }
        }
        </script>        
    </head>
        <body>
            <div id="wrap">
		<div class="navbar navbar-inverse navbar-fixed-top">
                    <div class="navbar-inner">
                        <div class="container">
                            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
                            </button>
                            <a class="brand" href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',order:'<%=order%>'});"><%=messages.getString("bireme_social_checklinks")%></a>
                            <div class="nav-collapse collapse">
                                <ul class="nav">
                                    <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',order:'<%=order%>'});"><%=messages.getString("home")%></a></li>
                                    <li><a href="http://wiki.bireme.org/pt/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
                                    <li><a href="http://feedback.bireme.org/feedback/?application=socialchecklinks&version=<%=BrokenLinks.VERSION%>&lang=<%=lang%>" target="_blank"><%=messages.getString("contact")%></a></li>
                                </ul>
                                <ul class="nav pull-right">
                                    <li class="dropdown">
                                        <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                        <ul class="dropdown-menu">
                                            <li <%if(lang.equals("en")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'en',order:'<%=order%>'});">English</a></li>
                                            <li <%if(lang.equals("pt")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'pt',order:'<%=order%>'});">Português</a></li>
                                            <li <%if(lang.equals("es")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'es',order:'<%=order%>'});">Español</a></li>
                                            <!--li<%if(lang.equals("fr")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'fr',order:'<%=order%>'});">Francés</a></li-->
                                        </ul>
                                    </li>
                                    <li class="dropdown">
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user %> <b class="caret"></b></a>
                                        <ul class="dropdown-menu">
                                            <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("index.jsp")%>', {lang:'<%=lang%>'});"><i class="icon-off"></i> <%=messages.getString("logout")%></a></li>
                                        </ul>
                                    </li>
                                </ul>
                            </div><!--/.nav-collapse -->
			</div>
                    </div>
		</div>

		<div class="container">
                    <div class="breadcrumb">
                        <ul class="breadcrumb">
                            <li class="active"><%=messages.getString("list")%></li>
                        </ul>     
                    </div>
                    <h1><%=messages.getString("broken_links")%></h1>
                    <p><%=messages.getString("the_list")%></p>
                    <% if(readOnlyMode) { %>
                        <div class="alert alert-danger fade in">
                            <button data-dismiss="alert" class="close" type="button">×</button>
                            <strong>Base de dados em manutenção</strong> Edição das URLs temporariamente indisponível.
                        </div>                  
                    <% } %>
                    <table class="table table-condensed">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>                                    
                                    <div class="nav-collapse">
                                        <ul style="list-style: none; padding: 0px; margin: 0px;">
                                            <li class="dropdown">
                                                <a class="dropdown-toggle" href="#" data-toggle="dropdown"><%=messages.getString("database")%><strong class="caret"></strong></a>
                                                <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                    <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                        <li style="margin-bottom: 8px;">
                                                            <li style="margin-bottom: 8px;">
                                                                <a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>',group:'0',order:'<%=order%>'});"><%=messages.getString("all")%></a>                                                                
                                                            </li>                                                            
                                                        </li>
                                                        <li>
                                                            <form action='<%=response.encodeRedirectURL("list.jsp?lang="+ lang + "&group=0&order="+ order)%>' method="post" >
                                                                <input name="dbFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_database")%>" />
                                                            </form>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>                                    
                                </th>
                                <th>                                    
                                    <div class="nav-collapse">
                                        <ul style="list-style: none; padding: 0px; margin: 0px;">
                                            <li class="dropdown">
                                                <a class="dropdown-toggle" href="#" data-toggle="dropdown">ID<strong class="caret"></strong></a>
                                                <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                    <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>',group:'0'});"><%=messages.getString("all")%></a></li>
                                                        <li>
                                                            <form action='<%=response.encodeRedirectURL("list.jsp?lang="+ lang + "&group=0$order=" + order)%>' method="post" >
                                                                <input name="idFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_id")%>" />
                                                            </form>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>                                                                                                                                                                                   
                                </th>
                                <th>
                                    <div class="nav-collapse">
                                        <ul style="list-style: none; padding: 0px; margin: 0px;">
                                            <li class="dropdown">
                                                <a class="dropdown-toggle" href="#" data-toggle="dropdown">URL<strong class="caret"></strong></a>
                                                <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                    <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>', group:'0',order:'<%=order%>'});"><%=messages.getString("all")%></a></li>
                                                        <li>
                                                            <form action='<%=response.encodeRedirectURL("list.jsp?lang="+ lang + "&group=0&prder=" + order)%>' method="post" >
                                                                <input name="urlFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_url")%>" />
                                                            </form>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>                                                                                                                                                                                                                                                                                                                                                                      
                                </th>                                                
                                <% if (showCenters) { %>
                                    <th>
                                        <div class="nav-collapse">
                                            <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                <li class="dropdown">
                                                    <a class="dropdown-toggle" href="#" data-toggle="dropdown">CC<strong class="caret"></strong></a>
                                                    <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                        <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                            <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("CenterFilterServlet")%>', {lang:'<%=lang%>', group:'0',order:'<%=order%>'});"><%=messages.getString("all")%></a></li>
                                                            <%
                                                            for (String id : centerIds)  {                               
                                                            %>    
                                                                <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("CenterFilterServlet")%>', {lang:'<%=lang%>',collFilterCenter:'<%=id%>',order:'<%=order%>'});"><%=id%></a></li>
                                                            <%
                                                            } 
                                                            %>
                                                        </ul>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>        
                                    </th>       
                                <% } else { %>
                                    <th>CC</th>
                                <% } %>
                                <th>
                                    <div class="nav-collapse">
                                        <ul style="list-style: none; padding: 0px; margin: 0px;">
                                            <li class="dropdown">
                                                <a class="dropdown-toggle" href="#" data-toggle="dropdown"><%=messages.getString("since")%><strong class="caret"></strong></a>
                                                <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                    <ul style="list-style: none; padding: 0px; margin: 0px;">                                                        
                                                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("CenterFilterServlet")%>', {lang:'<%=lang%>',collFilterCenter:0,order:'ascending'});"><%=messages.getString("ascending_order")%></a></li>
                                                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("CenterFilterServlet")%>', {lang:'<%=lang%>',collFilterCenter:0,order:'descending'});"><%=messages.getString("descending_order")%></a></li>
                                                    </ul>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>
                                </th>
                                <th><%=messages.getString("actions")%></th>                                                
                            </tr>
                        </thead>
                        <tbody>
                            <%
                            int cur = from + 1;
                            for (IdUrl iu : lst) {
                                final String nurl = iu.url.replace("&","<<amp;>>");
                                final String id = iu.id.substring(0,iu.id.indexOf("_"));
                                boolean first = true;                                                                                    
                            %>
                                <tr>                                    
                                    <td><%=cur%></td>
                                    <td><%=iu.mst%></td>
                                    <td><a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id%>"><%=id%></a></td>                                    
                                    <td><a target="_blank" href="<%=iu.url%>"><%=Tools.limitString(iu.url.trim(),77)%></a></td>  
                                    <td>
                                    <%
                                    for (String cc : iu.ccs) {
                                        if (centerIds.contains(cc)) {
                                            if (first) {
                                                first = false;
                                            } else {
                                                out.print(", ");
                                            }
                                            out.print(cc);
                                        }
                                    }
                                    %>             
                                    </td>
                                    <td><%=iu.since%></td>    
                                    <td>
                                        <% if(!readOnlyMode) { %>
                                            <a href="javascript:postToUrl('<%=response.encodeRedirectURL("CheckOneLinkServlet")%>', {id:'<%=iu.id%>',url:'<%=nurl%>',furl:'<%=nurl%>',lang:'<%=lang%>',group:'<%=group%>'});" title="<%=messages.getString("edit_broken_url")%>" class="btn btn-mini btn-primary"> &nbsp;<%=messages.getString("edit")%>&nbsp;</a>&nbsp;&nbsp;
                                            <!--a href="javascript:postToUrl('<%=response.encodeRedirectURL("GoogleSearchServlet")%>', {url:'http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id%>'});" title="<%=messages.getString("edit_broken_url")%>" class="btn btn-mini btn-primary" target="_blank">Google</a></td-->
                                        <% } %>
                                        <a href="GoogleSearchServlet?url=http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id%>" title="<%=messages.getString("look_for_document")%>" class="btn btn-mini btn-primary" target="_blank">Google</a>
                                    </td>
                                </tr>
                            <%
                                cur++;
                            }
                            %>
                        </tbody>
                    </table>
                    <div class="pagination pagination-centered">                               
                        <ul>
                            <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',order:'<%=order%>'});">&laquo;</a></li>
                                    <%                                        
                            for (int idx = initGroup; idx < initGroup+5; idx++) {
                                if (idx == group) {
                            %>
                                    <li class="active"><a><%=idx+1%></a></li>
                            <%
                                } else if (idx <= lastGroup) {
                            %>
                                    <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=idx%>',lang:'<%=lang%>',order:'<%=order%>'});"><%=idx+1%></a></li>
                            <%
                                }
                            }    
                            %>
                            <li><input class="gotoPage" id="gotoPage" type="text" placeholder='<%=messages.getString("goto_page")%>' value="" onkeydown="if (event.keyCode == 13) gotoPage('gotoPage')"  /></li>
                            <li class="enabled"><a href="javascript:gotoPage('gotoPage');">&raquo;</a></li>
                            <!--li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=lastGroup%>',lang:'<%=lang%>',order:'<%=order%>'});">&raquo;</a></li-->
                        </ul>
                    </div>   
                </div> <!-- /container -->
            <!--div id="push"></div-->
	</div>
                                                                                                
	<footer id="footer" class="footer">
            <div class="container">
                <strong>BIREME Social CheckLinks - V<%= BrokenLinks.VERSION %> - 2013</strong><br/>
                <%=messages.getString("source_code")%>: <a href="https://github.com/bireme/">https://github.com/bireme/social-checklinks</a>
            </div>
	</footer>
	<!-- javascript
        ================================================== -->
        <script src="js/jquery.js"></script>
        <script src="js/bootstrap-transition.js"></script>
        <script src="js/bootstrap-alert.js"></script>
        <script src="js/bootstrap-modal.js"></script>
        <script src="js/bootstrap-dropdown.js"></script>
        <script src="js/bootstrap-scrollspy.js"></script>
        <script src="js/bootstrap-tab.js"></script>
        <script src="js/bootstrap-tooltip.js"></script>
        <script src="js/bootstrap-popover.js"></script>
        <script src="js/bootstrap-button.js"></script>
        <script src="js/bootstrap-collapse.js"></script>
        <script src="js/bootstrap-carousel.js"></script>
        <script src="js/bootstrap-typeahead.js"></script>	
    </body>
</html>
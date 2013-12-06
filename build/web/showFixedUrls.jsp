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

<%-- 
    Document   : showFixedUrls
    Created on : 06/08/2013, 11:53:39
    Author     : Heitor Barbieri
--%>

<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.*,com.mongodb.DBCollection,br.bireme.scl.*" %>
<%@page contentType="text/html;charset=UTF-8" %>

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

    //final String url = (String)session.getAttribute("url");
    final Set<IdUrl> fixed = (Set<IdUrl>)session.getAttribute("IdUrls");
    final String url = (String)request.getParameter("url");
    final String brokenUrl = request.getParameter("brokenUrl");
    final int fixedUrls = fixed.size();
    final Set<String> centerIds = (Set<String>)request.getSession()
                                                     .getAttribute("centerIds");
    final boolean showCenters = (centerIds.size() > 1);    
    final int group = Integer.parseInt(request.getParameter("group"));
    final int lgroup = Integer.parseInt(request.getParameter("lgroup")); // list urls group
    final String id = request.getParameter("id");
    final String id2 = id.substring(0, id.lastIndexOf('_'));
    final int groupSize = 17;
    final int from = (group * groupSize);
    final int mod = (fixedUrls % groupSize);
    int lastGroup = (fixedUrls / groupSize);
    lastGroup = ((fixedUrls > 0) && (mod == 0)) ? lastGroup - 1 : lastGroup;
    final int initGroup = (group <= 1) ? 0 : (group >= lastGroup - 2) 
                                                ? (lastGroup - 4) : (group - 2);
    final Set<IdUrl> fixedX = new HashSet<IdUrl>();
    int current = 0;
    int begin = group * groupSize;
    int end = begin + groupSize - 1;
    for (IdUrl iu : fixed) {
        if (current < begin) {
            
        } else if (current <= end) {
            fixedX.add(iu);
        } else {
            break;
        }
        current++;
    }
%>

<!-- ================================================== -->

<!doctype html>
<html>
    <head>
	<title><%=messages.getString("bireme_social_checklinks")%></title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="HandheldFriendly" content="true">
	<link href="css/bootstrap.css" rel="stylesheet">
	<link href="css/bootstrap-responsive.css" rel="stylesheet">
	<link href="css/styles.css" rel="stylesheet">
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
        
        function confirmUndo(url) {
            if (confirm('<%=messages.getString("undo_confirm")%>')) {
                 postToUrl('<%=response.encodeRedirectURL("UndoFixServlet")%>', 
                 {undoUrl:url, group:'<%=group%>', lgroup:'<%=lgroup%>', 
                  lang:'<%=lang%>', id:'<%=id%>', brokenUrl:'<%=brokenUrl%>',
                  url:'<%=url%>'});
            } else {
                // Do nothing!
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
                        <a class="brand" href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>'});"><%=messages.getString("bireme_social_checklinks")%></a>
                        <div class="nav-collapse collapse">
                            <ul class="nav">
                                <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0', lang:'<%=lang%>'});"><%=messages.getString("home")%></a></li>
                                <li><a href="http://wiki.bireme.org/pt/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
                                <li><a href="http://feedback.bireme.org/feedback/?application=socialchecklinks&version=<%=BrokenLinks.VERSION%>&lang=<%=lang%>" target="_blank"><%=messages.getString("contact")%></a></li>
                            </ul>
                            <ul class="nav pull-right">
                                <li class="dropdown">
                                    <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                    <ul class="dropdown-menu">
                                        <li <%if(lang.equals("en")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'<%=group%>',lgroup:'<%=lgroup%>',lang:'en',id:'<%=id%>',brokenUrl:'<%=brokenUrl%>',url:'<%=url%>'});">English</a></li>
                                        <li <%if(lang.equals("pt")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'<%=group%>',lgroup:'<%=lgroup%>',lang:'pt',id:'<%=id%>',brokenUrl:'<%=brokenUrl%>',url:'<%=url%>'});">Português</a></li>
                                        <li <%if(lang.equals("es")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'<%=group%>',lgroup:'<%=lgroup%>',lang:'es',id:'<%=id%>',brokenUrl:'<%=brokenUrl%>',url:'<%=url%>'});">Español</a></li>
                                        <!--li <%if(lang.equals("fr")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'<%=group%>',lgroup:'<%=lgroup%>',lang:'fr',id:'<%=id%>',brokenUrl:'<%=brokenUrl%>',url:'<%=url%>'});">Francés</a></li-->
                                    </ul>
                                </li>
                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user%> <b class="caret"></b></a>
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
                <div class="breadcrumb"
                    <ul class="breadcrumb">
                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=lgroup%>',lang:'<%=lang%>'});"><%=messages.getString("list")%></a> <span class="divider">/</span></li>
                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("CheckOneLinkServlet")%>', {id:'<%=id%>',url:'<%=url%>',furl:'<%=url%>',lang:'<%=lang%>',group:'<%=lgroup%>'});"><%=messages.getString("edit")%></a> <span class="divider">/</span></li>
                        <li class="active"><%=messages.getString("show_changed")%></li>
                    </ul>     
                </div>
                <h1><%=messages.getString("url_changes")%></h1>
                <div class="urlEditor">
                    <div class="urlLine">
                        <div class="seg-q">
                            <div class="URL-tested">ID: <a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id2%>"><%=id2%></a></div>
                            <div class="URL-tested">URL: <a target="_blank" href="<%=brokenUrl%>"><%=brokenUrl%></a>  &#8594; <a target="_blank" href="<%=url%>"><%=url%></a></div>
                        </div>
                    </div>
                    <p><%=fixedUrls%> <%=messages.getString("urls_were_affected")%></p>
                    <% if (fixedUrls > 0) { %>
                        <table class="table table-condensed">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th><%=messages.getString("database")%></th>
                                    <th>ID</th>
                                    <th>URL</th>                                                
                                    <th>CC</th>
                                    <th><%=messages.getString("since")%></th>
                                    <th><%=messages.getString("action")%></th>                                                
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                int cur = from + 1;
                                for (IdUrl iu : fixedX) {
                                    final String nurl = iu.url.replace("&","<<amp;>>");
                                    final String xid = iu.id.substring(0,iu.id.indexOf("_"));
                                    boolean first = true;                                                                                       
                                %>
                                    <tr>                                    
                                        <td><%=cur%></td>
                                        <td><%=iu.mst%></td>
                                        <td><a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=xid%>"><%=xid%></a></td>
                                        <td><a target="_blank" href="<%=iu.url%>"><%=iu.url.trim()%></a></td>  
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
                                        <td><a href="javascript:confirmUndo('<%=nurl%>');" title="<%=messages.getString("undo_last_url")%>" class="btn btn-mini btn-primary"><%=messages.getString("undo")%></a></td>
                                        <!--td><a href="http://www.bireme.br" title="<%=messages.getString("undo_last_url")%>" class="btn btn-mini btn-primary"><%=messages.getString("undo")%></a></td-->
                                    </tr>
                                <%
                                    cur++;
                                }
                                %>
                            </tbody>
                        </table>                
                    <% } %>
                    <div class="accordion">
                        <div class="pagination pagination-centered">
                            <ul>
                                <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'0','lang:'<%=lang%>'});">&laquo;</a></li>
                                <%                                        
                                for (int idx = initGroup; idx < initGroup+5; idx++) {
                                    if (idx == group) {
                                %>
                                        <li class="active"><a><%=idx+1%></a></li>
                                <%
                                    } else if (idx <= lastGroup) {
                                %>
                                        <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'<%=idx%>','lang:'<%=lang%>'});" ><%=idx+1%></a></li>
                                <%
                                    }
                                }    
                                %>
                                <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("showFixedUrls.jsp")%>', {group:'<%=lastGroup%>','lang:'<%=lang%>'});">&raquo;</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div> <!-- /container -->
            <div id="push"></div>
	</div>
	<footer id="footer">
            <div class="container">
                <strong><%=messages.getString("bireme_social_checklinks")%> - V<%= BrokenLinks.VERSION %> - 2013</strong><br/>
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
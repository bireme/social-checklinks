<%--

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

--%>

<%-- 
    Document   : Report
    Created on : 08/04/2015, 14:45:40
    Author     : Heitor Barbieri
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page session="true" %>
<%@page import="java.util.*,java.text.*,com.mongodb.DBCollection,br.bireme.scl.*,br.bireme.scl.MongoOperations" %>

<%
    final String CODEC = "UTF-8";    
    request.setCharacterEncoding(CODEC);
    
    String lang = (String)request.getParameter("lang");
    if (lang == null) {
        lang = "en";
    }
    ResourceBundle messages = Tools.getMessages(lang);
    if (messages == null) {
        lang = "en";
        messages = Tools.getMessages(lang);
    }
    
    final String user = (String)session.getAttribute("user");
    
    if (user == null) {
        response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
        return;
    }
    
    final ServletContext context = getServletContext();
    final DBCollection coll = (DBCollection)context.getAttribute("historycoll");           
    final Set<String> databases = (Set<String>)context.getAttribute("databases");
    final Set<String> centerIds = (Set<String>)session.getAttribute("centerIds");
    
    //--------------- List Broken Links paramenters ----------------------------
    
    String dbFilter = request.getParameter("dbFilter");
    dbFilter = "null".equals(dbFilter) ? null : dbFilter;
    
    String idFilter = request.getParameter("idFilter");
    idFilter = "null".equals(idFilter) ? null : idFilter;
    
    String urlFilter = request.getParameter("urlFilter");
    urlFilter = "null".equals(urlFilter) ? null : urlFilter;
    final String urlFilter_E = (urlFilter == null) ? null
                                  : EncDecUrl.encodeUrl(urlFilter, CODEC, true);
    
    String collCenterFilter = request.getParameter("collCenterFilter");
    collCenterFilter = "null".equals(collCenterFilter) ? null : collCenterFilter;
    
    String sgroup = request.getParameter("group");
    int group = ((sgroup == null ) || "null".equals(sgroup)) ? 0 
                                                     : Integer.parseInt(sgroup);

    String dateFilter = request.getParameter("dateFilter");
    dateFilter = "null".equals(dateFilter) ? null : dateFilter;
    
    String order = request.getParameter("order");
    order = "null".equals(order) ? "descending" : order;

    //--------------- Report Broken Links parameters ---------------------------
    
    String r_dbFilter = request.getParameter("r_dbFilter");
    r_dbFilter = "null".equals(r_dbFilter) ? null : r_dbFilter;
    
    String r_idFilter = request.getParameter("r_idFilter");
    r_idFilter = "null".equals(r_idFilter) ? null : r_idFilter;
    
    String r_urlFilter = request.getParameter("r_urlFilter");
    r_urlFilter = "null".equals(r_urlFilter) ? null : r_urlFilter;
    final String r_urlFilter_E = (r_urlFilter == null) ? null
                                : EncDecUrl.encodeUrl(r_urlFilter, CODEC, true);
    final String r_urlFilter_D = (r_urlFilter == null) ? null
                                             : EncDecUrl.decodeUrl(r_urlFilter);
    
    String r_collCenterFilter = request.getParameter("r_collCenterFilter");
    r_collCenterFilter = "null".equals(r_collCenterFilter) ? null 
                                                           : r_collCenterFilter;
    r_collCenterFilter = (r_collCenterFilter == null) ? null :
                                        r_collCenterFilter.trim().toUpperCase();
    
    String r_sgroup = request.getParameter("r_group");
    int r_group = ((r_sgroup == null) || "null".equals(r_sgroup)) ? 0 
                                                   : Integer.parseInt(r_sgroup);

    String r_dateFilter = request.getParameter("r_dateFilter");
    r_dateFilter = "null".equals(r_dateFilter) ? null : r_dateFilter;
    
    String r_userFilter = request.getParameter("r_userFilter");
    r_userFilter = "null".equals(r_userFilter) ? null : r_userFilter;
        
   //---------------------------------------------------------------------------
   
    final int r_groupSize = 18;        
    
    final List<String> collCenters = new ArrayList<String>();    
    if (r_collCenterFilter == null) {
        collCenters.addAll(centerIds);        
    } else {
        collCenters.add(r_collCenterFilter);
    }
                    
    final Element elem = new Element(r_idFilter, 
                                     null,
                                     null,
                                     r_urlFilter_E, 
                                     r_dbFilter, 
                                     r_dateFilter,
                                     r_userFilter,
                                     collCenters,
                                     true) ;    
    
    final MongoOperations.SearchResult2 sr = MongoOperations.getHistoryDocuments(
                                                    coll,
                                                    elem,
                                                    (r_group * r_groupSize) + 1, 
                                                    r_groupSize);
    final int maxUrls = sr.size;
    
    final int mod = (maxUrls % r_groupSize);
    int lastGroup = (maxUrls / r_groupSize);
    lastGroup = ((maxUrls > 0) && (mod == 0)) ? lastGroup - 1 : lastGroup;
    final int initGroup = (r_group <= 1) ? 0 : (r_group >= lastGroup - 2) 
                                              ? (lastGroup - 4) : (r_group - 2);
    final int from = (r_group * r_groupSize);        
    final boolean showCenters = (centerIds.size() > 1);    
%>    

<!DOCTYPE html>
<html>
    <head>
        <title><%=messages.getString("bireme_social_checklinks")%> - Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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
            
        function postToUrl(path, params, blank) {
            var form = document.createElement("form");
            form.setAttribute("charset", "UTF-8");
            form.setAttribute("method", "post");
            form.setAttribute("action", path);            

            for(var key in params) {
                if (params.hasOwnProperty(key)) {
                    var hiddenField = document.createElement("input");
                    
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", key);
                    hiddenField.setAttribute("value", params[key]);
                    //alert('key=' + key + ' value' + params[key])
                    form.appendChild(hiddenField);
                }
            }

            document.body.appendChild(form);
            
            if (blank) {
                form.setAttribute("target", "_blank");
            }
            form.submit();
        }
        function isNumber(n) {
            return !isNaN(parseFloat(n)) && isFinite(n);
        }
        function gotoPage(elemId) {
            var goto = document.getElementById(elemId).value;
                        
            if (goto && isNumber(goto)) {
                postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:(goto - 1),r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});
            } else {
                postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=lastGroup%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});
            }
        }
        function confirmUndo(undoId) {
            if (confirm('<%=messages.getString("undo_confirm")%>')) {                
                postToUrl('<%=response.encodeRedirectURL("UndoFixReportServlet")%>', {id:undoId, lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=r_group%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});
            } else {
                // Do nothing!
            }
        }
        function confirmUndo2(undoId, database, cc, fixedUrl, brokenUrl) {
            if (confirm('<%=messages.getString("undo_confirm")%>')) {                
                postToUrl('<%=response.encodeRedirectURL("UndoFixFeedbackServlet")%>', {id:undoId, database:database, cc:cc, fixedUrl:fixedUrl, brokenUrl:brokenUrl, lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=r_group%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});
            } else {
                // Do nothing!
            }
        }
        
        </script>        
    </head>
    
    <body style="background-color:#f7faff">
        <div id="wrap">
            <div class="navbar navbar-inverse navbar-fixed-top">
                <div class="navbar-inner">
                    <div class="container">
                        <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="brand" href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("bireme_social_checklinks")%></a>
                        <div class="nav-collapse collapse">
                            <ul class="nav">
                                <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("home")%></a></li>
                                <li><a href="http://wiki.bireme.org/<%=lang%>/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
                                <li><a href="http://feedback.bireme.org/feedback/?application=socialchecklinks&version=<%=BrokenLinks.VERSION%>&lang=<%=lang%>" target="_blank"><%=messages.getString("contact")%></a></li>
                            </ul>
                            <ul class="nav pull-right">
                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                    <ul class="dropdown-menu">
                                        <li <%if(lang.equals("en")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {group:'<%=group%>',lang:'en',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=r_group%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">English</a></li>
                                        <li <%if(lang.equals("pt")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {group:'<%=group%>',lang:'pt',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=r_group%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">Português</a></li>
                                        <li <%if(lang.equals("es")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {group:'<%=group%>',lang:'es',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=r_group%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">Español</a></li>
                                        <!--li <%if(lang.equals("fr")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {group:'<%=group%>',lang:'fr',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=r_group%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">Francés</a></li-->
                                    </ul>
                                </li>
                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user %> <b class="caret"></b></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("index.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><i class="icon-off"></i> <%=messages.getString("logout")%></a></li>
                                    </ul>
                                </li>
                            </ul>
                        </div><!--/.nav-collapse -->
                    </div>
                </div>
            </div>
            <div class="container"> 
                <div class="row" style="margin-bottom: 0px;">
                    <div class="span9">        
                        <div class="breadcrumb">
                            <ul class="breadcrumb">
                                <li class="active"><%=messages.getString("report")%></li>
                            </ul>     
                        </div>
                    </div>                        
                    <div class="span3">
                        <div class="breadcrumb" style="margin-bottom: 0px;">
                            <ul class="unstyled" style="color:#0088cc;font-size:12px;">
                                <li style="line-height:15px">DB: <%=r_dbFilter==null?messages.getString("all"):r_dbFilter%></li>
                                <li style="line-height:15px">ID: <%=r_idFilter==null?messages.getString("all"):r_idFilter%></li>
                                <li style="line-height:15px">URL: <%=r_urlFilter_D==null?messages.getString("all"):r_urlFilter_D%></li>                           
                                <li style="line-height:15px">CC: <%=r_collCenterFilter==null?messages.getString("all"):r_collCenterFilter%></li>
                                <li style="line-height:15px"><%=messages.getString("since")%>: <%=r_dateFilter==null?messages.getString("all"):r_dateFilter%></li>
                                <li style="line-height:15px"><%=messages.getString("user")%>: <%=r_userFilter==null?messages.getString("all"):r_userFilter%></li>
                            </ul>
                        </div>
                    </div>                        
                </div>                    
                                
                <h1 style="margin-top: 5px;"><%=messages.getString("fixed_links")%></h1>(total=<%=maxUrls%>)
                <p> </p>
                <table class="table table-condensed">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>                                    
                                <div class="nav-collapse">
                                    <ul style="list-style: none; padding: 0px; margin: 0px;">
                                        <li class="dropdown">
                                            <a class="dropdown-toggle" href="#" data-toggle="dropdown">DB<strong class="caret"></strong></a>
                                            <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                    <li style="margin-bottom: 8px;">
                                                        <a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});"><%=messages.getString("all")%></a>                                                          
                                                    </li>                                                            
                                                    <li style="margin-bottom: 8px;">
                                                        <% for (String db : databases) { %>
                                                            <a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=db%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=db%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});"><%=db%></a>
                                                        <% } %>
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
                                                    <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});"><%=messages.getString("all")%></a></li>
                                                    <li>
                                                        <form action='<%=response.encodeRedirectURL("report.jsp?lang=" + lang + "&group=0&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&urlFilter=" + urlFilter_E + "&collCenterFilter=" + collCenterFilter + "&order=" + order + "&r_dbFilter=" + r_dbFilter + "&r_urlFilter=" + r_urlFilter_E + "&r_collCenterFilter=" + r_collCenterFilter + "&r_group=0" + "&r_dateFilter=" + r_dateFilter + "&r_userFilter=" + r_userFilter)%>' method="post" >
                                                            <input name="r_idFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_id")%>" />
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
                                                    <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});"><%=messages.getString("all")%></a></li>
                                                    <li>
                                                        <form action='<%=response.encodeRedirectURL("report.jsp?lang=" + lang + "&group=" + group + "&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&urFilter=" + urlFilter_E + "&collCenterFilter=" + collCenterFilter + "&order=" + order + "&r_dbFilter=" + r_dbFilter + "&r_idFilter=" + r_idFilter + "&r_collCenterFilter=" +  r_collCenterFilter + "&r_group=0" + "&r_dateFilter=" + r_dateFilter + "&r_userFilter=" + r_userFilter)%>' method="post" >
                                                            <input name="r_urlFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_url")%>" />
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
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});"><%=messages.getString("all")%></a></li>
                                                        <li>
                                                            <form action='<%=response.encodeRedirectURL("report.jsp?lang=" + lang + "&group=" + group + "&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&urlFilter=" + urlFilter_E + "&collCenterFilter=" + collCenterFilter + "&order=" + order + "&r_dbFilter=" + r_dbFilter + "&r_idFilter=" + r_idFilter + "&r_urlFilter=" + r_urlFilter_E + "&r_group=0" + "&r_dateFilter=" + r_dateFilter + "&r_userFilter=" + r_userFilter)%>' method="post" >
                                                                <input name="r_collCenterFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_cc")%>" />
                                                            </form>
                                                        </li>
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
                                                    <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_userFilter:'<%=r_userFilter%>'});"><%=messages.getString("all")%></a></li>
                                                    <li>
                                                        <form action='<%=response.encodeRedirectURL("report.jsp?lang=" + lang + "&group=" + group + "&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&collCenterFilter=" + collCenterFilter + "&order=" + order + "&r_dbFilter=" + r_dbFilter + "&r_idFilter=" + r_idFilter + "&r_urlFilter=" + r_urlFilter_E + "&r_collCenterFilter=" +  r_collCenterFilter + "&r_group=0" + "&r_userFilter=" + r_userFilter)%>' method="post" >
                                                            <input name="r_dateFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_date")%>" />
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
                                            <a class="dropdown-toggle" href="#" data-toggle="dropdown"><%=messages.getString("user")%><strong class="caret"></strong></a>
                                            <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                    <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>'});"><%=messages.getString("all")%></a></li>
                                                    <li>
                                                        <form action='<%=response.encodeRedirectURL("report.jsp?lang=" + lang + "&group=" + group + "&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&urlFilter=" + urlFilter_E + "&collCenterFilter=" + collCenterFilter + "&order=" + order + "&r_dbFilter=" + r_dbFilter + "&r_idFilter=" + r_idFilter + "&r_urlFilter=" + r_urlFilter_E + "&r_collCenterFilter=" +  r_collCenterFilter + "&r_group=0" + "&r_dateFilter=" + r_dateFilter)%>' method="post" >
                                                            <input name="r_userFilter" type="text" style="margin-bottom: 15px;" placeholder="<%=messages.getString("search_user")%>" />
                                                        </form>
                                                    </li>
                                                </ul>
                                            </div>
                                        </li>
                                    </ul>
                                </div>                                        
                            </th>                                                
                            <th><%=messages.getString("exported")%></th>
                            <th><%=messages.getString("actions")%></th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                        int cur = from + 1;
                        for (Element xelem : sr.documents) {
                            final String furl = xelem.getFurl();
                            final String furl_D = EncDecUrl.decodeUrl(furl);
                            final String burl = xelem.getBurl();
                            final String burl_D = EncDecUrl.decodeUrl(burl);
                            final String id = xelem.getId()
                                       .substring(0, xelem.getId().indexOf('_'));
                        %>                        
                            <tr>                                    
                                <td><%=cur%></td>
                                <td style="font-size: 13px;"><%=xelem.getDbase()%></td>
                                <td><a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id%>"><%=id%></a></td>                                    
                                <td><a target="_blank" href="<%=furl%>" 
                                       title="<%=burl_D%> &#8594; <%=furl_D%>">
                                       <%= 
                                     (furl_D.length() > 60) ? furl_D.substring(0,60) + " ..."
                                                          : furl_D
                                     %>
                                    </a>
                                </td>                                  
                                <td style="font-size: 13px;">
                                <%
                                boolean first = true;
                                final List<String> ccs=xelem.getCcs();
                                for (String cc : ccs) {
                                    if (first) {
                                        first = false;
                                    } else {
                                        out.print(", ");
                                    }
                                    out.print(cc);
                                }
                                %>             
                                </td>
                                <td style="font-size: 13px;"><%=xelem.getDate()%></td>    
                                <td style="font-size: 13px;"><%=xelem.getUser()%></td>
                                <td style="text-align:center"><%=(xelem.isExported() ? "&Chi;" : "")%></td>                                
                                <td>   
                                    <%
                                    if (xelem.isExported()) {
                                    %>
                                        <!--a href="javascript:confirmUndo('<%=xelem.getId()%>', '<%=xelem.getDbase()%>', '<%=xelem.getCcs().get(0)%>', '<%=xelem.getFurl()%>', '<%=xelem.getBurl()%>');" title="<%=messages.getString("undo_last_url")%>" class="btn btn-primary btn-mini pull-right "><%=messages.getString("undo")%></a-->
                                    <%                                        
                                    } else {    
                                    %>
                                        <a href="javascript:confirmUndo('<%=xelem.getId()%>');" title="<%=messages.getString("undo_last_url")%>" class="btn btn-primary btn-mini pull-right"><%=messages.getString("undo")%></a>
                                    <%    
                                    }
                                    %>
                                </td>
                            </tr>
                        <%
                            cur++;
                        }
                        %>
                    </tbody>
                </table> 
                <%
                if (maxUrls > r_groupSize) {        
                %>    
                    <div class="pagination pagination-centered">                               
                        <ul>
                            <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'0',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">&LeftArrowBar;</a></li>
                            <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%= (r_group > 0 ? r_group -1 : 0)%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">&LeftArrow;</a></li>
                            <%                                        
                            for (int idx = initGroup; idx < initGroup+5; idx++) {
                                if (idx == r_group) {
                            %>
                                    <li class="active"><a><%=idx+1%></a></li>
                            <%
                                } else if (idx <= lastGroup) {
                            %>
                                    <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>',{lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=idx%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});"><%=idx+1%></a></li>
                            <%
                                }
                            }    
                            %>
                            <li><input class="gotoPage" id="gotoPage" type="text" placeholder='<%=messages.getString("goto_page")%>' value="" onkeydown="if (event.keyCode == 13) gotoPage('gotoPage')"  /></li>
                            <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',r_dbFilter:'<%=r_dbFilter%>',r_idFilter:'<%=r_idFilter%>',r_urlFilter:'<%=r_urlFilter_E%>',r_collCenterFilter:'<%=r_collCenterFilter%>',r_group:'<%=(r_group < lastGroup ? r_group + 1 : lastGroup)%>',r_dateFilter:'<%=r_dateFilter%>',r_userFilter:'<%=r_userFilter%>'});">&RightArrow;</a></li>
                            <li class="enabled"><a href="javascript:gotoPage('gotoPage');">&RightArrowBar;</a></li>
                        </ul>
                    </div>   
                <%
                }
                %>    
            </div>
        </div>
        
        <footer id="footer" class="footer">
            <div class="container">
                <strong><%=messages.getString("bireme_social_checklinks")%> - V<%= BrokenLinks.VERSION %> - <%=BrokenLinks.VERSION_DATE%></strong><br/>
                <%=messages.getString("source_code")%>: <a href="https://github.com/bireme/social-checklinks">https://github.com/bireme/social-checklinks</a>
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

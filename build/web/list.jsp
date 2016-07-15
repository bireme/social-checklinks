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

<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.*,com.mongodb.DBCollection,br.bireme.scl.*,br.bireme.scl.MongoOperations" %>
<%@page contentType="text/html;charset=UTF-8"%>

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
    final boolean readOnlyMode = (Boolean)context.getAttribute("readOnlyMode");
    final DBCollection coll = (DBCollection)context.getAttribute("collection");           
    final Set<String> databases = (Set<String>)context.getAttribute("databases");
    final String cc = (String)session.getAttribute("cc");
    
    // If is not BR1.1 get centers from Bireme Accounts else get from Mongo 
    // because we want all centers that can change anytime (dynamic).
    final Set<String> centerIds = (cc.equals("BR1.1")) 
        ? new HashSet<String>(MongoOperations.getCenters(coll))
        : (Set<String>)session.getAttribute("centerIds");
        
    String dbFilter = request.getParameter("dbFilter");
    dbFilter = "null".equals(dbFilter) ? null : (dbFilter == null) ? null 
                                                               :dbFilter.trim();
    
    String idFilter = request.getParameter("idFilter");
    idFilter = "null".equals(idFilter) ? null : (idFilter == null) ? null 
                                                               :idFilter.trim();
    
    String urlFilter = request.getParameter("urlFilter");
    urlFilter = "null".equals(urlFilter) ? null : (urlFilter == null) ? null 
                                                             : urlFilter.trim();
    final String urlFilter_E1 = (urlFilter == null) ? null : 
                                    EncDecUrl.encodeUrl(urlFilter, CODEC, true);
    final String urlFilter_E2 = (urlFilter == null) ? null : 
                                    EncDecUrl.encodeUrl(urlFilter, CODEC, false);
    final String urlFilter_D = (urlFilter == null) ? null : 
                                          EncDecUrl.decodeUrl(urlFilter);
    
    String collCenterFilter = request.getParameter("collCenterFilter");
    collCenterFilter = "null".equals(collCenterFilter) ? null 
                       : (collCenterFilter == null) ? null 
                                        : collCenterFilter.trim().toUpperCase();
    
    String sgroup = request.getParameter("group");
    int group = ((sgroup == null) || "null".equals(sgroup)) ? 0 
                                                     : Integer.parseInt(sgroup);

    String order = request.getParameter("order");
    order = "null".equals(order) ? "descending" : (order == null) ? null
                                                                 : order.trim();
    
    final int groupSize = 18;
    
    final Set<String> collCenterSet;
    if (collCenterFilter == null) {
        collCenterSet = cc.equals("BR1.1") ? null : centerIds;
    } else {
        collCenterSet = new HashSet<String>();
        collCenterSet.add(collCenterFilter);
    }
                   
    final MongoOperations.SearchResult sr = MongoOperations.getDocuments(
                                                    coll, 
                                                    dbFilter, 
                                                    idFilter, 
                                                    urlFilter_E2, 
                                                    collCenterSet, 
                                                    "descending".equals(order), 
                                                    (group * groupSize) + 1, 
                                                    groupSize);
    final List<IdUrl> lst = sr.documents;
    final int maxUrls = sr.size;
                                           
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
            
        function postToUrl(path, params, blank) {
            var form = document.createElement("form");
            form.setAttribute("charset", "UTF-8");
            form.setAttribute("method", "post");
            form.setAttribute("action", path); 
            form.setAttribute("enctype", "application/x-www-form-urlencoded");
            form.setAttribute("encoding", "application/x-www-form-urlencoded");

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
                postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:(goto - 1),lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});
            } else {
                postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=lastGroup%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});
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
                            <a class="brand" href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("bireme_social_checklinks")%></a>
                            <div class="nav-collapse collapse">
                                <ul class="nav">
                                    <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("home")%></a></li>
                                    <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("report")%></a></li>
                                    <li><a href="http://wiki.bireme.org/<%=lang%>/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
                                    <li><a href="http://feedback.bireme.org/feedback/?application=socialchecklinks&version=<%=BrokenLinks.VERSION%>&lang=<%=lang%>" target="_blank"><%=messages.getString("contact")%></a></li>
                                </ul>
                                <ul class="nav pull-right">
                                    <li class="dropdown">
                                        <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                        <ul class="dropdown-menu">
                                            <li <%if(lang.equals("en")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'en',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">English</a></li>
                                            <li <%if(lang.equals("pt")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'pt',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">Português</a></li>
                                            <li <%if(lang.equals("es")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'es',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">Español</a></li>
                                            <!--li<%if(lang.equals("fr")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'fr',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">Francés</a></li-->
                                        </ul>
                                    </li>
                                    <li class="dropdown">
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user %> <b class="caret"></b></a>
                                        <ul class="dropdown-menu">
                                            <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("index.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><i class="icon-off"></i> <%=messages.getString("logout")%></a></li>
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
                                    <li class="active"><%=messages.getString("list")%></li>
                                </ul>     
                            </div>
                        </div>                        
                        <div class="span3">
                            <div class="breadcrumb" style="margin-bottom: 0px;">
                                <ul class="unstyled" style="color:#0088cc;font-size:12px;">
                                    <li style="line-height:15px">DB: <%=dbFilter==null?messages.getString("all"):dbFilter%></li>
                                    <li style="line-height:15px">ID: <%=idFilter==null?messages.getString("all"):idFilter%></li>
                                    <li style="line-height:15px">URL: <%=urlFilter==null?messages.getString("all"):urlFilter_D%></li>                           
                                    <li style="line-height:15px">CC: <%=collCenterFilter==null?messages.getString("all"):collCenterFilter%></li>
                                    <li style="line-height:15px"><%=messages.getString("since")%>: <%=order.equals("ascending") ? messages.getString("ascending_order") : messages.getString("descending_order")%></li>
                                </ul>
                            </div>
                        </div>                        
                    </div>                    
                        
                    <h1 style="margin-top: 5px;"><%=messages.getString("broken_links")%> </h1>(total=<%=maxUrls%>)
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
                                                <a class="dropdown-toggle" href="#" data-toggle="dropdown">DB<strong class="caret"></strong></a>
                                                <div class="dropdown-menu" style="padding: 15px; padding-bottom: 0px;">
                                                    <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                        <li style="margin-bottom: 8px;">
                                                            <a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>',group:'0',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("all")%></a>                                                          
                                                        </li>                                                            
                                                        <li style="margin-bottom: 8px;">
                                                            <% for (String db : databases) { %>
                                                                <a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>',group:'0',dbFilter:'<%=db%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=db%></a>
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
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>',group:'0',dbFilter:'<%=dbFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("all")%></a></li>
                                                        <li>
                                                            <form action='<%=response.encodeRedirectURL("list.jsp?lang="+ lang + "&group=0&dbFilter=" + dbFilter + "&urlFilter=" + urlFilter_E1 + "&collCenterFilter=" + collCenterFilter + "&order=" + order)%>' method="post" >
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
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {lang:'<%=lang%>',group:'0',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("all")%></a></li>
                                                        <li>
                                                            <form action='<%=response.encodeRedirectURL("list.jsp?lang=" + lang + "&group=0&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&collCenterFilter=" + collCenterFilter + "&order=" + order)%>' method="post" >
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
                                                    <div  class="dropdown-menu scrollable-menu" role="menu"  style="padding: 15px; padding-bottom: 0px;height: auto;max-height: 500px;overflow-x: hidden;">
                                                        <ul style="list-style: none; padding: 0px; margin: 0px;">
                                                            <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',order:'<%=order%>'});"><%=messages.getString("all")%></a></li>
                                                            <li>
                                                                <form action='<%=response.encodeRedirectURL("list.jsp?lang=" + lang + "&group=" + group + "&dbFilter=" + dbFilter + "&idFilter=" + idFilter + "&order=" + order)%>' method="post" >
                                                                    <input name="collCenterFilter" type="text" style="margin-bottom: 0px; max-width: 120px;" placeholder="<%=messages.getString("search_cc")%>" />
                                                                </form>
                                                            </li>
                                                            
                                                            <li class="divider"></li>
                                                                                                                
                                                            <%
                                                            for (String id : centerIds)  {                               
                                                            %>    
                                                                <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=id%>',order:'<%=order%>'});"><%=id%></a></li>
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
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'ascending'});"><%=messages.getString("ascending_order")%></a></li>
                                                        <li style="margin-bottom: 8px;"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'descending'});"><%=messages.getString("descending_order")%></a></li>
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
                                //final String nurl = iu.url.trim().replace("%20", " ");
                                final String nurl = EncDecUrl.decodeUrl(iu.url);
                                final String id = iu.id.substring(0,iu.id.indexOf("_"));
                                boolean first = true;                                                                                    
                            %>
                                <tr>                                    
                                    <td><%=cur%></td>
                                    <td style="font-size: 13px;"><%=iu.mst%></td>
                                    <td><a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id%>"><%=id%></a></td>                                    
                                    <td><a target="_blank" href="<%=iu.url%>" title="<%=nurl%>"><%=Tools.limitString(nurl,80)%></a></td>  
                                    <td style="font-size: 13px;">
                                    <%
                                    for (String ccx : iu.ccs) {
                                        if (first) {
                                            first = false;
                                        } else {
                                            out.print(", ");
                                        }
                                        out.print(ccx);
                                    }
                                    %>             
                                    </td>
                                    <td style="font-size: 13px;"><%=iu.since%></td>    
                                    <td>
                                        <% if(!readOnlyMode) { %>
                                            <a href="javascript:postToUrl('<%=response.encodeRedirectURL("CheckOneLinkServlet")%>', {id:'<%=iu.id%>',url:'<%=nurl%>',furl:'<%=nurl%>',lang:'<%=lang%>',group:'<%=group%>',dbFilter:'<%=dbFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>',new:'1'});" title="<%=messages.getString("edit_broken_url")%>" class="btn btn-mini btn-primary"> &nbsp;<%=messages.getString("edit")%>&nbsp;</a>&nbsp;&nbsp;
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
                    <%
                    if (maxUrls > groupSize) {        
                    %>    
                        <div class="pagination pagination-centered">                               
                            <ul>
                                <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">&LeftArrowBar;</a></li>
                                <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%= (group > 0 ? group -1 : 0)%>',lang:'<%=lang%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">&LeftArrow;</a></li>
                                <!--li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">&laquo;</a></li-->
                                        <%                                        
                                for (int idx = initGroup; idx < initGroup+5; idx++) {
                                    if (idx == group) {
                                %>
                                        <li class="active"><a><%=idx+1%></a></li>
                                <%
                                    } else if (idx <= lastGroup) {
                                %>
                                        <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=idx%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=idx+1%></a></li>
                                <%
                                    }
                                }    
                                %>
                                <li><input class="gotoPage" id="gotoPage" type="text" placeholder='<%=messages.getString("goto_page")%>' value="" onkeydown="if (event.keyCode == 13) gotoPage('gotoPage')"  /></li>
                                <li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=(group < lastGroup ? group + 1 : lastGroup)%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">&RightArrow;</a></li>
                                <li class="enabled"><a href="javascript:gotoPage('gotoPage');">&RightArrowBar;</a></li>
                                <!--li class="enabled"><a href="javascript:gotoPage('gotoPage');">&raquo;</a></li-->
                                <!--li class="enabled"><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=lastGroup%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',idFilter:'<%=idFilter%>',urlFilter:'<%=urlFilter_E1%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});">&raquo;</a></li-->
                            </ul>
                        </div>   
                    <%
                    }
                    %>
                </div> <!-- /container -->
            <!--div id="push"></div-->
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
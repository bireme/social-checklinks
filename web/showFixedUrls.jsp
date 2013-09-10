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
    final String lang = (String)request.getParameter("lang");
    final ResourceBundle messages = Tools.getMessages(lang);
    final String user = (String)session.getAttribute("user");
    
    if (user == null) {
        response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
        return;
    }

    final String url = (String)session.getAttribute("url");
    final Set<IdUrl> fixed = (Set<IdUrl>)session.getAttribute("IdUrls");
    final int fixedUrls = fixed.size();
    final int group = Integer.parseInt(request.getParameter("group"));
    final int groupSize = 17;
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
			height: 100%; /* The html and body elements cannot have any padding or margin. */
                        padding-top: 30px;
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
					<a class="brand" href="#"><%=messages.getString("bireme_social_checklinks")%></a>
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li class="active"><a href="javascript:postToUrl('list.jsp', {group:'0', lang:'<%=lang%>',cc:'All'});"><%=messages.getString("home")%></a></li>
							<li><a href="http://wiki.bireme.org/pt/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
							<li><a href="http://reddes.bvsalud.org/" target="_blank"><%=messages.getString("contact")%></a></li>
						</ul>
						<ul class="nav pull-right">
                                                        <li class="dropdown">
                                                            <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                                            <ul class="dropdown-menu">
                                                                <li><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'<%=group%>',lang:'en'});">English</a></li>
                                                                <li><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'<%=group%>',lang:'pt'});">Português</a></li>
                                                                <li><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'<%=group%>',lang:'es'});">Español</a></li>
                                                                <!--li><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'<%=group%>',lang:'fr'});">Francés</a></li-->
                                                            </ul>
                                                        </li>
							<li class="dropdown">
								<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user%> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="javascript:postToUrl('index.jsp', {lang:'<%=lang%>'});"><i class="icon-off"></i> <%=messages.getString("logout")%></a></li>
								</ul>
							</li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>

		<div class="container">
			<h1><%=messages.getString("url_changes")%></h1>
			<div class="urlEditor">
				<div class="urlLine">
					<div class="seg-q">
						<div class="URL-tested"><%=url%></div>
						
					</div>
				</div>
				<div class="accordion">
					<div class="accordion-heading">
						<a href="#URL-list" class="accordion-toggle" data-toggle="collapse"><i class="icon-list"></i><%=fixedUrls%> <%=messages.getString("urls_were_affected")%></a>
					</div>
					<div id="URL-list" class="accordion-body <%=(group==0) ? "collapse out" : ""%>">
						<table class="table table-condensed">
							<thead>
								<tr>
									<th>#</th>
									<th>URL</th>
                                                                        <th><%=messages.getString("actions")%></th>
								</tr>
							</thead>
							<tbody>
                                                            <%
                                                                int idxx = group * groupSize;                                                                
                                                                for (IdUrl idUrl : fixedX) {
                                                                    final String nurl = idUrl.url.replace("&","<<amp;>>");
                                                                    final String id2 = idUrl.id.substring(0, idUrl.id.lastIndexOf('_'));
                                                                    final String lang2 = lang.equals("null") ? "en" : lang.equals("fr") ? "en" : lang;
                                                            %>
                                                                <tr>
									<td><%=++idxx%></td>
									<td><a href="<%=nurl%>" title="<%=messages.getString("view_document")%>" target="_blank"><%=idUrl.url%></a></td>
                                                                        <!--<td><a href="http://pesquisa.bvsalud.org/regional/?lang=<%=lang2%>&q=++%28id%3A%28LIL-<%=id2%>%29%29" title="<%=messages.getString("see_bibliographic_record")%>" target="_blank" class="btn btn-mini btn-primary"><i class="icon-eye-open icon-white"></i> <%=messages.getString("see")%></a>&nbsp;-->
                                                                            <td><a href="http://pesquisa.bvsalud.org/regional/?lang=<%=lang2%>&q=++(id:(LIL-<%=id2%>))" title="<%=messages.getString("see_bibliographic_record")%>" target="_blank" class="btn btn-mini btn-primary"><i class="icon-eye-open icon-white"></i> <%=messages.getString("see")%></a>&nbsp;
                                                                            <a href="javascript:postToUrl('UndoFixServlet', {undoUrl:'<%=nurl%>', lang:'<%=lang%>'});" title="<%=messages.getString("undo_last_url")%>" class="btn btn-mini btn-primary"><i class="icon-repeat icon-white"></i> <%=messages.getString("undo")%></a></td>
								</tr>
                                                            <%    
                                                                }
                                                            %>
							</tbody>
						</table>
						<div class="pagination pagination-centered">
                                                    <ul>
                                                        <li class="enabled"><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'0','lang:'<%=lang%>'});">&laquo;</a></li>
                                                        <%                                        
                                                        for (int idx = initGroup; idx < initGroup+5; idx++) {
                                                            if (idx == group) {
                                                        %>
                                                        <li class="active"><a><%=idx+1%></a></li>
                                                        <%
                                                            } else if (idx <= lastGroup) {
                                                        %>
                                                        <li class="enabled"><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'<%=idx%>','lang:'<%=lang%>'});" ><%=idx+1%></a></li>
                                                        <%
                                                            }
                                                        }    
                                                        %>
                                                        <li class="enabled"><a href="javascript:postToUrl('showFixedUrls.jsp', {group:'<%=lastGroup%>','lang:'<%=lang%>'});">&raquo;</a></li>
                                                    </ul>
						</div>
					</div>
				</div>
			</div>
		</div> <!-- /container -->
		<div id="push"></div>
	</div>
	<footer id="footer">
		<div class="container">
			<strong><%=messages.getString("bireme_social_checklinks")%> - <%= BrokenLinks.VERSION %> - 2013</strong><br/>
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
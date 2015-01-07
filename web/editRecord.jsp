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
<%@page import="java.util.*,java.net.*,com.mongodb.DBCollection,br.bireme.scl.*,br.bireme.scl.MongoOperations" %>
<%@page contentType="text/html;charset=UTF-8"%>

<%     
    final String CODEC = "UTF-8";
    request.setCharacterEncoding(CODEC);
    
    String lang = (String)request.getParameter("lang");
    if (lang == null) {
        lang = "en";
    }
    final ResourceBundle messages = Tools.getMessages(lang);
    
    if (session.getAttribute("user") == null) {
        response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
        return;
    }
    
    // status = -1 (new), 1 (broken) and 0 (not broken) 
    final int status = Integer.parseInt(request.getParameter("status"));    
    final boolean isNew = (status == -1);
    final boolean isBroken = (status == 1);    
    final String user = (String)session.getAttribute("user");
    final String id = (String)request.getParameter("id");
    final String id2 = id.substring(0, id.lastIndexOf('_'));
    final String url = (String)request.getParameter("url");
    final String group = (String)request.getParameter("group");
    
    final String furl = (String)request.getParameter("furl");
    final String lang2 = lang.equals("null") ? "en" : lang.equals("fr") 
                                                    ? "en" :lang;
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
            form.setAttribute("charset", "UTF-8");
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
            
        function replaceAll(string, token, newtoken) {
            while (string.indexOf(token) !== -1) {
 		string = string.replace(token, newtoken);
            }
            return string;
        }               
        function callUrl(id, url, lang) {
           //var nurl = encodeURIComponent(document.getElementById('input-1').value);
           var nurl = document.getElementById('input-1').value;
           var nurl2 = encodeURI(nurl);
           //alert(nurl);
           postToUrl('<%=response.encodeRedirectURL("CheckOneLinkServlet")%>', 
                 {id: id, url: url, furl: nurl, lang: lang, group: <%=group%>});                                                        
                 
        }
        function callUrl2(id, url, lang) {
            var nurl = document.getElementById('input-1').value;
            //var nurl = encodeURIComponent(document.getElementById('input-1').value);
            //alert(nurl2);
            postToUrl('<%=response.encodeRedirectURL("CheckManyLinksServlet")%>',
            {id: id, url: url, furl: nurl, lang: lang, group: <%=group%>});
        }
        
        function isVisible(elem) {
            return elem.offsetWidth > 0 || elem.offsetHeight > 0;
        }
        function hideSave() {
            var danger = document.getElementById("alert-danger");            
           
            if (danger && isVisible(danger)) {                
                danger.style.display = 'none';
            } else {
                document.getElementById("alert-success").style.display = 'none';
                document.getElementById("save").style.display = 'none';
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
                                        <li <%if(lang.equals("en")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'en',group:'<%=group%>'});">English</a></li>
                                        <li <%if(lang.equals("pt")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'pt',group:'<%=group%>'});">Português</a></li>
                                        <li <%if(lang.equals("es")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'es',group:'<%=group%>'});">Español</a></li>
                                        <!--li <%if(lang.equals("fr")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'fr',group:'<%=group%>'});">Francés</a></li-->
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
                <div class="breadcrumb"
                    <ul class="breadcrumb">
                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>'});"><%=messages.getString("list")%></a> <span class="divider">/</span></li>
                        <li class="active"><%=messages.getString("edit")%></li>
                    </ul>     
                </div>
                <h1><%=messages.getString("edit_url")%></h1>
                <div class="urlEditor">
                    <div class="urlLine">
                        <div class="seg-q">
                            <div class="URL-tested">ID: <a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id2%>"><%=id2%></a></div>
                            <div class="URL-tested">URL: <a target="_blank" href="<%=url%>"><%=url%></a> &#8594; ?</div>
                            <div class="URL-tested2">
                                <input type="url" id="input-1" class="span8" onfocus="hideSave()" value="<%=furl%>"/> &nbsp;
                                <a href="javascript:callUrl('<%=id%>','<%=url%>','<%=lang%>');" class="btn btn-primary" title="Test your changes"><%=messages.getString("test")%></a>
                            </div>

                            <!--a href="http://pesquisa.bvsalud.org/regional/?lang=<%=lang2%>&q=++%28id%3A%28LIL-<%=id2%>%29%29" title="<%=messages.getString("see_bibliographic_record")%>" target="_blank" class="btn btn-mini btn-primary"><i class="icon-eye-open icon-white"></i> <%=messages.getString("see")%></a-->
                            <%
                            if (isBroken) {
                            %>
                                <div id="alert-danger" class="alert alert-danger fade in">
                                    <!--button data-dismiss="alert" class="close" type="button">×</button-->
                                    <strong><%=messages.getString("bad_news")%></strong> <%=messages.getString("url_is_broken")%>
                                </div>
                            <%
                            } else {
                            %>
                                <div id="alert-success" class="alert alert-success fade in">
                                    <!--button data-dismiss="alert" class="close" type="button">×</button-->
                                    <strong><%=messages.getString("url_fixed")%></strong> <%=messages.getString("press_save")%>
                                </div>
                            <%
                            }
                            %>   

                            <div class="ctrl">
                                <%
                                if (isNew || isBroken) {
                                } else {
                                %>       
                                    <a id="save" href="javascript:callUrl2('<%=id%>','<%=url%>','<%=lang%>');" class="btn btn-primary enabled"><%=messages.getString("save")%></a>
                                <%       
                                }
                                %>                                                           							
                            </div>
                        </div>          
                    </div>				
                </div>
            </div> <!-- /container -->
            <div id="push"></div>
	</div>

        <footer id="footer" class="footer">
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

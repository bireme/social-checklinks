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
    final ResourceBundle messages = Tools.getMessages(lang);
    
    if (session.getAttribute("user") == null) {
        response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
        return;
    }
    
    // status = 1 (broken) and 0 (not broken) 
    final int status = Integer.parseInt(request.getParameter("status"));    
    final boolean isNew = "1".equals((String)request.getParameter("new"));
    final boolean isBroken = (status == 1);    
    final String user = (String)session.getAttribute("user");
    final String id = request.getParameter("id");
    final String id2 = id.substring(0, id.lastIndexOf('_'));
    final String url = request.getParameter("url");
    final String url_D = EncDecUrl.decodeUrl(url);
    final String url_E = EncDecUrl.encodeUrl(url, CODEC, true);
    final String sgroup = request.getParameter("group");
    final String group = "null".equals(sgroup) ? "0" : sgroup;
    final String scollCenterFilter = request.getParameter("collCenterFilter");
    final String collCenterFilter = "null".equals(scollCenterFilter) ? null 
                                                            : scollCenterFilter;    
    final String sorder = request.getParameter("order");
    final String order = (sorder == null) ? "descending" 
                              : ("null".equals(sorder) ? "descending" : sorder);
    final String furl = request.getParameter("furl");
    final String furl_D = EncDecUrl.decodeUrl(furl);
    final String furl_E = EncDecUrl.encodeUrl(furl, CODEC, true);
    final String lang2 = lang.equals("null") ? "en" : lang.equals("fr") 
                                                    ? "en" :lang;    
    final String sdbFilter = request.getParameter("dbFilter");
    final String dbFilter = "null".equals(sdbFilter) ? null : sdbFilter;  
    final String serrCode = request.getParameter("errCode");
    final String errCode = "null".equals(serrCode) ? "0" : serrCode;
    final String serrMsg = request.getParameter("errMsg");
    final String errMsg = "null".equals(serrCode) ? "UNKNOWN" : serrMsg;
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
        
        function postToUrl(path, params, blank) {
            var form = document.createElement("form");
            form.setAttribute("charset", "UTF-8");
            form.setAttribute("method", "post");
            form.setAttribute("action", path);            

            for (var key in params) {
                if ((key !== null) && (params.hasOwnProperty(key))) {
                    var value = params[key];  
                    var hiddenField = document.createElement("input");
                    
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", key);
                    hiddenField.setAttribute("value", value);
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
               
        function replaceAll(string, token, newtoken) {
            while (string.indexOf(token) !== -1) {
 		string = string.replace(token, newtoken);
            }
            return string;
        }               
        
        function callUrl(id, url, lang) {
           var nurl = document.getElementById('input-1').value;
           var nurl2 = replaceAll(nurl, "%20", " ");
           
           //var nurl2 = encodeURI(nurl);
           //var nurl2 = decodeURI(nurl);
           //alert('nurl=' + nurl2);
           
           postToUrl('<%=response.encodeRedirectURL("CheckOneLinkServlet")%>', 
                 {id:id, url:url, furl:nurl2, lang:lang, group:'<%=group%>',
                  dbFilter:'<%=dbFilter%>', collCenterFilter:'<%=collCenterFilter%>', 
                  order:'<%=order%>'});              
        }
        
        function callUrl2(id, url, lang, force) {
            var nurl = document.getElementById('input-1').value;
            var nurl2 = replaceAll(nurl, "%20", " ");
            //var nurl = decodeURIComponent(document.getElementById('input-1').value);

            postToUrl('<%=response.encodeRedirectURL("CheckManyLinksServlet")%>',
                {id: id, url: url, furl: nurl2, lang: lang, group: '<%=group%>',
                dbFilter: '<%=dbFilter%>', collCenterFilter: '<%=collCenterFilter%>',
                order: '<%=order%>', force: force});
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
    
    <body style="background-color:#f7faff">
        <!--%
            if (isNew && !isBroken) {
                response.sendRedirect("CheckManyLinksServlet?id=" + id + "&url=" +
                url_E + "&furl=" + url_E + "&lang=" + lang + "&group=" + group +
                "&dbFilter=" + dbFilter + "&collCenterFilter=" + collCenterFilter +
                "&order=" + order + "&force=force");
            }
        %-->
	<div id="wrap">
            <div class="navbar navbar-inverse navbar-fixed-top">
                <div class="navbar-inner">
                    <div class="container">
                        <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="brand" href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("bireme_social_checklinks")%></a>
                        <div class="nav-collapse collapse">
                            <ul class="nav">
                                <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'0', lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("home")%></a></li>
                                <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("report.jsp")%>', {group:'0',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("report")%></a></li>
                                <li><a href="http://wiki.bireme.org/<%=lang%>/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
                                <li><a href="http://feedback.bireme.org/feedback/?application=socialchecklinks&version=<%=BrokenLinks.VERSION%>&lang=<%=lang%>" target="_blank"><%=messages.getString("contact")%></a></li>
                            </ul>
                            <ul class="nav pull-right">
                                <li class="dropdown">
                                    <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                    <ul class="dropdown-menu">                                                                
                                        <li <%if(lang.equals("en")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url_E%>',furl:'<%=furl_E%>',status:'<%=status%>',lang:'en',group:'<%=group%>',new:'1',errCode:'<%=errCode%>',errMsg:'<%=errMsg%>'});">English</a></li>
                                        <li <%if(lang.equals("pt")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url_E%>',furl:'<%=furl_E%>',status:'<%=status%>',lang:'pt',group:'<%=group%>',new:'1',errCode:'<%=errCode%>',errMsg:'<%=errMsg%>'});">Português</a></li>
                                        <li <%if(lang.equals("es")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url_E%>',furl:'<%=furl_E%>',status:'<%=status%>',lang:'es',group:'<%=group%>',new:'1',errCode:'<%=errCode%>',errMsg:'<%=errMsg%>'});">Español</a></li>
                                        <!--li <%if(lang.equals("fr")) {%> class="disabled"<%}%>><a href="javascript:postToUrl('<%=response.encodeRedirectURL("editRecord.jsp")%>', {id:'<%=id%>',url:'<%=url_E%>',furl:'<%=furl_E%>',status:'<%=status%>',lang:'fr',group:'<%=group%>',new:'1',errCode:'<%=errCode%>',errMsg:'<%=errMsg%>'});">Francés</a></li-->
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
                        <li><a href="javascript:postToUrl('<%=response.encodeRedirectURL("list.jsp")%>', {group:'<%=group%>',lang:'<%=lang%>',dbFilter:'<%=dbFilter%>',collCenterFilter:'<%=collCenterFilter%>',order:'<%=order%>'});"><%=messages.getString("list")%></a> <span class="divider">/</span></li>
                        <li class="active"><%=messages.getString("edit")%></li>
                    </ul>     
                </div>
                <h1><%=messages.getString("edit_url")%></h1>
                <div class="urlEditor">
                    <div class="urlLine">
                        <div class="seg-q">
                            <div class="URL-tested">ID: <a target="_blank" href="http://pesquisa.bvsalud.org/portal/resource/<%=lang%>/lil-<%=id2%>"><%=id2%></a></div>
                            <div class="URL-tested">URL: <a target="_blank" href="<%=url_E%>"><%=url_D%></a> &#8594; ?</div>
                            <%
                            if (isBroken) {
                            %>
                            <div class="URL-tested">ERR: <%=errCode%> (<%=errMsg%>)</div>
                            <%
                            }
                            %>
                            <div class="URL-tested2">                                
                                <input  style="vertical-align:top;" type="url" id="input-1" class="span8" onfocus="hideSave()" value="<%=furl_D%>"/> &nbsp;
                            <%
                            if (isBroken) {
                            %>        
                                <a href="javascript:callUrl('<%=id%>','<%=url_E%>','<%=lang%>');" class="btn btn-primary" title="Test your changes"><%=messages.getString("test")%></a>
                            <%
                            }
                            if (isNew || isBroken) {
                            %>
                                <a id="save" href="javascript:callUrl2('<%=id%>','<%=url_E%>','<%=lang%>', 'force');" class="btn btn-primary enabled" title="<%=messages.getString("accept_tip")%>"><%=messages.getString("accept")%></a>                                
                            <%
                            }
                            %>                                
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
                                if (isBroken) {
                                } else {
                                %>       
                                <a id="save" href="javascript:callUrl2('<%=id%>','<%=url_E%>','<%=lang%>','does not force');" class="btn btn-primary enabled" title="<%=messages.getString("save_tip")%>"><%=messages.getString("save")%></a>
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

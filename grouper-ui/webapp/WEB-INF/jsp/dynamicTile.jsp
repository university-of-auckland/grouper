<%-- @annotation@
		  Tile which is configured to load dynamic tiles based on a 'view'
		  and a 'viewObject'
--%><%--
  @author Gary Brown.
  @version $Id: dynamicTile.jsp,v 1.2 2005-11-08 15:55:41 isgwb Exp $
--%>
<%@page import="org.apache.struts.tiles.ComponentContext"%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<!--       view=<c:out value="${view}"/>
    object type=<c:out value="${dynamicObjectType}"/>
       from key=<c:out value="${dynamicTemplateKey}"/>
    dynamicTile=<c:out value="${dynamicTemplate}"/>
--><%
ComponentContext tContext = ComponentContext.getContext(request);
pageContext.setAttribute("parentTilesContext",tContext);
String prefix = org.apache.struts.util.ModuleUtils.getInstance().getModuleConfig(request).getPrefix();
pageContext.setAttribute("modulePrefix",prefix);
%>	<grouper:recordTile view="${view}" type="${dynamicObjectType}" key="${dynamicTemplateKey}" tile="${dynamicTemplate}">
		<c:if test="${!empty dynamicTemplate}">
			<c:set var="safeObject" value="${viewObject}"/>
			<c:set var="safeTemplate" value="${dynamicTemplate}"/>
			
			<tiles:insert controllerUrl="${modulePrefix}/propogateTilesAttributes.do" page="${dynamicTemplate}">
				<tiles:put name="parentTilesContext" beanName="parentTilesContext"/>
			</tiles:insert>
			<c:set var="viewObject" scope="request" value="${safeObject}"/>
			
		</c:if>
	</grouper:recordTile>


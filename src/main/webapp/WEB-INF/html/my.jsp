<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%--<fmt:message key="messages.username"/>--%>
<fmt:message key="MVC_One"/>：<input type="text" name="username"/><br/>
<fmt:message key="MVC_TWO"/>：<input type="text" name="password"/><br/>

Language:
<a href="?locale=zh_CN">中文</a>  
<a href="?locale=en_US">英文</a><br/>
当前语言: ${pageContext.response.locale}
</body>
</html>
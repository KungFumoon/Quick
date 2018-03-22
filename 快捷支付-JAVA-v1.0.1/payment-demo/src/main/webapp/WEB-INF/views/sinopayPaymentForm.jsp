<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>订单支付</title>
    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
</head>
<body>

<form id="form" action="${action}" method="post">
    <input name="pGateWayReq" type="hidden" value="${pGateWayReq}" />
</form>

<script type="text/javascript">
    $(document).ready(function(){
        $("#form").submit();
    });
</script>

</body>
</html>

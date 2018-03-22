<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
<head>
    <style type="text/css">
        <!--
        TD {
            FONT-SIZE: 9pt
        }

        SELECT {
            FONT-SIZE: 9pt
        }

        OPTION {
            COLOR: #5040aa;
            FONT-SIZE: 9pt
        }

        INPUT {
            FONT-SIZE: 9pt
        }

        -->
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
</head>
<body bgcolor="#FFFFFF">
<table width="400" border="1" cellspacing="0" cellpadding="3"
       bordercolordark="#FFFFFF" bordercolorlight="#333333"
       bgcolor="#F0F0FF" align="center">
    <tr bgcolor="#8070FF">
        <td colspan="2">
            <div align="center">
                <font color="#FFFF00"><b>webService返回</b></font>
            </div>
        </td>
    </tr>
    <tr>
        <td>返回报文</td>
        <td>
            <textarea style="width: 250;height: 200;">${result}</textarea>
        </td>
    </tr>
</table>
</body>
</html>	

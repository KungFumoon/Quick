package com.ips.core.psfp.demo.servlet;

import com.ips.core.psfp.demo.entity.SinoPayRequestForm;
import com.ips.core.psfp.demo.utils.Verify;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IH1334 on 2016/11/21.
 */
public class PayMentServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try { // 获取页面请求信息
            SinoPayRequestForm merchantForm = createMerForm(request);
            Map<String, String> map = getUrlByCfg();
            String action = map.get("formAction");

            StringBuffer sb = new StringBuffer();
            sb.append("<body>");
            sb.append("<MerBillNo>" + merchantForm.getMerBillNo() + "</MerBillNo>");
            sb.append("<Lang>" + merchantForm.getLang() + "</Lang>");
            sb.append("<Amount>" + merchantForm.getAmount() + "</Amount>");
            sb.append("<Date>" + merchantForm.getDate() + "</Date>");
            sb.append("<CurrencyType>" + merchantForm.getCurrencyType() + "</CurrencyType>");
            sb.append("<GatewayType>" + merchantForm.getGatewayType() + "</GatewayType>");
            sb.append("<Merchanturl>" + merchantForm.getMerchantUrl() + "</Merchanturl>");
            sb.append("<FailUrl><![CDATA[" + merchantForm.getFailUrl() + "]]></FailUrl>");
            sb.append("<Attach><![CDATA[" + merchantForm.getAttach() + "]]></Attach>");
            sb.append("<OrderEncodeType>" + merchantForm.getOrderEncodeType() + "</OrderEncodeType>");
            sb.append("<RetEncodeType>" + merchantForm.getRetEncodeType() + "</RetEncodeType>");
            sb.append("<RetType>" + merchantForm.getRettype() + "</RetType>");
            sb.append("<ServerUrl><![CDATA[" + merchantForm.getServerUrl() + "]]></ServerUrl>");
            sb.append("<BillEXP>" + merchantForm.getBillExp() + "</BillEXP>");
            sb.append("<GoodsName>" + merchantForm.getGoodsName() + "</GoodsName>");
            sb.append("<IsCredit>" + merchantForm.getIsCredit() + "</IsCredit>");
            sb.append("<BankCode>" + merchantForm.getBankcode() + "</BankCode>");
            sb.append("<ProductType>" + merchantForm.getProductType() + "</ProductType>");
            if (!merchantForm.getCardInfo().equals("")) {
                String desKey = map.get("desKey");
                String desIv = map.get("desIv");
                sb.append("<CardInfo>" + Verify.encrypt3DES(merchantForm.getCardInfo(), desKey, desIv) + "</CardInfo>");
            }
            sb.append("</body>");
            // body部分
            String bodyXml = sb.toString();

            // MD5签名
            String directStr = map.get("directStr");
            String sign = DigestUtils
                    .md5Hex(Verify.getBytes(bodyXml + merchantForm.getMerCode() + directStr,
                            "UTF-8"));
            // xml
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String xml = "<Ips>" +
                    "<GateWayReq>" +
                    "<head>" +
                    "<Version>" + merchantForm.getVersion() + "</Version>" +
                    "<MerCode>" + merchantForm.getMerCode() + "</MerCode>" +
                    "<MerName>" + merchantForm.getMerName() + "</MerName>" +
                    "<Account>" + merchantForm.getMerAcccode() + "</Account>" +
                    "<MsgId>" + "msg" + date + "</MsgId >" +
                    "<ReqDate>" + date + "</ReqDate >" +
                    "<Signature>" + sign + "</Signature>" +
                    "</head>" +

                    bodyXml +

                    "</GateWayReq>" +
                    "</Ips>";
            System.out.println(">>>>> 订单支付 请求信息: " + xml);
            request.setAttribute("action", action);
            request.setAttribute("pGateWayReq", xml);
            request.getRequestDispatcher("WEB-INF/views/sinopayPaymentForm.jsp").forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private SinoPayRequestForm createMerForm(HttpServletRequest request) {
        SinoPayRequestForm merchantForm = new SinoPayRequestForm();
        merchantForm.setMerBillNo(request.getParameter("Billno"));
        merchantForm.setAmount(request.getParameter("Amount"));
        merchantForm.setCurrencyType(request.getParameter("Currency_Type"));
        merchantForm.setDate(request.getParameter("Date"));
        merchantForm.setOrderEncodeType(request.getParameter("OrderEncodeType"));
        merchantForm.setGatewayType(request.getParameter("Gateway_Type"));
        merchantForm.setLang(request.getParameter("Lang"));
        merchantForm.setMerchantUrl(request.getParameter("Merchanturl"));
        merchantForm.setFailUrl(request.getParameter("FailUrl"));
        merchantForm.setAttach(request.getParameter("Attach"));
        merchantForm.setRetEncodeType(request.getParameter("RetEncodeType"));
        merchantForm.setRettype(request.getParameter("Rettype"));
        merchantForm.setServerUrl(request.getParameter("ServerUrl"));
        merchantForm.setBillExp(request.getParameter("BillEXP"));
        merchantForm.setGoodsName(request.getParameter("CommodityName"));
        merchantForm.setIsCredit(request.getParameter("DoCredit"));
        merchantForm.setBankcode(request.getParameter("Bankco"));
        merchantForm.setProductType(request.getParameter("PrdType"));
        merchantForm.setSysCode(request.getParameter("SysCode"));
        merchantForm.setWhoFee(request.getParameter("WhoFee"));
        merchantForm.setFeeType(request.getParameter("FeeType"));
        merchantForm.setUserId(request.getParameter("UserId"));
        merchantForm.setUserRealName(request.getParameter("UserRealName"));
        merchantForm.setBizType(request.getParameter("BizType"));
        merchantForm.setMerCode(request.getParameter("Mer_code"));
        merchantForm.setMerName(request.getParameter("Mer_Name"));
        merchantForm.setMerAcccode(request.getParameter("Mer_acccode"));
        merchantForm.setVersion(request.getParameter("version"));
        merchantForm.setCardInfo(request.getParameter("CardInfo"));
        return merchantForm;
    }

    /**
     * 获取报文中<Signature></Signature>部分
     *
     * @param xml
     * @return
     */
    public String getSign(String xml) {
        int s_index = xml.indexOf("<Signature>");
        int e_index = xml.indexOf("</Signature>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 11, e_index);
        }
        return sign;
    }

    /**
     * 获取body部分
     *
     * @param xml
     * @return
     */
    public String getBodyXml(String xml) {
        int s_index = xml.indexOf("<body>");
        int e_index = xml.indexOf("</body>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index, e_index + 7);
        }
        return sign;
    }

    /**
     * 获取报文中<RspCode></RspCode>部分
     *
     * @param xml
     * @return
     */
    public String getRspCode(String xml) {
        int s_index = xml.indexOf("<RspCode>");
        int e_index = xml.indexOf("</RspCode>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 9, e_index);
        }
        return sign;
    }

    /**
     * 获取报文中<Status></Status>部分
     *
     * @param xml
     * @return
     */
    public String getStatus(String xml) {
        int s_index = xml.indexOf("<Status>");
        int e_index = xml.indexOf("</Status>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 8, e_index);
        }
        return sign;
    }

    /**
     * 获取报文中<RetEncodeType></RetEncodeType>部分
     *
     * @param xml
     * @return
     */
    public String getRetEncodeType(String xml) {
        int s_index = xml.indexOf("<RetEncodeType>");
        int e_index = xml.indexOf("</RetEncodeType>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 15, e_index);
        }
        return sign;
    }

    /**
     * 获取报文中<Amount></Amount>部分
     *
     * @param xml
     * @return
     */
    public String getAmount(String xml) {
        int s_index = xml.indexOf("<Amount>");
        int e_index = xml.indexOf("</Amount>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 8, e_index);
        }
        return sign;
    }

    /**
     * 获取报文中<Date></Date>部分
     *
     * @param xml
     * @return
     */
    public String getDate(String xml) {
        int s_index = xml.indexOf("<Date>");
        int e_index = xml.indexOf("</Date>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 6, e_index);
        }
        return sign;
    }

    /**
     * 获取配置文件
     *
     * @return
     */
    public Map<String, String> getUrlByCfg() {
        try {
            Properties prop = new Properties();
            InputStream ins = this.getClass().getClassLoader().getResourceAsStream("configurations.properties");
            prop.load(ins);
            // URL
            String formAction = prop.getProperty("formAction");
            // 商户证书
            String directStr = prop.getProperty("directStr");
            String desKey = prop.getProperty("desKey");
            String desIv = prop.getProperty("desIv");
            System.out.println("formAction ：" + formAction);
            System.out.println("directStr ：" + directStr);
            Map<String, String> map = new HashMap<String, String>();
            map.put("formAction", formAction);
            map.put("directStr", directStr);
            map.put("desKey", desKey);
            map.put("desIv", desIv);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

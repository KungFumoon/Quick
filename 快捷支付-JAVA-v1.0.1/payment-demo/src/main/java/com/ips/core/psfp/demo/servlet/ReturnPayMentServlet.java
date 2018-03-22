package com.ips.core.psfp.demo.servlet;

import com.ips.core.psfp.demo.utils.Verify;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IH1334 on 2016/11/23.
 */
public class ReturnPayMentServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		System.out.println("接收服务端返回：===================");
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		try{
			// 获取xml
			String resultXml = req.getParameter("paymentResult");
			// 读取配置文件
			Map<String, String> map = getParamsByCfg();
			String merCode = map.get("merCode");
			String directStr = map.get("directStr");
			// 获取请求uri
			String httpInfo  =  req.getRequestURI();
			if (httpInfo.contains("/merchant/success")) {
				// 支付结果成功返回
				String result = returnPaymentPage(resultXml, merCode, directStr);
				req.setAttribute("result", result);
				// 返回相应页面
				req.getRequestDispatcher("/WEB-INF/views/wsResult.jsp").forward(req, resp);
			} else if (httpInfo.contains("/merchant/s2surl")) {
				// S2S返回
				s2sNotify(resultXml, merCode, directStr);
				System.out.println("ipscheckok");
				PrintWriter printer = resp.getWriter();
				printer.write("ipscheckok");
				printer.flush();
				printer.close();
			} else if (httpInfo.contains("/merchant/notifyurl")) {
				// http://192.168.12.110:8080/payment-demo/merchant/notifyurl.html
				// 主动对账返回  地址需联系技术支持配置
				s2sNotify(resultXml, merCode, directStr);
				// (为主动对账返回接收，需返回“ipscheckok”)
				System.out.println("ipscheckok");
				PrintWriter printer = resp.getWriter();
				printer.write("ipscheckok");
				printer.flush();
				printer.close();
			}
		} catch (Throwable ex){
			ex.printStackTrace();
		}
	}

	public String returnPaymentPage(String resultXml, String merCode, String directStr) {

		System.out.println(">>>>>(merchant/pagexml) received success message from IPS......" + resultXml);
		// 1、获取签名方式 验签
		if (!checkSign(resultXml, merCode, directStr)) {
			System.out.println("验签失败");
			return "验签失败";
		}
		//2、 验签通过，判断IPS返回状态码
		if (!getRspCode(resultXml).equals("000000")) {
			// 具体错误信息可获取<RspMsg></RspMsg>
			System.out.println("请求响应不成功");
			return resultXml;
		}
		//3、通过返回商户订单编号获取商户系统该笔订单金额和订单日期 与报文返回订单金额和订单日期进行比较
		/*BigDecimal amount = new BigDecimal(0.02);
		try {
			BigDecimal backAmount = new BigDecimal(getAmount(resultXml));
			if (backAmount.compareTo(amount) != 0) {
				System.out.println("返回订单金额有误");
				return resultXml;
			}
		}catch(Throwable ex) {
			System.out.println("返回订单金额有误");
			return resultXml;
		}
		String dateStr = "20160818";
		String backDate = getDate(resultXml);
		if (backDate == null || !backDate.equals(dateStr)) {
			System.out.println("返回订单日期有误");
			return resultXml;
		}*/
		//4、IPS返回成功  根据交易状态做相应处理
		String status = getStatus(resultXml);
		if (status.equals("Y")) {
			System.out.println("交易成功");
		} else if (status.equals("N")) {
			System.out.println("交易失败");
		} else if (status.equals("P")) {
			System.out.println("交易处理中");
		}
		// 5、商户自己业务逻辑处理
		/**
		 * TODO
		 */
		return resultXml;
	}

	public void s2sNotify(String resultXml, String merCode, String directStr) {

		System.out.println(">>>>>(merchant/s2sxml) received message from IPS......" + resultXml);
		// 1、获取签名方式 验签
		if (!checkSign(resultXml, merCode, directStr)) {
			System.out.println("验签失败");
			return;
		}
		//2、验签通过，判断IPS返回状态码
		if (!getRspCode(resultXml).equals("000000")) {
			// 具体错误信息可获取<RspMsg></RspMsg>
			System.out.println("请求响应不成功");
			return;
		}
		//3、IPS返回成功  根据交易状态做相应处理
		String status = getStatus(resultXml);
		if (status.equals("Y")) {
			System.out.println("交易成功");
		} else if (status.equals("N")) {
			System.out.println("交易失败");
		} else if (status.equals("P")) {
			System.out.println("交易处理中");
		}
		//4、通过返回商户订单编号获取商户系统该笔订单金额和订单日期 与报文返回订单金额和订单日期进行比较
		/*BigDecimal amount = new BigDecimal(0.02);
		try {
			BigDecimal backAmount = new BigDecimal(getAmount(resultXml));
			if (backAmount.compareTo(amount) != 0) {
				System.out.println("返回订单金额有误");
				return;
			}
		}catch(Throwable ex) {
			System.out.println("返回订单金额有误");
			return;
		}
		String dateStr = "20160818";
		String backDate = getDate(resultXml);
		if (backDate == null || !backDate.equals(dateStr)) {
			System.out.println("返回订单日期有误");
			return;
		}*/

		// 5、商户自己业务逻辑处理
		/**
		 * TODO
		 */

	}

	/**
	 * 验签
	 *
	 * @param xml
	 * @return
	 */
	public boolean checkSign(String xml, String merCode, String directStr) {

		if (xml == null){
			return false;
		}
		String OldSign = getSign(xml); // 返回签名
		String text = getBodyXml(xml); // body
		System.out.println("MD5验签，验签文：" + text + "\n待比较签名值:" + OldSign);
		String retEncodeType =  getRetEncodeType(xml); //加密方式
		System.out.println("加密方式 ：" + retEncodeType);
		String result = null;
		if (OldSign == null || retEncodeType == null) {
			return false;
		}
		// 根据验签方式进行验签
		if (retEncodeType.equals("17")){
			result = DigestUtils
					.md5Hex(Verify.getBytes(text + merCode + directStr,
							"UTF-8"));
		} else {
			return false;
		}
		if (result == null || !OldSign.equals(result)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取报文中<Signature></Signature>部分
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
	 * @return
	 */
	public Map<String, String> getParamsByCfg() {
		try {
			Properties prop = new Properties();
			InputStream ins = this.getClass().getClassLoader().getResourceAsStream("configurations.properties");
			prop.load(ins);
			// 商户号(需与页面上传输的商户号保持一致)
			String merCode = prop.getProperty("merCode");
			// 商户证书
			String directStr = prop.getProperty("directStr");
			// ips公钥
			System.out.println("merCode ：" + merCode);
			System.out.println("directStr ：" + directStr);
		
			Map<String, String> map = new HashMap<String, String>();
			map.put("merCode",merCode);
			map.put("directStr",directStr);
		
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

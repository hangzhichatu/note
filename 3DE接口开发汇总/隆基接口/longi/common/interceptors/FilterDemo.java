package longi.common.interceptors;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspose.cad.system.Threading.Thread;
import com.matrixone.apps.framework.ui.UIUtil;

import longi.common.util.LONGiPropertiesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import small.danfer.sso.SingleSignOnConfig;
import small.danfer.sso.SingleSignOnConfigHolder;
import small.danfer.sso.SingleSignOnConstants.URI_TYPE;
import small.danfer.sso.http.PathMap;


public class FilterDemo implements Filter {
	private final static Logger logger = LoggerFactory.getLogger(FilterDemo.class);
	
//	private final static String appUrl = "https://plmdev2.longi.com/internal";
	private final static String appUrl = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_URL");
//	private final static String client_id = "wlbmtest";
	private final static String client_id = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_SSO_ID");
	private final static String client_auth = LONGiPropertiesUtil.readInterfaceProperties("SSO_AUTH_URL");
	
	protected PathMap pathMap = new PathMap();
	protected int uriBeginIndex = 0;
	protected boolean useBlackMechanism = true;
	protected boolean hasCode = false;
	protected HashMap<String, String> logonParams = new HashMap();
	protected HashMap<String, String> logoutParams = new HashMap();
	protected boolean logonURISameWithLogoutURI = false;
	
	@Override
	public void destroy(){
		System.out.println("销毁操作");
	}

	/**
	* @Title: doFilter
	* @Description: 过滤器实现方法，通过Code查询
	* @param servletReq
	* @param servletRep
	* @param chain
	* @throws IOException
	* @throws ServletException
	* @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	*/
	@Override
	public void doFilter(ServletRequest servletReq, ServletResponse servletRep, FilterChain chain)
			throws IOException, ServletException {
//		response.setCharacterEncoding("UTF-8");
//		request.setCharacterEncoding("UTF-8");
//		response.setContentType("text/html");
//		chain.doFilter(request, response);
		try {
//			String appUrl1 = "https://plmshare.longi.com/internal";
			HttpServletRequest request = (HttpServletRequest) servletReq;
			MyHttpServletRequestWrapper1 requestWrapper1 = new MyHttpServletRequestWrapper1(
					(HttpServletRequest) servletReq);
			HttpServletResponse response = (HttpServletResponse) servletRep;
//			SingleSignOnConfig config = SingleSignOnConfigHolder.getConfig();
			String allUrlString = request.getRequestURI();
			logger.info("request.getRequestURI():"+request.getRequestURI());
			//封装log实体类
			String code = requestWrapper1.getParameter("code");
//			String state = requestWrapper1.getParameter("state");
			
//			if(UIUtil.isNotNullAndNotEmpty(code) && code != null){
//
//				System.out.println("开始请求token code:"+code);
//				String tokenJson = SSOHttpPost.getTokenPost(code,"https://plmdev1.longi.com/internal");
//				System.out.println("token 结果:"+tokenJson);
//				Map tokenMap = (Map) JSON.parse(tokenJson);
//				//{"access_token":"AT-126-jw2eZaSkFpRcKlQmX9jQxkspT96oDt5qj6e","token_type":"bearer","expires_in":86400,"refresh_token":"RT-113-dahxUSIjMreqGJVa6kbpORg6I7gN9gYD4bA"}
//				if(tokenMap.containsKey("access_token")){
//					String access_token = tokenMap.get("access_token").toString();
////					response.sendRedirect(tokenURL);
//
//
//					System.out.println("开始请求UID token:"+access_token);
//					String uidJson = SSOHttpPost.getUIDsGet(access_token);
//					System.out.println("uid结果:"+uidJson);
//				}
////				String internalUrl = "";
////				System.out.println(longi.common.util.UrlUtil.getURLEncoderString(string));
//
//				//response.sendRedirect(tokenURL);
//			}


//			String uri = request.getRequestURI().substring(this.uriBeginIndex);
//			System.out.println("当前轮CODE：" + code);
//			System.out.println("request.getRequestURI():：" + request.getRequestURI());
			logger.info("allUrlString：:"+allUrlString);
			if(UIUtil.isNullOrEmpty(code) && (allUrlString.equals("/internal/emxLogin.jsp") || allUrlString.equals("/internal/"))){
//				if(UIUtil.isNullOrEmpty(code) && (allUrlString.equals("/internal/emxLogin.jsp") || allUrlString.equals("/internal/") || allUrlString.equals("/internal/common/emxNavigator.jsp"))){
//				String redirectUrl = "https://esctest.longi.com/esc-sso/oauth2.0/authorize?client_id="+client_id+"&response_type=code&redirect_uri="+appUrl+"&oauth_timestamp="+new Date().getTime()+"&state="+appUrl;
				//https://sso.longi.com:9050/esc-sso/oauth2.0/authorize?client_id=wlbm&response_type=code&redirect_uri=https://10.0.150.72:8070/internal/common/emxNavigator.jsp?objectId=30103.58115.8376.59456
				String redirectUrl = client_auth+"?client_id="+client_id+"&response_type=code&redirect_uri="+appUrl;
//				response.sendRedirect("https://esctest.longi.com/esc-sso/oauth2.0/authorize?client_id=wlbmtest&response_type=code&redirect_uri=https://plmdev1.longi.com/internal&oauth_timestamp="+new Date().getTime()+"&state=https://plmdev1.longi.com/internal");
				logger.info("登陆重定向地址：:"+redirectUrl);
//				System.out.println("认证地址：redirectUrl：" + redirectUrl);
				response.sendRedirect(redirectUrl);
			}else {
//				hasCode = true;
				logger.info("Code 存在的情况获取编码code:"+code);
				System.out.println("Code 存在的情况获取编码，直接转发地址："+ request.getRequestURL());
				chain.doFilter(request, response);
			}
			
//			response.sendRedirect("../failure.jsp");

//			if (URI_TYPE.OPERATION_HANDLE_URI.equals(uriType)) {
////				this.handlePrivateOperation(request, response, chain, config);
//			} else if (URI_TYPE.LOGON_URI.equals(uriType) && this.matchLogonParams(request)) {
//				this.handleSingleSignOn(request, response, chain, config);
//			} else if (URI_TYPE.LOGOUT_URI.equals(uriType) && this.matchLogoutParams(request)) {
//				this.handleSingleSignOut(request, response, chain, config);
//			} else if (this.logonURISameWithLogoutURI && URI_TYPE.LOGOUT_URI.equals(uriType)
//					&& this.matchLogonParams(request)) {
//				this.handleSingleSignOn(request, response, chain, config);
//			} else {
//				this.doDefaultProcess(request, response, chain, config, uriType);
//			}
			//https://esctest.longi.com/esc-sso/oauth2.0/accessToken?grant_type=authorization_code&oauth_timestamp=1630647508001&client_id=wlbmtest&client_secret=64482a80-b86d-4052-8524-6fe3a1e58d32&code=OC-14359-ViyzChdmxCdgTpRFcue9z6l77JYdzb2DHpD&redirect_uri=http://plmdev1.longi.com/internal




		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new IOException(e);
		}
		
		
	}

	/**
	* @Title: init
	* @Description: 拦截器初始化
	* @param filterConfig
	* @throws ServletException
	* @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	*/
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("登陆初始化init");
		String contextPath = filterConfig.getServletContext().getContextPath();
		if (logger.isDebugEnabled()) {
			logger.debug("Init SingleSignOnFilter, current context path is [" + contextPath + "]");
		}

		if (!contextPath.equals("/")) {
			this.uriBeginIndex = contextPath.length();
		}

		this.pathMap.put("/iTrusSsoPrivateOperation", URI_TYPE.OPERATION_HANDLE_URI);
		String blackURI = filterConfig.getInitParameter("blackURI");
		String whiteURI = filterConfig.getInitParameter("whiteURI");
		boolean isBlack = blackURI != null && !blackURI.equals("");
		boolean isWhite = whiteURI != null && !whiteURI.equals("");
		if (isBlack && isWhite) {
			this.pathMap.put(whiteURI, URI_TYPE.WHITE_URI);
			System.out.println("WhiteURI:[" + whiteURI + "]");
			this.useBlackMechanism = false;
		} else if (isBlack) {
			this.pathMap.put(blackURI, URI_TYPE.BLACK_URI);
			System.out.println("BlackURI:[" + blackURI + "]");
			this.useBlackMechanism = true;
		} else if (isWhite) {
			this.pathMap.put(whiteURI, URI_TYPE.WHITE_URI);
			System.out.println("WhiteURI:[" + whiteURI + "]");
			this.useBlackMechanism = false;
		} else {
			this.pathMap.put("/*", URI_TYPE.WHITE_URI);
			System.out.println("WhiteURI:[/*]");
			this.useBlackMechanism = false;
		}
		logger.info("登陆初始化结果："+this.pathMap);
//
//		String logonURI = filterConfig.getInitParameter("logonURI");
//		String ssoConfigFilePath;
//		if (logonURI != null) {
//			if (logonURI.contains("?")) {
//				int beginIndex = logonURI.indexOf("?");
//				ssoConfigFilePath = logonURI.substring(beginIndex + 1);
//				this.markParams(ssoConfigFilePath, this.logonParams);
//				logonURI = logonURI.substring(0, beginIndex);
//			}
//
//			this.pathMap.put(logonURI, URI_TYPE.LOGON_URI);
//		}
//
//		String logoutURI = filterConfig.getInitParameter("logoutURI");
//		String autoReload;
//		if (logoutURI != null) {
//			if (logoutURI.contains("?")) {
//				int beginIndex = logoutURI.indexOf("?");
//				autoReload = logoutURI.substring(beginIndex + 1);
//				this.markParams(autoReload, this.logoutParams);
//				logoutURI = logoutURI.substring(0, beginIndex);
//			}
//
//			this.pathMap.put(logoutURI, URI_TYPE.LOGOUT_URI);
//		}
//
//		this.logonURISameWithLogoutURI = logonURI.equals(logoutURI);
	}
}

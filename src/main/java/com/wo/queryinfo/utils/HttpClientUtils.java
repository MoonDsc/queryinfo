package com.wo.queryinfo.utils;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient工具类
 * 
 * @author ZhangYuanHui
 *
 */

public class HttpClientUtils {

	private HttpClientUtils() {

	}

	/**
	 * 连接超时时间
	 */
	public static final int CONNECTION_TIMEOUT_MS = 1000 * 25;

	/**
	 * 读取数据超时时间
	 */
	public static final int SO_TIMEOUT_MS = 1000 * 70;

	/**
	 * httpclient读取内容时使用的字符集
	 */
	public static final String CHARSET = "UTF-8";
	
	private static PoolingHttpClientConnectionManager httpConnMgr;
	
	private static PoolingHttpClientConnectionManager httpsConnMgr;

	static {
		// 设置连接池
		httpConnMgr = new PoolingHttpClientConnectionManager();
		// 设置连接池大小
		httpConnMgr.setMaxTotal(50);
		httpConnMgr.setDefaultMaxPerRoute(httpConnMgr.getMaxTotal());
		
		// 设置连接池
		httpsConnMgr = new PoolingHttpClientConnectionManager();
		// 设置连接池大小
		httpsConnMgr.setMaxTotal(50);
		httpsConnMgr.setDefaultMaxPerRoute(httpsConnMgr.getMaxTotal());
	}  

	/**
	 * 创建HttpClient
	 * 
	 * @param isMultiThread 是否多线程
	 * @return HttpClient
	 */
	public static CloseableHttpClient buildHttpClient(boolean isMultiThread) {
		CloseableHttpClient client;
		if (isMultiThread) {
			client = HttpClientBuilder.create()
					.setConnectionManager(httpConnMgr).build();
		} else {
			client = HttpClientBuilder.create().build();
		}
		// 设置代理服务器地址和端口
		// client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
		return client;
	}
	
	/**
	 * 创建HttpClient
	 * 
	 * @return HttpClient
	 */
	public static CloseableHttpClient buildHttpClient() {
		return buildHttpClient(false);
	}
	
	/**
	 * 创建HttpsClient
	 * 
	 * @param isMultiThread 是否多线程
	 * @return HttpClient
	 */
	public static CloseableHttpClient buildHttpsClient(boolean isMultiThread) {
		CloseableHttpClient client;
		if (isMultiThread) {
			client = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(httpsConnMgr).build();
		} else {
			client = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).build();
		}
		// 设置代理服务器地址和端口
		// client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
		return client;
	}
	
	/**
	 * 创建HttpsClient
	 * @Title: buildHttpsClient
	 * @return Client
	 */
	public static CloseableHttpClient buildHttpsClient() {
		return buildHttpsClient(false);
	}
	
	/** 
     * 创建SSL安全连接 
     * @return SSLConnectionSocketFactory
     */  
	private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
		SSLConnectionSocketFactory sslsf = null;
		X509TrustManager x509mgr = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] xcs, String string) {}
			public void checkServerTrusted(X509Certificate[] xcs, String string) {}
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] {x509mgr}, new SecureRandom());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		sslsf = new SSLConnectionSocketFactory(sslContext, hv);
		return sslsf;
	}
	
	/** 
     * 创建SSL安全连接 (废弃)
     * 
     * @return factory
     */  
	@SuppressWarnings("unused")
	private static SSLConnectionSocketFactory createSSLConnSocketFactory_OLD() {
		SSLConnectionSocketFactory sslsf = null;
		try {
			SSLContext sslContext =
					new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
						// 信任所有
						public boolean isTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
							return true;
						}
					}).build();
			sslsf = new SSLConnectionSocketFactory(sslContext);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return sslsf;
	}
	
	/**
	 * 设置成消息体的长度 setting MessageBody length
	 * 
	 * @param httpMethod
	 * @param he
	 */
	public static void setContentLength(HttpRequestBase httpMethod, HttpEntity he) {
		if (he == null) {
			return;
		}
		httpMethod.setHeader(HTTP.CONTENT_LEN, String.valueOf(he.getContentLength()));
	}

	/**
	 * 构建公用RequestConfig
	 * 
	 * @return config
	 */
	public static RequestConfig buildRequestConfig() {
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SO_TIMEOUT_MS)
				.setConnectTimeout(CONNECTION_TIMEOUT_MS).build();
		return requestConfig;
	}

	/**
	 * 构建httpGet对象
	 * 
	 * @param url url
	 * @param params params
	 * @return HttpGet
	 * @throws URISyntaxException 异常
	 */
	public static HttpGet buildHttpGet(String url, Map<String, Object> params)
			throws URISyntaxException {
		Assert.notNull(url, "构建HttpGet时,url不能为null");
		HttpGet get = new HttpGet(buildGetUrl(url, params));
		setCommonHttpMethod(get);
		get.setConfig(buildRequestConfig());
		return get;
	}

	/**
	 * 构建httpPost对象
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return HttpPost
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	public static HttpPost buildHttpPost(String url, Map<String, Object> headers, Map<String, Object> params, String charset)
			throws UnsupportedEncodingException, URISyntaxException {
		Assert.notNull(url, "构建HttpPost时,url不能为null");
		HttpPost post = new HttpPost(url);
		if(null != headers){
			for (String header : headers.keySet()) {
				post.setHeader(header, headers.get(header) + "");
			}
		}

		setCommonHttpMethod(post);
		post.setConfig(buildRequestConfig());
		HttpEntity he = null;
		if (params != null) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				formparams.add(new BasicNameValuePair(key, params.get(key) + ""));
			}
			he = new UrlEncodedFormEntity(formparams, charset);
			post.setEntity(he);
		}
		// 在RequestContent.process中会自动写入消息体的长度，自己不用写入，写入反而检测报错
		// setContentLength(post, he);
		return post;
	}

	/**
	 * 构建httpPost对象(数据流)
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @param charset 字符集
	 * @return HttpPost
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	public static HttpPost buildHttpPost(String url, Map<String, Object> headers, String params, String charset)
			throws UnsupportedEncodingException, URISyntaxException {
		Assert.notNull(url, "构建HttpPost时,url不能为null");
		HttpPost post = new HttpPost(url);
		if(null != headers){
			for (String header : headers.keySet()) {
				post.setHeader(header, headers.get(header) + "");
			}
		}
		setCommonHttpMethod(post);
		post.setConfig(buildRequestConfig());
		// 以流的形式提交数据
		StringEntity entity = new StringEntity(params, charset);
		post.setEntity(entity);
		// 在RequestContent.process中会自动写入消息体的长度，自己不用写入，写入反而检测报错
		// setContentLength(post, he);
		return post;
	}
	
	/**
	 * 构建httpPost对象(数据流)
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return HttpPost
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	public static HttpPost buildHttpPost(String url, String params)
			throws UnsupportedEncodingException, URISyntaxException {
		Assert.notNull(url, "构建HttpPost时,url不能为null");
		return buildHttpPost(url, null, params, CHARSET);
	}
	
	/**
	 * 设置HttpMethod通用配置
	 * 
	 * @param httpMethod
	 */
	public static void setCommonHttpMethod(HttpRequestBase httpMethod) {
		// setting
		//httpMethod.setHeader(HTTP.CONTENT_ENCODING, CHARSET);
		//httpMethod.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=utf-8");
		//httpMethod.setHeader(HTTP.CONTENT_TYPE, "text/html;charset=utf-8");
	}
	
	/**
	 * 设置HttpMethod通用配置
	 * 
	 * @param httpMethod
	 * @param contentTypeMap
	 */
	public static void setCommonHttpMethod(HttpRequestBase httpMethod, Map<String, String> contentTypeMap) {
		if(null!= contentTypeMap && contentTypeMap.size() > 0){
			for (String contentType : contentTypeMap.keySet()) {
				httpMethod.setHeader(contentType, contentTypeMap.get(contentType));
			}
		}else{
			setCommonHttpMethod(httpMethod);
		}
	}

	/**
	 * 强验证必须是200状态否则报异常
	 * 
	 * @param res
	 * @throws HttpException 异常
	 */
	static void assertStatus(HttpResponse res) throws IOException {
		Assert.notNull(res, "http响应对象为null");
		Assert.notNull(res.getStatusLine(), "http响应对象的状态为null");
		switch (res.getStatusLine().getStatusCode()) {
			case HttpStatus.SC_OK:
			//case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			// case HttpStatus.SC_CREATED:
			// case HttpStatus.SC_ACCEPTED:
			// case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
			// case HttpStatus.SC_NO_CONTENT:
			// case HttpStatus.SC_RESET_CONTENT:
			// case HttpStatus.SC_PARTIAL_CONTENT:
			// case HttpStatus.SC_MULTI_STATUS:
				break;
			default:
				throw new IOException("服务器响应状态异常,失败.");
		}
	}
	
	/**
	 * 处理http请求返回结果
	 * @param response 响应
	 * @return String
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String processResponse(HttpResponse response) throws ClientProtocolException, IOException, URISyntaxException {
		return processResponse(response, CHARSET);
	}
	
	/**
	 * 处理http请求返回结果
	 * @param response 响应
	 * @param charset 编码格式
	 * @return String
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String processResponse(HttpResponse response, String charset) throws ClientProtocolException, IOException, URISyntaxException {
		// assertStatus(response);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String returnStr = EntityUtils.toString(entity, charset);
			return returnStr;
		}
		return null;
	}
	
	/**
	 * get调用
	 * 
	 * @param url 请求地址
	 * @return String String 请求结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGet(String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		return doGet(url, null);
	}

	/**
	 * get调用
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return String String 请求结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGet(String url, Map<String, Object> params)
			throws ClientProtocolException, IOException, URISyntaxException {
		return doGet(url, params, CHARSET);
	}
	
	/**
	 * get代理调用
	 * @Title: doGet
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param proxyHost 代理地址
	 * @param port 代理端口
	 * @param charset 字符集
	 * @return 结果信息
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGet(String url, Map<String, Object> params, String proxyHost, int port, String charset)
			throws ClientProtocolException, IOException, URISyntaxException {
		HttpHost proxy = new HttpHost(proxyHost, port);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
		CloseableHttpClient httpClient = HttpClients.custom().setRoutePlanner(routePlanner).build();
		HttpGet get = buildHttpGet(url, params);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000 * 10)
				.setConnectTimeout(1000 * 10).build();
		get.setConfig(requestConfig);
		CloseableHttpResponse response = httpClient.execute(get);
		String result = processResponse(response, charset);
		response.close();
		httpClient.close();
		return result;
	}
	
	/**
	 * get代理调用
	 * @Title: doGet
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param proxyHost 代理地址
	 * @param port 代理端口
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGet(String url, Map<String, Object> params, String proxyHost, int port)
			throws ClientProtocolException, IOException, URISyntaxException {
		return doGet(url, params, proxyHost, port, CHARSET);
	}

	/**
	 * get调用
	 * 
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param charset 编码格式
	 * @return String String 请求结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGet(String url, Map<String, Object> params, String charset)
			throws ClientProtocolException, IOException, URISyntaxException {
		CloseableHttpClient client = buildHttpClient(false);
		HttpGet get = buildHttpGet(url, params);
		CloseableHttpResponse response = client.execute(get);
		String result = processResponse(response, charset);
		response.close();
		client.close();
		return result;
	}
	/*********************************** https get ******************************************/
	/**
	 * get调用 https
	 * 
	 * @param url 请求地址
	 * @return String String 请求结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGetSSL(String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		return doGetSSL(url, null);
	}

	/**
	 * get调用 https
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return String String 请求结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGetSSL(String url, Map<String, Object> params)
			throws ClientProtocolException, IOException, URISyntaxException {
		return doGetSSL(url, params, CHARSET);
	}

	/**
	 * get调用 https
	 * 
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param charset 编码格式
	 * @return String String 请求结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String doGetSSL(String url, Map<String, Object> params, String charset)
			throws ClientProtocolException, IOException, URISyntaxException {
		CloseableHttpClient client = buildHttpsClient(false);
		HttpGet get = buildHttpGet(url, params);
		CloseableHttpResponse response = client.execute(get);
		String result = processResponse(response, charset);
		response.close();
		client.close();
		return result;
	}
	/*********************************** https get end ******************************************/
	
	/**
	 * post调用 
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return String
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPost(String url, Map<String, Object> params)
			throws URISyntaxException, ClientProtocolException, HttpException, IOException {
		return doPost(url, null, params, CHARSET);
	}

	public static String doPost(String url, Map<String, Object> headers, Map<String, Object> params)
			throws URISyntaxException, ClientProtocolException, HttpException, IOException {
		return doPost(url, headers, params, CHARSET);
	}

	/**
	 * post调用
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @param charset 编码格式
	 * @return String 请求结果
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPost(String url, Map<String, Object> headers, Map<String, Object> params, String charset)
			throws URISyntaxException, ClientProtocolException, HttpException, IOException {
		CloseableHttpClient client = buildHttpClient(false);
		HttpPost postMethod = buildHttpPost(url, headers, params, charset);
		CloseableHttpResponse response = client.execute(postMethod);
		StatusLine status = response.getStatusLine();
		String result = processResponse(response, charset);
		response.close();
		client.close();
		if(HttpStatus.SC_OK != status.getStatusCode()){
			throw new HttpException(status.getStatusCode() + ":" + status.getReasonPhrase());
		}
		return result;
	}

	/**
	 * post调用（数据流发送）
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return String 请求结果
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPost(String url, Map<String, Object> headers, String params)
			throws URISyntaxException, ClientProtocolException, HttpException, IOException {
		return doPost(url, headers, params, CHARSET);
	}

	/**
	 * post调用
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @param charset 编码格式
	 * @return String 请求结果
	 * @throws URISyntaxException 异常
	 * @throws ClientProtocolException 异常
	 * @throws HttpException 异常
	 * @throws IOException 异常
	 */
	public static String doPost(String url, Map<String, Object> headers, String params, String charset)
			throws URISyntaxException, ClientProtocolException, HttpException, IOException {
		CloseableHttpClient client = buildHttpClient(false);
		Assert.notNull(url, "构建HttpPost时,url不能为null");
		HttpPost postMethod = buildHttpPost(url, headers, params, charset);
		postMethod.setConfig(buildRequestConfig());
		CloseableHttpResponse response = client.execute(postMethod);
		StatusLine status = response.getStatusLine();
		String result = processResponse(response, charset);
		response.close();
		client.close();
		if(HttpStatus.SC_OK != status.getStatusCode()){
			throw new HttpException(String.format("%s %s %s", status.getProtocolVersion().toString(), status.getStatusCode(), status.getReasonPhrase()));
		}
		return result;
	}
	
	
	/*********************************** https post ******************************************/
	/**
	 * post调用 https
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return String
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPostSSL(String url, Map<String, Object> params)
			throws URISyntaxException, ClientProtocolException, IOException {
		return doPostSSL(url, params, CHARSET);
	}

	/**
	 * post调用 https
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @param charset 编码格式
	 * @return String 请求结果
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPostSSL(String url, Map<String, Object> params, String charset)
			throws URISyntaxException, ClientProtocolException, IOException {
		CloseableHttpClient client = buildHttpsClient(false);
		HttpPost postMethod = buildHttpPost(url, null, params, charset);
		CloseableHttpResponse response = client.execute(postMethod);
		String result = processResponse(response, charset);
		response.close();
		client.close();
		return result;
	}

	/**
	 * post调用（数据流发送）
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @return String 请求结果
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPostSSL(String url, String params)
			throws URISyntaxException, ClientProtocolException, IOException {
		return doPostSSL(url, params, CHARSET);
	}

	/**
	 * post调用 https
	 * 
	 * @param url 请求地址
	 * @param params 参数
	 * @param charset 编码格式
	 * @return String 请求结果
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPostSSL(String url, String params, String charset)
			throws URISyntaxException, ClientProtocolException, IOException {
		CloseableHttpClient client = buildHttpsClient(false);
		Assert.notNull(url, "构建HttpPost时,url不能为null");
		HttpPost postMethod = buildHttpPost(url, null, params, charset);
		postMethod.setConfig(buildRequestConfig());
		CloseableHttpResponse response = client.execute(postMethod);
		String result = processResponse(response, charset);
		response.close();
		client.close();
		return result;
	}

	public static String doPostSSL(String url, Map<String, Object> headers, String params, String charset)
			throws URISyntaxException, ClientProtocolException, IOException {
		CloseableHttpClient client = buildHttpsClient(false);
		Assert.notNull(url, "构建HttpPost时,url不能为null");
		HttpPost postMethod = buildHttpPost(url, headers, params, charset);
		postMethod.setConfig(buildRequestConfig());
		CloseableHttpResponse response = client.execute(postMethod);
		String result = processResponse(response, charset);
		response.close();
		client.close();
		return result;
	}
	
	/*********************************** https post end******************************************/

	/**
	 * 拼接url，将参数拼接到请求地址后面
	 * 
	 * @param url 请求地址
	 * @param params 请求参数
	 * @return String
	 */
	private static String buildGetUrl(String url, Map<String, Object> params) {
		StringBuffer uriStr = new StringBuffer(url);
		if (params != null) {
			List<NameValuePair> ps = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				ps.add(new BasicNameValuePair(key, params.get(key) + ""));
			}
			uriStr.append("?");
			uriStr.append(URLEncodedUtils.format(ps, CHARSET));
		}
		return uriStr.toString();
	}
	
}

package com.sengsational.fop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import kotlin.Pair;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FirstGetOptum {

	//============================= URL PARAMETERS ================================================================

	public static final ArrayList<Pair<String, String>> URL_PARAM_LIST = new ArrayList<>();
	{
		URL_PARAM_LIST.add(new Pair<>("response_type", "code"));
		URL_PARAM_LIST.add(new Pair<>("client_id", "55796a71-8104-4625-b259-bb91e9f13a60"));
		URL_PARAM_LIST.add(new Pair<>("state", "0124"));
		URL_PARAM_LIST.add(new Pair<>("scope", "patient/Patient.read"));
		URL_PARAM_LIST.add(new Pair<>("redirect_uri", "https://sites.google.com/sengsational.com/privacy/privacypolicy"));
		URL_PARAM_LIST.add(new Pair<>("code_challenge", "s6kElxScJMXGilr1VTwZYsjlq5XexWCUn94rmO7Y29o")); // optionally replaced in main()
		URL_PARAM_LIST.add(new Pair<>("code_challenge_method", "S256"));
	}
	
	//============================= HEADERS ================================================================
	
	public static final ArrayList<Pair<String, String>> HEADER_LIST = new ArrayList<>();
	{
		HEADER_LIST.add(new Pair<>("Upgrade-Insecure-Requests", "1"));
		HEADER_LIST.add(new Pair<>("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) PostmanCanary/11.2.14-canary240621-0734 Electron/20.3.11 Safari/537.36"));
		HEADER_LIST.add(new Pair<>("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"));
		HEADER_LIST.add(new Pair<>("Sec-Fetch-Site", "none"));
		HEADER_LIST.add(new Pair<>("Sec-Fetch-Mode", "navigate"));
		HEADER_LIST.add(new Pair<>("Sec-Fetch-User", "?1"));
		HEADER_LIST.add(new Pair<>("Sec-Fetch-Dest", "document"));
		HEADER_LIST.add(new Pair<>("Accept-Encoding", "gzip, deflate, br"));
		HEADER_LIST.add(new Pair<>("Accept-Language", "en-US"));
	}

	private static String generateCodeChallenge() throws Exception {
		// Construct the code challenge url parameter
		StringBuilder sb = new StringBuilder ();
		String characters = "01234567890abcde";
		Random random = new Random ();
		for (int i = 0; i < 56; i ++) {
			sb.append (characters.charAt (random.nextInt (characters.length ())));
		}
		String randomText = sb.toString();
		
		// Temporarily override the random text with the same text each time
		randomText = "6b890b254542c9de4603278153e1b127d21730d46ac2620e6e35514c";

		byte[] binaryData = null;
	    try {
	        binaryData = MessageDigest.getInstance("SHA-256").digest(randomText.getBytes(StandardCharsets.UTF_8));
	    } catch (NoSuchAlgorithmException e) {
	        throw new Exception("Failed SHA-256");
	    }
		
	    Base64.Encoder encoder = Base64.getUrlEncoder();
	    String codeChallenge = encoder.encodeToString(binaryData);
	    codeChallenge =  codeChallenge.replaceAll("=", ""); // remove pad
	    return codeChallenge;
	}
	

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("unused")
		FirstGetOptum fgo = new FirstGetOptum(); // for static initializers
		
		// Use this boolean to run the test or the actual call
		HttpUrl.Builder urlBuilder = null;
		boolean getHttpBinDump = false;
		if (getHttpBinDump) {
			urlBuilder = HttpUrl.parse("https://www.httpbin.org/get").newBuilder();
		} else {
			urlBuilder = HttpUrl.parse("https://sandbox.authz.flex.optum.com/oauth/authorize").newBuilder();
		}
		
		for (Pair<String, String> pair : URL_PARAM_LIST) {
			urlBuilder.addQueryParameter(pair.getFirst(), pair.getSecond());
		}
		
		boolean replaceCodeChallengeValue = true;
		if (replaceCodeChallengeValue) {
			String codeChallenge = generateCodeChallenge();
			System.out.println("generated codeChallenge [" + codeChallenge + "]");
			urlBuilder.setQueryParameter("code_challenge", codeChallenge);
			urlBuilder.setQueryParameter("code_challenge_method", "S256");
		}

		String url = urlBuilder.build().toString();
		
	    System.out.println("Constructed the URL: [" + url + "]");

	    Request.Builder requestBuilder = new Request.Builder();
	    requestBuilder.url(url);
	    
	    for (Pair<String, String> pair : HEADER_LIST) {
	    	requestBuilder.addHeader(pair.getFirst(), pair.getSecond());
	    }

	    List<String> headerDebug = requestBuilder.getHeaders$okhttp().getNamesAndValues$okhttp();
	    for (int i = 0; i < headerDebug.size(); i = i+2) {
	    	System.out.println("Header item: " + headerDebug.get(i) + ":" + headerDebug.get(i + 1));
	    }
	    
	    Request request = requestBuilder.build();
	    
	    OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
	    okHttpBuilder.followRedirects(false); // Essential or server error occurs [ https://stackoverflow.com/a/78955987/897007 ]

		OkHttpClient client = okHttpBuilder.build();

	    Call call = client.newCall(request);
	    
	    Response response = call.execute();

	    System.out.println("response " + response.code() + " (should be 302)");
		    
	    System.out.println("response body\n" + response.body().string());

	}
	
	class HeaderInterceptor implements Interceptor {
		private String mVariableValue;
		private String mVariableName;

	    public HeaderInterceptor(String variableName, String variableValue) {
	        mVariableName = variableName;
	        mVariableValue = variableValue;
	    }

	    @Override
	    public Response intercept(Chain chain) throws IOException {
	        Request request = chain.request()
	                .newBuilder()
	                .header(mVariableName, mVariableValue)
	                .build();
	        return chain.proceed(request);
	    }
	}
}

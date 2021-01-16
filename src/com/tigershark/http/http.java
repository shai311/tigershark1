package com.tigershark.http;

import okhttp3.*;
import java.io.*;
import com.google.gson.*;
import java.util.concurrent.*;
import java.util.*;
import java.net.URI;
import java.nio.file.Paths;
import java.net.*;


public class http
{

	static PrintWriter printer = new PrintWriter(System.out,true);

	public JsonObject get(String url) {
		String entity = new String("entity");
		String ct = new String("ct");

		try {
			OkHttpClient client = new OkHttpClient();
		  	Request request = new Request.Builder()
				.url(url)
				.build();

			
			Response response = client.newCall(request).execute();
			ct = response.headers().get("Content-Type");
			entity = response.body().string();
			
		}

		catch (Exception E) {
			
			new PrintWriter(System.out,true).printf("request " + url + " failed");
			E.printStackTrace();
		}
		finally {
		
		}


		log(entity);

		if (ct.equals("text/html")) {
			JsonObject result = new JsonObject();
			result.addProperty("html", entity);
			return result ;
		}

		
		JsonParser parser = new JsonParser();
		JsonObject object = (JsonObject) parser.parse(entity);
		return object;
	}

	public JsonObject download(String url, String path) {
		String headers = new String("headers");
		String filename = new String("filename");

		try {
			OkHttpClient client = new OkHttpClient();
	  		Request request = new Request.Builder()
				.url(url)
				.build();

			Response response = client.newCall(request).execute();
			List<String> hnames = new LinkedList(response.headers().names());
			InputStream istream = response.body().byteStream();
			headers = response.headers().toString();
			int cl = Integer.valueOf(response.headers().get("Content-Length"));
			filename = Paths.get(new URI(url).getPath()).getFileName().toString();


			FileOutputStream fos = new FileOutputStream(new File(new File(path), filename));

			byte[] data = new byte[1024];
			int count = 0;
			float read = 0;
			double lp = 0;

 			while ((count = istream.read(data)) != -1) {
				read = read + count;
				double cp = Math.floor( ((read / (float) cl) * 100 ) );
				fos.write(data,0,count);

				if (cp > lp){
					printer.printf("Downloaded %d %%\n", (int)cp);
				}
				  
				lp = cp;
			}

			fos.flush();
			fos.close();
			istream.close();
		}

		catch (IOException E) {
			new PrintWriter(System.out, true).println(url);
			E.printStackTrace();
		}
		catch (URISyntaxException R) {
			R.printStackTrace();
		}
		finally {

		}

		printer.println();
		printer.println();


		return new JsonObject();
	}


	public JsonObject upload(String url, File F) {
		String entity = new String("entity");
		String headers = new String("headers");
		long flength = F.length();
		
		RequestBody requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("filename", F.getName(), RequestBody.create(MediaType.parse("application/mp3"), F))
        .build();


		RequestBody rbody = new progress(requestBody,
			
			new progress.Listener() {
				int cl=(int) F.length();
				double cp=0;
				double lp=0;

				public void onProgress(int progress) {	
					cp = Math.floor( ((progress / (float) cl) * 100 ) );

					if (cp > lp){
						printer.printf("Uploaded %d %%\n", (int) cp);
					}
					
					lp = cp;	
				}
				
			}
		
		);

		try {
			OkHttpClient client = new OkHttpClient().newBuilder()
			.readTimeout(5,TimeUnit.MINUTES)
			.connectTimeout(5,TimeUnit.MINUTES)
			.writeTimeout(5, TimeUnit.MINUTES)
			.build();

	  	Request request = new Request.Builder()
			.url(url)
			.addHeader("accept", "*.*")
			.addHeader("Content-Length", String.valueOf(flength))
			.post(rbody)
			.build();


			Response response = client.newCall(request).execute();
			headers = response.headers().toString();
			entity = response.body().string();
		}

		catch (IOException E) {
			E.printStackTrace();
		}
		finally {

		}

		JsonParser parser = new JsonParser();
		JsonObject object = (JsonObject) parser.parse(entity);
		printer.println();
		printer.println();


		return object;
	}


	public void log(String entity) {
		printer.printf("%s\n", entity);
	}


	public static void main(String[] args) {
			//new http().upload("https://api.pcloud.com/uploadfile?password=gooseman&username=eric.tekcities@gmail.com&folderid=0", new File("ballet.mp4"));
			//new http().download("http://aspen.125mb.com/videos/ballet.mp4", ".");
			new http().get("https://www.azlyrics.com/lyrics/snowpatrol/openyoureyes.html");
	}

}

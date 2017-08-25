package com.mantono.ghapic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Run
{

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		final Client client = new Client(new CacheSettings());
		
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(is);
		
		String line = br.readLine();
		
		while(line.length() > 0 && !line.equals("exit"))
		{
			Resource request = new Resource(Verb.GET, line);
			Future<Response> resp = client.submitRequest(request);
			
			while(!resp.isDone())
				Thread.yield();
			
			Response response = resp.get();
			
			System.out.println(response.getHeader());
			System.out.println(response.getBody());
			
			line = br.readLine();
		}
		
		is.close();
		
		System.exit(0);
	}

}

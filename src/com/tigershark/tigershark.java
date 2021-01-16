package com.tigershark;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.*;
import com.tigershark.http.*;

// tigershark asynchronious http chaining class
//---------------------------------------------------------------------------//
public class tigershark {
	protected static tigershark instance;
	protected static PrintWriter stdout = new PrintWriter(System.out,true);
	protected static hThread[] hThreads = new hThread[811];
	protected static Output[] outputs = new Output[811];
	protected static LinkedBlockingQueue<action> https = new LinkedBlockingQueue<action>();
	protected static Object SingletonInitLock = new Object();


 public static tigershark getInstance() {
		synchronized(SingletonInitLock) {
			if (instance == null) {
				instance = new tigershark();
			}
		}

		return instance;
 }

//---------------------------------------------------------------------------//
 public tigershark() {
		new Thread(new Orchestrator()).start();
 }

 class Orchestrator implements Runnable {
	 public void run() {
		 	hThread newThread = null;
			action theaction = null;

			while (true) {
				try  {
					theaction = https.take();
		 		}
				catch (InterruptedException e) {
					e.printStackTrace(); 
				}
				

				newThread = new hThread(new GetloadWorker(theaction));
				newThread.setUniqueIdentifier(theaction.getUniqueIdentifier());
				newThread.setUniqueSequence(theaction.getSequenceNumber());

				int availablePlot = findAnAvailablePlot();
				hThreads[availablePlot] = newThread;
				hThreads[availablePlot].start();
		  }
	 }
 }




//---------------------------------------------------------------------------//
 public void offer(action newaction) {
	List<action> Actions = newaction.toList();

	for (action actionpart : Actions) {
		https.offer(actionpart);
	}
 }



//---------------------------------------------------------------------------//
 class GetloadWorker implements Runnable {
 	action command;

 	public GetloadWorker(action command) {
 		this.command = command;
 	}

 	public void run() {
 		int identifier = command.getUniqueIdentifier();
 		List<hThread> prequisites = findCommandPrequisitesdByUniqueIdentifier(identifier);
 		String params = new String("");

 		for (hThread hthread : prequisites) {
 			try {
 				if (command.getSequenceNumber() == (hthread.getSequenceNumber() + 1)) {
 					new PrintWriter(System.out,true).printf("Thread %s waiting for thread %s\n", Thread.currentThread(), hthread);
 					hthread.join();

 					Output output = ((hThread) hthread).getOutput() ;

					if (!(output.isEmpty())) {
 						params += output.glueQueryParams();
 						System.out.println("Found output params " + params);
				  }
 				}
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}

		stdout.println(command);

		JsonObject response = null;
		
		if (command instanceof get){
			if (command.getPath() == null )
				response = new http().get(command.getUrl() + params);
			else
				response = new http().download(command.getUrl(), command.getPath());
		}
		else if (command instanceof post){
			response = new http().upload(command.getUrl(), command.getFile());
		}
		else {
			throw new failure();
		}

		JsonObject json = new JsonObject();


		if (command.hasListener()) {
			json = command.getListener().onComplete(response);
		}
		else {
			json = new JsonObject();
		}

		// Generate new commands
		if (command.hasListener()) {
			if (json.has("actions")) {
		 		String links = json.get("actions").getAsString();

		 		if (!(links == null)) {
		 			new PrintWriter(System.out,true).println("Sending new anonymous http actions to tigershark " + links);
		 			offer(new get(links, "."));
		 		}
		 		else {
		 			new PrintWriter(System.out,true).println("No anonymous http actions to send");
		 		}
		  }
		}


		// Set output for all commands
 		Output output = new Output();
 		output.setUniqueIdentifier(command.getUniqueIdentifier());
		output.setJsonObject(json);

 		hThread running = ((hThread) Thread.currentThread());
 		running.setOutput(output);

 	}
 }


// --------------------------------------------------------------------------//
 class hThread extends Thread {
	 protected int id;
	 protected int seq;
	 protected Output out;

	 public hThread(Runnable R) {
		 super(R);
	 }

	 public void setUniqueIdentifier(int id) {
		 this.id = id;
	 }

	 public void setUniqueSequence(int seq) {
		 this.seq = seq;
	 }

	 public int getUniqueIdentifier() {
		 return this.id;
	 }

	 public int getSequenceNumber() {
		 return this.seq;
	 }

	 public void setOutput(Output out) {
		 this.out = out;
	 }

	 public Output getOutput() {
		 	return this.out;
	 }

	 public String toString() {
		 String result = "hThread " + id + "-" + seq;
		 return result;
	 }
 }

	public static interface Listener {

			public JsonObject onComplete(JsonObject response);

	}

	public static interface ActionInitiative {

			public JsonObject onComplete(JsonObject response);

	}

	static class Output {
		protected Map<String,String> hashmap = new HashMap();
		protected Map<String,String> actionmap = new HashMap();
		protected JsonObject json = new JsonObject();
		protected int id;

		public void setJsonObject(JsonObject json) {
			this.json = json;
		}

		public JsonObject getJsonObject() {
				return this.json;
		}

		public Map getHashMap() {
			return this.hashmap;
		}

		public void setHashMap(Map<String,String> map) {
			this.hashmap = map;
		}

		public Map<String,String> getActionMap() {
			return this.actionmap;
		}

		public void setActionMap(Map<String,String> map) {
			this.actionmap = map;
		}

		public int getUniqueIdentifier() {
			return this.id;
		}

		public void setUniqueIdentifier(int id) {
			this.id = id;
		}

		public boolean isEmpty() {
			return ( this.json.entrySet().size() == 0 );
		}

		public String glueQueryParams() {
			StringBuilder builder = new StringBuilder();

			Iterator entries = this.json.entrySet().iterator();

			if (!entries.hasNext()) return "";

			builder.append("&");
			System.out.println("Glueing map of size " + this.hashmap.get("fileid"));

			int i = 0;
			for (Map.Entry<String,JsonElement> entry : this.json.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().getAsString();
				builder.append(key + "=" + value);

				if (i <   (this.json.entrySet().size()-1) ) {
					builder.append("&");
				}

				i++;
			}


			//builder.append("1=1");

			return builder.toString();
		}
	}

//---------------------------------------------------------------------------//
	public List<hThread> findCommandPrequisitesdByUniqueIdentifier(int uid) {
		LinkedList<hThread> result = new LinkedList<hThread>();

		for (int i=0; i < hThreads.length; i++) {
			hThread hthread = hThreads[i];

			if (hthread == null)
			continue;

			if (uid == hthread.getUniqueIdentifier()) {
					result.add(hthread);
			}
		}

		return result;
	}


	// pending
	public Output findOutputByUniqueIdentifier(int uid) {
		Output result = null;

		for (int i=0; i < outputs.length; i++) {
			Output output = outputs[i];

			if (output.getUniqueIdentifier() == uid) {
				result = output;
				break;
			}
		}

		return result;
	}

//---------------------------------------------------------------------------//
	public static int findAnAvailablePlot() {
		int index = Integer.MIN_VALUE;

		for (int i=0; i < hThreads.length; i++) {
			Thread T = hThreads[i];

			if (T == null) {
				index = i;
				break;
			}

			if (!(T.isAlive())) {
				index = i;
				break;
			}
		}

		if (index == Integer.MIN_VALUE)
			throw new failure("No available plot found");

		return index;
	}

//---------------------------------------------------------------------------//
	public static void main(String[] args) {
		tigershark sabertooth = new tigershark();
		sabertooth.offer( new get("http://aspen.125mb.com/videos/ballet.mp4", "."));
	}
//---------------------------------------------------------------------------//
}

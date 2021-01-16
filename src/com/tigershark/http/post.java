package com.tigershark.http;

import com.tigershark.tigershark;

import java.io.*;
import java.util.*;

public class post implements action {
	protected File F;
	protected String url;
	protected tigershark.Listener listener;
	protected int id;
	protected int seq;

	public post(String url, File F) {
		this.url = url;
		this.F = F;
	}


	public File getFile() {
		return F;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getParams() {
		return null;
	}

	public String getPath() {
		return null;
	}

	public int getUniqueIdentifier() {
		return this.id;
	}

	public void setUniqueIdentifier(int id) {
		this.id = id;
	}

	public int getSequenceNumber() {
		return this.seq;
	}

	public void setSequenceNumber(int seq) {
		this.seq = seq;
	}

	public List<action> toList() {
		LinkedList<action> result = new LinkedList();
		result.add(this);
		return result;
	}

	public boolean hasListener() {
		return (!(listener == null));
	}

	public tigershark.Listener getListener() {
		return this.listener;
	}

	public String toString() {
		return "POST " + this.url;
	}

}

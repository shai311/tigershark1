package com.tigershark.http;

import com.tigershark.tigershark;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;

public class get implements action {
	protected String url;
	protected String path;
	protected tigershark.Listener listener;
	protected tigershark.ActionInitiative initiative;
	protected List<action> commands = new LinkedList();
	protected static AtomicInteger ids = new AtomicInteger(0);
	protected int id = 0;
	protected int seq = 0;

	public get(String url) {
		this.url = url;
		this.id = getNextId();
		this.commands.add(this);
	}

	public get(String url, String path) {
		this(url);
		this.path = path;
	}

	public get(String url, tigershark.Listener listener) {
		this.listener = listener;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getParams() {
		return null;
	}

	public String getPath() {
		return path;
	}

	public File getFile() {
		return null;
	}

	public tigershark.Listener getListener() {
		return this.listener;
	}

	public tigershark.ActionInitiative getActionInitiative() {
		return this.initiative;
	}

	public boolean hasListener() {
			return (!(listener == null));
	}

	public boolean hasActionInitiative() {
			return (!(initiative == null));
	}

	public void addListener(tigershark.Listener listener) {
		this.listener = listener;
	}

	public void addActionInitiative(tigershark.ActionInitiative initiative) {
		this.initiative = initiative;
	}

	public get to(get command) {
		if (this.commands == null || this.commands.isEmpty()) {
			this.commands = new LinkedList<action>();
			this.commands.add(this);
		}

		action tail = ((LinkedList<action>) commands).peekLast();
		command.setUniqueIdentifier(tail.getUniqueIdentifier());
		command.setSequenceNumber(tail.getSequenceNumber() + 1);
		((LinkedList) commands).offerLast(command);
		return this;
	}

	public List<action> toList() {
		return commands;
	}

	public synchronized int getNextId() {
		int result = this.ids.getAndIncrement();
		return result;
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

	public String getId() {
		return this.id + "-" + this.seq + ":" + this.url;
	}

	public String toString() {
		String result = "GET " + this.url;
		return result;
	}
}

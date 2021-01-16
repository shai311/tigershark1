package com.tigershark.http;

import com.tigershark.tigershark;

import java.util.*;
import java.io.*;

public interface action {

	public String getUrl();
	public Map<String, String> getParams();
	public int getUniqueIdentifier();
	public int getSequenceNumber();
	public String getPath();
	public File getFile();
	public boolean hasListener();
	public tigershark.Listener getListener();
	public List<action> toList();

}

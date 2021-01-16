package com.tigershark.http;

public class failure extends RuntimeException {


	public failure() {
		super();
	}

	public failure(String msg) {
		super(msg);
	}

	public failure(Exception e) {
		super(e);
	}

}

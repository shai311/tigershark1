package com.tigershark;

import com.tigershark.http.post;

import java.io.*;

class tigershark1 extends tigershark {
//---------------------------------------------------------------------------//
	public static void main(String[] args) {
		tigershark sabertooth = new tigershark1();
		sabertooth.offer(
		new post("https://api.pcloud.com/uploadfile?password=coffee4me&username=shaigtz@gmail.com&folderid=0",
		new File(new File("."), "ballet.mp4")));
	}
//---------------------------------------------------------------------------//
}

package com.tigershark;

import com.tigershark.http.get;

class tigershark2 extends tigershark {
//---------------------------------------------------------------------------//
	public static void main(String[] args) {
		tigershark sabertooth = new tigershark2();
		sabertooth.offer(
		new get("https://api.pcloud.com/listfolder?password=coffee4me&username=shaigtz@gmail.com&getauth=1&folderid=0"));
	}
//---------------------------------------------------------------------------//
}

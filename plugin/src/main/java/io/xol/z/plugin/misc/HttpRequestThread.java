//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.misc;

// (c) 2014 XolioWare Interactive

public class HttpRequestThread extends Thread {
	HttpRequester requester;
	String info;
	String address;
	String params;

	public HttpRequestThread(HttpRequester requester, String info, String address, String params) {
		this.requester = requester;
		this.info = info;
		this.address = address;
		this.params = params;
		this.setName("Http Request Thread (" + info + "/" + address + ")");
	}

	public void run() {
		String result = HttpRequests.sendPost(address, params);
		if (result == null)
			result = "null";
		requester.handleHttpRequest(info, result);
	}
}
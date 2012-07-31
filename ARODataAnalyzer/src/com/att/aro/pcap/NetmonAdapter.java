/*
 Copyright [2012] [AT&T]
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.att.aro.pcap;

import java.io.File;
import java.io.IOException;

/**
 * 
 *
 */
public class NetmonAdapter {

	/**
	 * Microsoft Network Monitor related error.
	 */
	public static final int NETMON_ERROR = -100;

	/**
	 * Indicates that Microsoft Network Monitor trace file could not be loaded.
	 */
	public static final int NETMON_TRACE_FILE_LOAD_ERROR = -101;

	/**
	 * Indicates that parsing successful with Microsoft Network Monitor API.
	 */
	public static final int NETMON_PARSING_SUCCESS = 0;

	private PacketListener pl;

	public NetmonAdapter(File file, final PacketListener pl) throws IOException {
		if (pl == null) {
			throw new IllegalArgumentException("PacketListener cannot be null");
		}

		this.pl = pl;
		int retval = parseTrace(file.getAbsolutePath());
		switch (retval) {
		case NETMON_PARSING_SUCCESS:
			return;
		case NETMON_TRACE_FILE_LOAD_ERROR:
		case NETMON_ERROR:
		default:
			throw new IOException("NetMon error code: " + retval);
		}
	}
	
	/**
	 * Executes all packets from the Microsoft Network Monitor cap files.
	 * 
	 * @param filename
	 *            cap file name.
	 * @return result non zero if any error occurs while looping, else success
	 *         message.
	 */
	private native int parseTrace(String filename);

	/**
	 * Callback listener used by the native code that accesses pcap
	 * 
	 * @param datalink
	 * @param seconds
	 * @param microSeconds
	 * @param len
	 * @param data
	 */
	private void pcapHandler(int datalink, long seconds, long microSeconds,
			int len, byte[] data) {

		// Ignore netmon datalink type frames
		if (datalink >= 0xf000) {
			return;
		}

		pl.packetArrived(Packet.createPacketFromNetmon(datalink, seconds, microSeconds,
				len, data));
	}

	static {
		String os = System.getProperty("os.arch");
		if (os != null && os.contains("64")) {
			System.loadLibrary("NMCap64");
		} else {
			System.loadLibrary("NMCap");
		}
	}
}

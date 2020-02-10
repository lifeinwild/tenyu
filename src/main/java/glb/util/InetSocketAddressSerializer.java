package glb.util;

import java.net.*;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;

public class InetSocketAddressSerializer extends Serializer<InetSocketAddress> {

	@Override
	public void write(Kryo kryo, Output output, InetSocketAddress obj) {
		output.writeString(obj.getHostName());
		output.writeInt(obj.getPort(), true);
	}

	@Override
	public InetSocketAddress read(Kryo kryo, Input input,
			Class<? extends InetSocketAddress> klass) {
		String host = input.readString();
		int port = input.readInt(true);
		return new InetSocketAddress(host, port);
	}
}

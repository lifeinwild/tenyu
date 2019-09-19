package bei7473p5254d69jcuat.tenyu.release1.communication.request;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import io.netty.channel.*;

public class FileDownload extends P2PEdgeCommonKeyRequest {
	private long start;
	private long end = -1;
	private String path;

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		//for security
		if (path.contains("..")) {
			return false;
		}
		if (!path.startsWith(Glb.getFile().getModelDir())) {
			return false;
		}

		return true;
	}


	public long getEnd() {
		return end;
	}

	public long getStart() {
		return start;
	}

	public String getPath() {
		return path;
	}

	@Override
	public boolean isValid(Response res) {
		if (!(res instanceof FileDownloadResponse))
			return false;
		return true;
	}

	public static class FileDownloadResponse extends P2PEdgeCommonKeyResponse {

		@Override
		public boolean isValid(Request req) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(Message m) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}

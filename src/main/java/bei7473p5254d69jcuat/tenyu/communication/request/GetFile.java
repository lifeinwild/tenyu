package bei7473p5254d69jcuat.tenyu.communication.request;

import java.nio.file.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.netty.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * 近傍からファイルを取得する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetFile extends P2PEdgeCommonKeyRequest {
	private long position;
	private long size;
	private TenyuFile file;

	@Override
	protected boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		if (file == null
				|| file.getRelativePathStr()
						.length() > ChunkedDataConcatFile.fileNameLenMax
				|| !file.validateAtCreate(new ValidationResult())) {
			return false;
		}
		if (position < 0) {
			return false;
		}
		if (size <= 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(Response res) {
		Glb.getLogger().error("invalid response", new Exception());
		return false;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		//ファイル返信
		Path p = file.getRelativePath();
		if (p == null || !p.toFile().isFile()) {
			return false;
		}

		if (!file.isWritten(position, size)) {
			Glb.getLogger().warn("Invalid GetFile=" + this);
			return false;
		}

		return Glb.getP2p().responseFile(p, position, size, ctx) != null;
	}

	@Override
	public String toString() {
		return "file=" + file + " position=" + position + " size=" + size;
	}

	/**
	 * ファイルが返信された時、そのパスを通知する
	 * このオブジェクトは受信側で作成される。返信側は作成しない。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	/*	没案
	public static class ResopnseFile extends P2PEdgeCommonKeyResponse {
		private Path responsedFile;

		@Override
		protected boolean validateP2PEdgeCommonKeyResponseConcrete(Message m) {
			return responsedFile != null;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetFile;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;
		}

		public Path getResponsedFile() {
			return responsedFile;
		}

		public void setResponsedFile(Path responsedFile) {
			this.responsedFile = responsedFile;
		}

	}
	*/
	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public TenyuFile getFile() {
		return file;
	}

	public void setFile(TenyuFile file) {
		this.file = file;
	}

}

package bei7473p5254d69jcuat.tenyu.communication.request;

import java.nio.file.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyutalk.file.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * 近傍にファイルを持っているか問い合わせる
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class HasFile extends P2PEdgeCommonKeyRequest {
	private TenyutalkFileMetadataI file;

	@Override
	public boolean isValid(Response res) {
		return res instanceof HasFileResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		P2PEdge e = validated.getEdgeByInnermostPackage();
		HasFileResponse res = new HasFileResponse();
		Path writeBits = file.getWriteBitsPath();
		if (file.getRelativePath().toFile().exists()
				&& writeBits.toFile().exists()) {
			try {
				res.setWriteBits(Files.readAllBytes(writeBits));
			} catch (Exception ex) {
				Glb.getLogger()
						.warn("Failed to read writeBits path=" + writeBits, ex);
				res.setWriteBits(null);
			}
		} else {
			res.setWriteBits(null);
		}
		Message resM = Message.build(res).packaging(res.createPackage(e))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public void setFile(TenyutalkFileMetadataI file) {
		this.file = file;
	}

	@Override
	protected boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		if (file == null || !file.validateAtCreate(new ValidationResult()))
			return false;
		return true;
	}

	public static class HasFileResponse extends P2PEdgeCommonKeyResponse {
		/**
		 * ファイルのどの部分を持っているか
		 * nullはそのファイルを全く持っていない事を意味する
		 */
		private byte[] writeBits;
		public static final int writeBitsMax = 1000 * 1000;

		@Override
		public boolean isValid(Request req) {
			return req instanceof HasFile;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;
		}

		@Override
		protected boolean validateP2PEdgeCommonKeyResponseConcrete(Message m) {
			if (writeBits != null && writeBits.length > writeBitsMax)
				return false;
			return true;
		}

		public byte[] getWriteBits() {
			return writeBits;
		}

		public void setWriteBits(byte[] writeBits) {
			this.writeBits = writeBits;
		}

	}

}

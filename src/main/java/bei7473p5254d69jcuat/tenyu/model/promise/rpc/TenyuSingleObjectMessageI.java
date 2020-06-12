package bei7473p5254d69jcuat.tenyu.model.promise.rpc;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;

/**
 * SOM
 *
 * localhostからのRPCを想定
 * HTTPで転送される想定
 *
 * 参照：rpc.md
 *
 * SOMプロトコルにおいて、各システム毎にこのようなクラスが作成される。
 * 他に{@link ValidatableI#validateAtRpc(glb.util.ValidationResult)}
 * のようなインターフェースも必要。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuSingleObjectMessageI {

	default String getURLPrefix() {
		return "http://" + Glb.getConst().getAppName().toLowerCase() + "/?som=";
	}

	default String getURL(String som) {
		return getURLPrefix() + som;
	}

	/**
	 * SOM構想に基づくURL
	 * 参照：rpc.md
	 * @return URL引数となるこのオブジェクトをシリアライズした文字列
	 */
	default String toStringSOM() {
		try {
			//TODO 本当はRisonを使うつもりだったがクラス名つきだと機能しないのでkryoをbase64URLにする
			//https://github.com/bazaarvoice/rison/issues/28
			//その他、RisonのURLは一部のアプリで機能しない。参照：rpc.md
			byte[] kryoBytes = Glb.getUtil().toKryoBytes(this,
					Glb.getKryoForRPC());
			return Base64.getUrlEncoder().encodeToString(kryoBytes);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return "";
		}
	}

	/**
	 * @param urlEncodedKryoBytesSOM	シリアライズされた文字列
	 * @return	デシリアライズされたオブジェクト
	 */
	public static TenyuSingleObjectMessageI deserialize(
			String urlEncodedKryoBytesSOM) {
		byte[] kryoBytes = Base64.getUrlDecoder()
				.decode(urlEncodedKryoBytesSOM);
		return (TenyuSingleObjectMessageI) Glb.getUtil()
				.fromKryoBytes(kryoBytes, Glb.getKryoForRPC());
	}

	/**
	 * 周辺検証のRPC版
	 *
	 * RPCに伴う様々な情報を総合した検証を行う。
	 * ただし{@link ValidatableI#validateAtRpc(ValidationResult)}で行われる検証をしない。
	 *
	 * @param m			SOM
	 * @param addr		リクエスト元アドレス
	 * @param r			検証結果
	 * @return	妥当か
	 */
	abstract public boolean validateAtRpcSynthetic(TenyuSingleObjectMessageI m,
			byte[] addr, ValidationResult r);

	/**
	 * 機能の内容
	 * @return	返値。Rison文字列になりリクエスト元に返される。null可
	 */
	abstract public Object rpc() throws Exception;
}

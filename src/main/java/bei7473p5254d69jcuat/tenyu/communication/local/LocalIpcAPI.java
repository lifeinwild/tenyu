package bei7473p5254d69jcuat.tenyu.communication.local;

import com.github.arteam.simplejsonrpc.core.annotation.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;

@JsonRpcService
public class LocalIpcAPI {
	//基本情報関係
	@JsonRpcMethod
	public Conf getConf() {
		return Glb.getConf();
	}

	@JsonRpcMethod
	public Const getConst() {
		return Glb.getConst();
	}

	@JsonRpcMethod
	public FileManagement getFile() {
		return Glb.getFile();
	}

	@JsonRpcMethod
	public Subjectivity getSubje() {
		return Glb.getSubje();
	}

	@JsonRpcMethod
	public P2PDefense getDefense() {
		return Glb.getP2pDefense();
	}

	@JsonRpcMethod
	public FlowComputationState getFlow() {
		return Glb.getFlow();
	}

	@JsonRpcMethod
	public Gui getGui() {
		return Glb.getGui();
	}

	//署名関係
	@JsonRpcMethod
	public String getMyStandardPublicKeyType() {
		return Glb.getConf().getKeys().getMyStandardKeyType().name();
	}

	@JsonRpcMethod
	public byte[] getMyPublicKeyBinary(
			@JsonRpcParam("keyType") String keyType) {
		return Glb.getConf().getKeys().getMyPublicKey(KeyType.valueOf(keyType))
				.getEncoded();
	}

	/**
	 * WebAPIを通じた署名であることを証明するための接頭辞。
	 * @return
	 */
	/*
	@JsonRpcMethod
	public String getSignTargetPrefix() {
		return "WEBAPI_SIGN_TARGET_PREFIX";
	}
	*/

	/* WebAPIで署名機能を提供するのは危険と判断した。
	@JsonRpcMethod
	public byte[] sign(@JsonRpcParam("nominal") String nominal,
			@JsonRpcParam("target") byte[] target)
			throws InvalidKeySpecException, NoSuchAlgorithmException,
			IOException {
		byte[] prefix = getSignTargetPrefix().getBytes();
		return Glb.getConf().sign(nominal, KeyType.PC,
				Glb.getUtil().concat(prefix, target));
	}
	*/

	//DB関係
	@JsonRpcMethod
	public User getUser(@JsonRpcParam("id") Long id) {
		return Glb.getObje().getUser(us -> us.get(id));
	}
}
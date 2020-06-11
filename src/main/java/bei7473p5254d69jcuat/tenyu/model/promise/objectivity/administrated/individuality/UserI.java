package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;

public interface UserI extends IndividualityObjectI, HasSocialityI{
	boolean equalsMobilePubKey(byte[] mobilePubKey);

	boolean equalsOfflinePubKey(byte[] offlinePubKey);

	boolean equalsPcPubKey(byte[] pcPubKey);

	byte[] getMobilePublicKey();

	byte[] getOfflinePublicKey();

	byte[] getPcPublicKey();

	boolean isMyKey(byte[] pubKey);

	//void setMobilePublicKey(byte[] mobilePublicKey);

	//void setOfflinePublicKey(byte[] offlinePublicKey);

	//void setPcPublicKey(byte[] pcPublicKey);

	Long getInviter();

	@Override
	default int getNameMax() {
		return 30;//ファイルパスに入るから短め
	}
}

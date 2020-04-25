package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality;

public interface UserI extends IndividualityObjectI{
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
}

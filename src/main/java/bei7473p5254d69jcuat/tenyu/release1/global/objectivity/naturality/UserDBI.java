package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

public interface UserDBI extends NaturalityDBI{
	public boolean equalsMobilePubKey(byte[] mobilePubKey);

	public boolean equalsOfflinePubKey(byte[] offlinePubKey);

	public boolean equalsPcPubKey(byte[] pcPubKey);

	public byte[] getMobilePublicKey();

	public byte[] getOfflinePublicKey();

	public byte[] getPcPublicKey();

	public boolean isMyKey(byte[] pubKey);

	public void setMobilePublicKey(byte[] mobilePublicKey);

	public void setOfflinePublicKey(byte[] offlinePublicKey);

	public void setPcPublicKey(byte[] pcPublicKey);

	public Long getInviter();
}

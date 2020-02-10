package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.nio.file.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;
import bei7473p5254d69jcuat.tenyutalk.ui.*;
import glb.*;
import glb.util.*;
import glb.util.Util.*;
import jetbrains.exodus.env.*;

/**
 * ファイル
 *
 * ファイルアップロード系機能はtenyutalkのみが持ち、基盤ソフトウェアは持たない。
 * もともとそのためにtenyutalkというソフトウェアが必要になった。
 *
 * GUIからファイルをアップロードしたとき、
 * まずオブジェクトが作成されオブジェクトのIDが確定し、
 * その後にファイルが設置される。
 * ファイルパスがIDに依存しているので。
 *
 * ファイルの{@link IndividualityObject#getName()}は重複可能。
 * ファイルパスとは異なる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyutalkFile extends CreativeObject implements TenyutalkFileDBI {

	public static final int signaturesMax = 1000;

	/**
	 * ファイル名の最大長
	 */
	public static final int filenameMax = 150;

	/**
	 * 1ファイルの最大サイズ
	 */
	public static final long maxSize = 1000L * 1000 * 1000 * 2;
	public static final int userLimitationMax = 2000;
	public static final int fileNameMax = 50;

	public static final int safeSignsMax = 200;

	public static int getFilenamemax() {
		return filenameMax;
	}

	public static long getMaxsize() {
		return maxSize;
	}

	public static int getSignaturesmax() {
		return signaturesMax;
	}

	/**
	 * @param lastName
	 * @return	このファイルが設置される相対ファイルパス
	 */
	public String generateRelativePath() {
		User uploader = Glb.getObje()
				.getUser(us -> us.get(getUploaderUserId()));
		if (uploader == null) {
			Glb.getLogger().warn("uploader User not found. uploaderUserId="
					+ getUploaderUserId());
			return null;
		}
		String s = Glb.getConst().getFileSeparator();
		return Glb.getFile().getTenyutalkFileDir() + s + uploader.getName() + s
				+ getName();
	}

	/**
	 * 作成日時
	 * 更新の中でも変化しない
	 */
	private long createDate;

	/**
	 * このファイルをキャッシュしているノード
	 * このファイルのハッシュ値を求める場合これを除外する必要がある。
	 */
	private List<NodeIdentifierUser> cacheNodes = new ArrayList<>();

	/**
	 * このファイルのハッシュ値
	 */
	private byte[] fileHash;

	/**
	 * このファイルのサイズ
	 */
	private long fileSize;

	/**
	 * コンテンツに違法性が無いことを証言する署名
	 *
	 * 児童ポルノの単純所持が禁止されているが
	 * Tenyutalkは近傍がアップロードしたファイルをキャッシュする仕組みがあり
	 * PCの所有者の意思によらずファイルを保持する危険性がある。
	 * そこでアップロードされたファイルは他者による安全性署名があって初めて拡散する
	 * という仕様にした。
	 * Tenyuではユーザーの信用度が数値化できるのである程度の防壁となると思われる。
	 */
	private List<NominalSignature> safeSigns = new ArrayList<>();

	public boolean addSafeSign(NominalSignature sign) {
		return safeSigns.add(sign);
	}

	/**
	 * このオブジェクトが参照しているファイルをDLする。
	 * @return	DLに成功したか、あるいは既にDL済みだったか。
	 * つまり対象ファイルが自身のファイルシステムにセットされたか。
	 */
	public boolean download() {
		return false;//TODO
	}

	/**
	 * @return	DL及び検証に成功したか
	 */
	public boolean downloadAndValidateFileHash() {
		if (!download()) {
			Glb.getLogger().warn("Failed to download. this=" + this);
			return false;
		}
		Path p = getRelativePath();
		if (p == null)
			return false;

		//ハッシュ値
		byte[] hash = Glb.getUtil().digestFile(p);

		return getFileHashWrapper().equals(hash);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyutalkFile other = (TenyutalkFile) obj;
		if (cacheNodes == null) {
			if (other.cacheNodes != null)
				return false;
		} else if (!cacheNodes.equals(other.cacheNodes))
			return false;
		if (createDate != other.createDate)
			return false;
		if (!Arrays.equals(fileHash, other.fileHash))
			return false;
		if (fileSize != other.fileSize)
			return false;
		if (safeSigns == null) {
			if (other.safeSigns != null)
				return false;
		} else if (!safeSigns.equals(other.safeSigns))
			return false;
		return true;
	}

	public List<NodeIdentifierUser> getCacheNodes() {
		return cacheNodes;
	}

	public long getSubmitDate() {
		return createDate;
	}

	public ByteArrayWrapper getFileHashWrapper() {
		return new ByteArrayWrapper(fileHash);
	}

	public long getFileSize() {
		return fileSize;
	}

	@Override
	public TenyutalkFileGui getGui(String guiName, String cssIdPrefix) {
		return new TenyutalkFileGui(guiName, cssIdPrefix);
	}

	/**
	 * ファイルの内容にアクセスしたい場合、
	 * このメソッドを呼ぶ前に{@link #download()}でtrueを確認すること。
	 *
	 * @return	このファイルが保存される相対パス
	 */
	public Path getRelativePath() {
		String n = getName();
		if (n == null) {
			Glb.getLogger().error("name is null", new IllegalStateException());
			return null;
		}
		String s = Glb.getConst().getFileSeparator();
		return Paths.get(Glb.getMiddle().getMe().getName() + s + n
				+ GeneralVersioning.delimiter + getVersion());
	}

	@Override
	public byte[] getSignTarget() {
		if (!downloadAndValidateFileHash())
			return null;
		return super.getSignTarget();
	}

	@Override
	public TenyutalkFileStore getStore(Transaction txn) {
		return new TenyutalkFileStore(txn);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((cacheNodes == null) ? 0 : cacheNodes.hashCode());
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result + Arrays.hashCode(fileHash);
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		result = prime * result
				+ ((safeSigns == null) ? 0 : safeSigns.hashCode());
		return result;
	}

	public void setCacheNodes(List<NodeIdentifierUser> cacheNodes) {
		this.cacheNodes = cacheNodes;
	}

	public void setSubmitDate(long createDate) {
		this.createDate = createDate;
	}

	public void setFileHash(byte[] fileHash) {
		this.fileHash = fileHash;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * コンテンツが安全と思われる場合に署名する
	 * @return	署名に成功したか
	 */
	public boolean signSafe() {
		return Glb.getUtil().sign(sign -> addSafeSign(sign),
				() -> getSignNominalSafe(), () -> getSignTarget());
	}

	@Override
	public String toString() {
		return "TenyutalkFile [createDate=" + createDate + ", cacheNodes="
				+ cacheNodes + ", fileHash=" + Arrays.toString(fileHash)
				+ ", fileSize=" + fileSize + ", safeSigns=" + safeSigns + "]";
	}

	public boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (fileHash == null) {
			r.add(Lang.TENYU_FILE_HASH, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (fileHash.length != Glb.getConst().getHashSize()) {
				r.add(Lang.TENYU_FILE_HASH, Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (fileSize < 0) {
			r.add(Lang.TENYU_FILE_SIZE, Lang.ERROR_TOO_LITTLE);
			b = false;
		} else {
			if (fileSize > maxSize) {
				r.add(Lang.TENYU_FILE_SIZE, Lang.ERROR_TOO_BIG);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateCreativeObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (NominalSignature e : safeSigns) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeCreativeObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof TenyutalkFile)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.getClass()=" + old.getClass());
			return false;
		}
		TenyutalkFile old2 = (TenyutalkFile) old;
		if (createDate != old2.getSubmitDate()) {
			r.add(Lang.TENYUTALK_FILE_CREATE_DATE, Lang.ERROR_NOT_EQUAL,
					"createDate=" + createDate + " old2.createDate="
							+ old2.getSubmitDate());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateCreativeObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (NominalSignature e : safeSigns) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (createDate <= 0) {
			r.add(Lang.TENYUTALK_FILE_CREATE_DATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}
		if (cacheNodes == null) {
			r.add(Lang.TENYUTALK_FILE_CACHE_NODES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (cacheNodes.size() > cacheMax) {
				r.add(Lang.TENYUTALK_FILE_CACHE_NODES, Lang.ERROR_TOO_MANY,
						"cacheNodes.size()=" + cacheNodes.size());
				b = false;
			}
		}
		if (fileHash == null) {
			r.add(Lang.TENYUTALK_FILE_FILE_HASH, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (fileHash.length != Glb.getConst().getHashSize()) {
				r.add(Lang.TENYUTALK_FILE_FILE_HASH, Lang.ERROR_INVALID,
						"fileHash.length=" + fileHash.length);
				b = false;
			}
		}
		if (safeSigns == null) {
			r.add(Lang.TENYUTALK_FILE_SAFE_SIGNS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (safeSigns.size() > safeSignsMax) {
				r.add(Lang.TENYUTALK_FILE_SAFE_SIGNS, Lang.ERROR_TOO_MANY,
						"safeSigns.size()=" + safeSigns.size());
				b = false;
			}
		}

		return b;
	}

	@Override
	public boolean validateReferenceCreativeObjectConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		for (NominalSignature sign : safeSigns) {
			if (!sign.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		for (NodeIdentifierUser u : cacheNodes) {
			if (!u.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameTenyutalk.TENYUTALK_FILE;
	}

}

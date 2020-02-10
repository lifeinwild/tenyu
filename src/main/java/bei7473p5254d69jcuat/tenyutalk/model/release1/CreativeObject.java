package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.other.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;
import bei7473p5254d69jcuat.tenyutalk.ui.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public abstract class CreativeObject extends IndividualityObject
		implements CreativeObjectDBI {
	/**
	 * 最大キャッシュノード数
	 */
	public static final int cacheMax = 1000 * 10;

	public static final int referredsMax = 1000;

	public static final int refersMax = 1000;
	public static final int activePublicationMax = 100;

	public static final int activePublicationMin = 0;

	public static final int readSignsMax = 2000;

	public static int getActivepublicationmax() {
		return activePublicationMax;
	}

	public static int getActivepublicationmin() {
		return activePublicationMin;
	}

	public static int getCachemax() {
		return cacheMax;
	}

	public static int getReferredsmax() {
		return referredsMax;
	}

	public static int getRefersmax() {
		return refersMax;
	}

	/**
	 * チャット
	 * バージョンアップに伴い引き継がれない
	 * しかしGUIにおいて過去バージョンのチャットを表示することは可能だろう
	 */
	private Chat chat = new Chat();

	/**
	 * 公開日時証明のための読み取った人達による電子署名一覧
	 * {@link CreativeObject}以下のクラスは
	 * そのコンテンツとなる本質的情報が更新されると別オブジェクトが作成されるので
	 * 一部情報が不変になっていて、そこに関して電子署名すると後々まで検証可能であり続ける。
	 * さらにVersioned以下のクラスは創作的成果を扱う場合が多いので
	 * 読み取り時等に署名が必要となる。
	 */
	private List<NominalSignature> readSigns = new ArrayList<>();

	/**
	 * 現在の最新バージョン
	 */
	private GeneralVersioning version = new GeneralVersioning();

	/**
	 * 他のコンテンツからこのコンテンツへのトラックバック
	 */
	private List<TenyutalkReferenceSecure<?>> referreds = new ArrayList<>();

	/**
	 * このコンテンツから他のコンテンツへのトラックバック
	 */
	private List<TenyutalkReferenceSecure<?>> refers = new ArrayList<>();

	/**
	 * 積極的公開度
	 * 高いとより積極的にP2Pネットワークに拡散、保持される。
	 * 0-100
	 * 通常50。
	 * アクセスされたら勝手に広まるので人気のあるファイルは50でいい。
	 */
	private int activePublication = 50;

	/**
	 * 公開日時証明か。
	 * trueならこのデータをリクエストする時に作者に電子署名つきメッセージを送信する。
	 * なおそれは善意あるノードのみが送ると想定すべきで、
	 * 悪意あるノードは送らない可能性がある。
	 * ただし既に１０００名以上が署名済みだったら送らなくていい。
	 */
	private boolean publicationTimestamp = false;

	/**
	 * このオブジェクトをアップロードしたユーザーのID
	 * {@link SelfHosting}なのでホストでもある
	 */
	private Long uploaderUserId;

	/**
	 * 公開範囲
	 * 読み取り制限
	 */
	private UserScope scope = new UserScope();

	/**
	 * アップロード者による電子署名
	 */
	private byte[] sign;

	/**
	 * 初代ID
	 * 初代オブジェクトはnull
	 *
	 * バージョンアップされていくから初代オブジェクトという概念がある
	 */
	private Long firstId;

	/**
	 * オブジェクトに電子署名を追加する。
	 * addとしているがListを持つか１件のみとするかは実装クラスの自由。
	 *
	 * @param sign	電子署名
	 * @return	追加に成功したか
	 */
	public boolean addReadSign(NominalSignature sign) {
		return readSigns.add(sign);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CreativeObject other = (CreativeObject) obj;
		if (activePublication != other.activePublication)
			return false;
		if (chat == null) {
			if (other.chat != null)
				return false;
		} else if (!chat.equals(other.chat))
			return false;
		if (firstId == null) {
			if (other.firstId != null)
				return false;
		} else if (!firstId.equals(other.firstId))
			return false;
		if (publicationTimestamp != other.publicationTimestamp)
			return false;
		if (readSigns == null) {
			if (other.readSigns != null)
				return false;
		} else if (!readSigns.equals(other.readSigns))
			return false;
		if (referreds == null) {
			if (other.referreds != null)
				return false;
		} else if (!referreds.equals(other.referreds))
			return false;
		if (refers == null) {
			if (other.refers != null)
				return false;
		} else if (!refers.equals(other.refers))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		if (!Arrays.equals(sign, other.sign))
			return false;
		if (uploaderUserId == null) {
			if (other.uploaderUserId != null)
				return false;
		} else if (!uploaderUserId.equals(other.uploaderUserId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public int getActivePublication() {
		return activePublication;
	}

	private List<Long> getAdministrators() {
		List<Long> r = new ArrayList<>();
		r.add(uploaderUserId);
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return getAdministrators();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministrators();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministrators();
	}

	/**
	 * @return	具象クラス名
	 */
	public String getConcreteClassName() {
		return this.getClass().getCanonicalName();
	}

	/**
	 * {@link CreativeObject}以下はこのオブジェクトを通じてDBを操作する
	 * @return	このオブジェクトを保存するストアのDB
	 */
	public Tenyutalk getDb(NodeIdentifierUser node) {
		return Glb.getTenyutalk(node);
	}

	/**
	 * @return	このオブジェクトのユーザーとしての情報を保存するストアのDB
	 */
	public Environment getDbCookie() {
		return Glb.getDb(Glb.getFile().getCookieDBPath());
	}

	public Long getFirstId() {
		return firstId;
	}

	abstract public CreativeObjectGui<?, ?, ?, ?, ?, ?> getGui(String guiName,
			String cssIdPrefix);

	public List<TenyutalkReferenceSecure<?>> getReferreds() {
		return referreds;
	}

	public List<TenyutalkReferenceSecure<?>> getRefers() {
		return refers;
	}

	public UserScope getScope() {
		return scope;
	}

	public byte[] getSign() {
		return sign;
	}

	/**
	 * @return	読み取り署名名目
	 */
	public String getSignNominalRead() {
		return "Read" + " " + getName() + " "
				+ CreativeObject.class.getSimpleName();
	}

	/**
	 * @return	安全署名名目
	 */
	public String getSignNominalSafe() {
		return "Safe" + " " + getName() + " "
				+ CreativeObject.class.getSimpleName();
	}

	/**
	 * このメソッドが抽象クラスで実装された場合、
	 * 子クラスは署名において依存させたい情報がある場合オーバーライドする。
	 *
	 * thisをシリアライズして返値とするようなことをすると、
	 * 署名対象として本質的ではない情報に依存してしまい、
	 * あらゆる更新で署名が検証不可となるので注意。
	 *
	 * このメソッドの実装はファイルDL処理等をしてもいい。
	 *
	 * @return	電子署名の対象となるデータ
	 */
	public byte[] getSignTarget() {
		Long uploaderUserId = getUploaderUserId();
		String name = getName();
		byte[] nameB = name.getBytes(Glb.getConst().getCharsetNio());
		Long id = getId();

		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 4 + nameB.length);
		buffer.putLong(uploaderUserId);
		buffer.putLong(id);
		buffer.put(nameB);
		byte[] signTarget = Glb.getUtil().hashSecure(buffer.array());
		return signTarget;
	}

	abstract public CreativeObjectStore<? extends IndividualityObjectDBI,
			? extends IndividualityObjectDBI> getStore(Transaction txn);

	/**
	 * TODO もっと上のレベル
	 * @param txn
	 * @return	自分のためだけの情報を記録するコンテンツのユーザー用ストア
	 */
	public CookieStore getStoreCookie(Transaction txn) {
		return new CookieStore(getConcreteClassName(), txn);
	}

	public Long getUploaderUserId() {
		return uploaderUserId;
	}

	public GeneralVersioning getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + activePublication;
		result = prime * result + ((chat == null) ? 0 : chat.hashCode());
		result = prime * result + ((firstId == null) ? 0 : firstId.hashCode());
		result = prime * result + (publicationTimestamp ? 1231 : 1237);
		result = prime * result
				+ ((readSigns == null) ? 0 : readSigns.hashCode());
		result = prime * result
				+ ((referreds == null) ? 0 : referreds.hashCode());
		result = prime * result + ((refers == null) ? 0 : refers.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + Arrays.hashCode(sign);
		result = prime * result
				+ ((uploaderUserId == null) ? 0 : uploaderUserId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	public boolean isPublicationTimestamp() {
		return publicationTimestamp;
	}

	/**
	 * このオブジェクトのユーザーが自分のためだけに何か情報をセーブする場合に使用する。
	 * TODO もっと上の抽象クラスへ移動
	 * @return	セーブに成功したか
	 */
	boolean saveCookie(String key, Cookie data) {
		return getDbCookie().computeInTransaction(txn -> {
			try {
				CookieStore s = getStoreCookie(txn);
				Cookie old = s.get(key);
				if (old == null) {
					return s.create(key, data);
				} else {
					return s.update(key, data);
				}
			} catch (Exception e) {
				Glb.getLogger().error("Failed to saveCookie " + data, e);
				return false;
			}
		});
	}

	/**
	 * @return	シリアライズされたデータ
	 * @throws Exception
	 */
	public byte[] serialize() throws Exception {
		return Glb.getUtil().toKryoBytes(this, Glb.getKryoForTenyutalk());
	}

	public void setActivePublication(int activePublication) {
		this.activePublication = activePublication;
	}

	public void setFirstId(Long firstId) {
		this.firstId = firstId;
	}

	public void setPublicationTimestamp(boolean publicationTimestamp) {
		this.publicationTimestamp = publicationTimestamp;
	}

	public void setReferreds(List<TenyutalkReferenceSecure<?>> referreds) {
		this.referreds = referreds;
	}

	public void setRefers(List<TenyutalkReferenceSecure<?>> refers) {
		this.refers = refers;
	}

	public void setScope(UserScope scope) {
		this.scope = scope;
	}

	public void setSign(byte[] sign) {
		this.sign = sign;
	}

	public void setUploaderUserId(Long uploaderUserId) {
		this.uploaderUserId = uploaderUserId;
	}

	public void setVersion(GeneralVersioning version) {
		this.version = version;
	}

	/**
	 * コンテンツ読み取り時に署名する
	 * @return	署名に成功したか
	 */
	public boolean signRead() {
		return Glb.getUtil().sign(sign -> addReadSign(sign),
				() -> getSignNominalRead(), () -> getSignTarget());
	}

	private boolean validateAtCommonIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (version == null) {
			r.add(Lang.VERSION_LEVEL, Lang.ERROR_EMPTY);
			b = false;
		}
		if (referreds == null) {
			r.add(Lang.VERSIONED_REFERREDS, Lang.ERROR_EMPTY);
			b = false;
		}
		if (refers == null) {
			r.add(Lang.VERSIONED_REFERS, Lang.ERROR_EMPTY);
			b = false;
		}
		if (activePublication < activePublicationMin) {
			r.add(Lang.VERSIONED_ACTIVEPUBLICATION, Lang.ERROR_TOO_FEW);
			b = false;
		} else if (activePublication > activePublicationMax) {
			r.add(Lang.VERSIONED_ACTIVEPUBLICATION, Lang.ERROR_TOO_BIG);
			b = false;
		}
		if (uploaderUserId == null) {
			r.add(Lang.VERSIONED_UPLOADER_USERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandard(uploaderUserId)) {
				r.add(Lang.VERSIONED_UPLOADER_USERID, Lang.ERROR_INVALID,
						"uploaderUserId=" + uploaderUserId);
				b = false;
			}
		}
		if (readSigns == null) {
			r.add(Lang.VERSIONED_READ_SIGNS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (readSigns.size() > readSignsMax) {
				r.add(Lang.VERSIONED_READ_SIGNS, Lang.ERROR_TOO_MANY);
				b = false;
			}
		}

		//nameのnullチェックは別のクラスで行われている
		if (name != null && name.contains(Glb.getConst().getFileSeparator())) {
			r.add(Lang.VERSIONED_NAME, Lang.ERROR_INVALID, "name=" + name);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateAtCommonIndividualityObjectConcrete(r)) {
			if (!version.validateAtCreate(r)) {
				b = false;
			}
			for (TenyutalkReferenceSecure<?> e : referreds) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
			for (TenyutalkReferenceSecure<?> e : refers) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
			if (!scope.validateAtCreate(r)) {
				b = false;
			}
		} else {
			b = false;
		}
		if (!validateAtCreateCreativeObjectConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtCreateCreativeObjectConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;

		if (!(old instanceof CreativeObject)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		CreativeObject old2 = (CreativeObject) old;

		if (uploaderUserId.equals(old2.getUploaderUserId())) {
			r.add(Lang.VERSIONED_UPLOADER_USERID, Lang.ERROR_UNALTERABLE,
					"uploaderUserId=" + uploaderUserId + " uploaderUserId2="
							+ old2.getUploaderUserId());
			b = false;
		}

		if (!validateAtUpdateChangeCreativeObjectConcrete(r, old)) {
			b = false;
		}
		return b;
	}

	protected abstract boolean validateAtUpdateChangeCreativeObjectConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateAtCommonIndividualityObjectConcrete(r)) {
			if (!version.validateAtUpdate(r)) {
				b = false;
			}
			for (TenyutalkReferenceSecure<?> e : referreds) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
			for (TenyutalkReferenceSecure<?> e : refers) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
			if (!scope.validateAtUpdate(r)) {
				b = false;
			}
		} else {
			b = false;
		}
		if (!validateAtUpdateCreativeObjectConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtUpdateCreativeObjectConcrete(
			ValidationResult r);

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (!version.validateReference(r, txn)) {
			b = false;
		}
		for (TenyutalkReferenceSecure<?> e : referreds) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		for (TenyutalkReferenceSecure<?> e : refers) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		UserStore us = new UserStore(txn);
		if (us.get(uploaderUserId) == null) {
			r.add(Lang.VERSIONED_UPLOADER_USERID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"uploaderUserId=" + uploaderUserId);
			b = false;
		}
		for (NominalSignature sign : readSigns) {
			if (!sign.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		if (!scope.validateReference(r, txn)) {
			b = false;
		}
		if (!validateReference(r, txn))
			b = false;
		return b;
	}

	public abstract boolean validateReferenceCreativeObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public String toString() {
		return "Versioned [chat=" + chat + ", readSigns=" + readSigns
				+ ", version=" + version + ", referreds=" + referreds
				+ ", refers=" + refers + ", activePublication="
				+ activePublication + ", publicationTimestamp="
				+ publicationTimestamp + ", uploaderUserId=" + uploaderUserId
				+ ", scope=" + scope + ", sign=" + Arrays.toString(sign)
				+ ", firstId=" + firstId + "]";
	}

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public List<NominalSignature> getReadSigns() {
		return readSigns;
	}

	public void setReadSigns(List<NominalSignature> readSigns) {
		this.readSigns = readSigns;
	}

	public static int getReadsignsmax() {
		return readSignsMax;
	}

}

package bei7473p5254d69jcuat.tenyu.communication.request.gui.right;

import bei7473p5254d69jcuat.tenyu.communication.*;
import jetbrains.exodus.env.*;

/**
 * P2Pネットワークにファイルを登録する。
 * 必ずFileProvementが先行する。
 * ファイルの性質は、ほぼ静的で、比較的サイズが大きく、著作物の主な媒体など。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class FileRegistration extends UserRightRequest {
	/**
	 * ファイルのハッシュ
	 */
	private byte[] hash;

	/**
	 * クリエイターのユーザーID
	 * クリエイターが直接ファイル登録しなければならない。
	 */
	private long userId;
	/**
	 * 全近傍、サブネットワーク
	 */
	private String domain;

	/**
	 * categoryA/categoryB/categoryC
	 * フォルダ構造のように
	 */
	private String category;
	/**
	 * ファイル名に相当
	 */
	private String name;

	/**
	 * ファイルサイズ
	 */
	private long size;

	/**
	 * 最終更新日時
	 */
	private long lastUpdate;

	/**
	 * 登録日時
	 */
	private long createDate;

	/**
	 * 0=統一値 1=分担値
	 */
	private int type;

	/**
	 * ホワイトリストかブラックリストか。
	 * FileRestrictionでリストを設定できる。
	 */
	private boolean whitelist = false;

	@Override
	protected final boolean validateUserRightConcrete(Message m) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public long getCreateHistoryIndex() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}


	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}

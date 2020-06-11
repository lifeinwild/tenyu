package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;

public abstract class TenyutalkRepository implements TenyutalkRepositoryI {
	/**
	 * 最大キャッシュノード数
	 */
	public static final int cacheMax = 1000 * 10;

	/**
	 * このファイルをミラーしているノード
	 * このオブジェクトのハッシュ値を求める場合これを除外する必要がある。
	 */
	private List<NodeIdentifierUser> mirrorNodes = Collections
			.synchronizedList(new ArrayList<>());

	public List<NodeIdentifierUser> getMirrorNodes() {
		return mirrorNodes;
	}

	public void setMirrorNodes(List<NodeIdentifierUser> mirrorNodes) {
		this.mirrorNodes = mirrorNodes;
	}

}

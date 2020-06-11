package bei7473p5254d69jcuat.tenyu.communication.packaging;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import glb.*;

/**
 * Packageは梱包で、メッセージを梱包する。梱包を梱包する多重梱包の場合もある。
 *
 * 梱包の役割は暗号化や認証である。
 * 暗号化は、まだ相手の公開鍵も共通鍵も知らない場合できないし、
 * ネットワークに拡散させるメッセージは共通鍵による暗号化はできないし、
 * 過剰に公開鍵で暗号化すると性能の問題があるし、そもそも公開鍵による暗号化はサイズ制限がある。
 *
 * という事でメッセージの種類に応じて使用する梱包を選択している。
 * 現状、同じ種類のメッセージは同じ種類の梱包を使用する。恐らく今後もそれで足りる。
 * つまりメッセージのインスタンス毎に梱包を選択する必要は生じていない。
 *
 * 梱包は最終的にMessegeオブジェクトのフィールドに設定され、
 * Messageオブジェクトがシリアライズされて送信される。
 * Messageは通信に使われたP2PEdgeを特定する機能や、
 * 送信者のユーザーIDを取得する機能を持つ。
 *
 * 種類				暗号		公開鍵特定情報			パッケージ名
 * 認識				無し		公開鍵そのもの			PlainPackage
 *  	※RSA梱包は廃止		共通鍵交換		公開鍵		エッジID				RSAPackage
 * ユーザー登録		同
 * 鍵交換			同
 * 分散合意系		共通鍵		P2PEdgeID				CommonKeyPackage
 * リクエスト系		同
 * 拡散系			無し、署名	UserId					UserPackage
 *
 * P2PEdgeは有向グラフのエッジで、A→BとB→Aは異なるエッジ。
 * P2PEdgeにおいて相手から自分へのエッジのIDも記録される。
 *
 * 認証のプロセスについて。
 * 認証は他のノードについて何も知らない状態から
 * IPアドレスやポートや公開鍵などの基本情報を知る過程。
 * まず何らかの方法で１つIPアドレスとポートを知る。方法はGUIからの入力等。
 * そして認証→共通鍵交換→共通鍵通信の確認
 * それ以降、近傍リストに加えている事を通知するため、挨拶を定期的に交換する。
 * もし自分の近傍リストに入っていないノードから挨拶が来たら、
 * 返信にその情報を記述し、相手はbidirectionalをfalseにし、挨拶を停止する。
 * 挨拶によってIPアドレスの変化に対応する。
 *
 * PlainPackageは平文で、それで送っていい情報は公開鍵等に限られる。
 *
 * ※RSA梱包は廃止。RSAPackageは相手の公開鍵で暗号化され、セキュアな情報の交換が可能だがサイズ制限があり
 * 事実上共通鍵交換のためにある。
 *
 * CommonKeyPackageは共通鍵で暗号化され、高速かつセキュアかつ大容量可能。
 * UserPackageは暗号化されず、しかし署名がついていて誰のメッセージか証明できる、つまり認証可能。
 *
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class Package extends Communicatable {
	/**
	 * 多重梱包の限界
	 */
	private static final int packMax = 10;

	/**
	 * シリアライズや暗号化などがされた内容
	 * 具象クラスによってシリアライズ方法が異なる
	 */
	protected byte[] contentBinary;

	/**
	 * 一度deserializedされたデータがキャッシュされる。
	 */
	protected transient Communicatable deserialized;

	public void clearCache() {
		deserialized = null;
	}

	/**
	 * 梱包に内容（シリアライズされたbyte[]）をセットする。
	 *
	 * isValidTypeを呼び出すコードを一元化
	 *
	 * @param content	これをシリアライズや暗号化して内容に設定する。
	 *
	 * ここでcontentの型が{@link Communicatable}になっている事は、
	 * {@link MessageContent}だけではなく{@link Package}も含まれる事、
	 * 即ち梱包の内容として梱包が入れられ、その内容として梱包が入れられる
	 * という入れ子になりうる。ただし最も内側は{@link MessageContent}になる。
	 */
	public boolean serializeAndSetContent(Communicatable content, Message m) {
		//梱包は組み合わせを問わない
		//内容のinterfaceにおいて梱包を指定しているのは
		//最も内側の梱包がgetEdgeやgetUserIdで意味を持つから、もう一つは
		//セキュリティ等を考えた場合に１つ梱包を指定しなければならないからであり、
		//梱包は他の梱包との組み合わせを限定しない
		if (!(content instanceof Package) && !isValidType(content))
			return false;
		if (!serializeAndSetContentConcrete(content, m)) {
			return false;
		}
		//成功した場合キャッシュする
		deserialized = content;
		return true;
	}

	/**
	 * binarizeAndSetContentから呼び出される具象クラス側の実装。
	 *
	 * @param content
	 * @param detector
	 * @return
	 */
	protected abstract boolean serializeAndSetContentConcrete(
			Communicatable content, Message m);

	/**
	 * 内容を返す
	 * isValidTypeを呼び出すコードを一元化
	 * @return デシリアライズされた内容
	 */
	public Communicatable deserialize(Message m) {
		//キャッシュがあればキャッシュを返す。
		if (deserialized == null) {
			Communicatable tmp = deserializeConcrete(m);
			if (tmp != null && isValidType(tmp) && tmp.validate(m)) {
				deserialized = tmp;
			} else {
				Glb.getLogger().error(
						"tmp=" + tmp + " " + tmp == null ? "" : tmp.getClass(),
						new Exception());
			}
		}
		return deserialized;
	}

	/**
	 * 内容をデシリアライズして返す。
	 * 梱包の具象クラスは、deserializeの実装において、
	 * 適切なクラスではないオブジェクトが内容に入っていた場合、nullを返すべき。
	 * 適切なクラスとは例えばUserPackageContent等の空インターフェース。
	 *
	 * @return	PackageもContentもありうる。
	 */
	protected abstract Communicatable deserializeConcrete(Message m);

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Package))
			return false;
		Package p = (Package) obj;

		return Arrays.equals(contentBinary, p.getContentBinary());
	}

	/**
	 * @return シリアライズや暗号化等がされた内容
	 */
	public byte[] getContentBinary() {
		return contentBinary;
	}

	/**
	 * 型チェック
	 * @param content
	 * @return			妥当な型か
	 */
	protected abstract boolean isValidType(Object content);

	/**
	 * 多重梱包の内容と全梱包を取得する。
	 * thisの梱包を解くが、内部にさらに梱包があった場合さらに解く。
	 * 全ての梱包及び内容についてvalidate()が行われる。単品の検証。
	 *
	 * 検証処理を呼び出す流れはここに一元化されている。
	 *
	 * @param packs		this及び解除された全ての梱包
	 * @return		内容または内容が見つからなければnull
	 */
	public MessageContent unpackage(Message m) {
		//thisの検証
		if (!validateConcrete(m)) {
			Glb.debug(() -> "invalid received message: "
					+ this.getClass().getSimpleName());
			return null;
		}

		//直前の梱包 まず一番外側の梱包を設定
		Package pack = this;
		m.addInner(pack);
		//直前の梱包から取り出されたもの
		Communicatable c = null;
		for (int i = 0; i < packMax; i++) {
			//内容または梱包を取り出す
			c = pack.deserialize(m);
			//異常データまたは内容なら終了
			if (c == null || !(c instanceof Package)) {
				break;
			}
			//直前の梱包を更新
			pack = (Package) c;
			//梱包一覧に追加
			m.addInner(pack);
		}
		//内容なら内容を返す
		if (c instanceof MessageContent) {
			MessageContent content = (MessageContent) c;
			m.setContent(content);
			return content;
		}
		//そうでなく異常データならnullを返す
		return null;
	}

	@Override
	protected final boolean validateConcrete(Message m) {
		return contentBinary != null && contentBinary.length > 0
				&& validatePackageConcrete(m);
	}

	protected abstract boolean validatePackageConcrete(Message m);
}

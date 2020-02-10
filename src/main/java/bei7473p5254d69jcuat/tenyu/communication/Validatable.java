package bei7473p5254d69jcuat.tenyu.communication;

/**
 * 没案。現在これら検証メソッドはクラス設計の中に実装されている。
 * TODO：削除
 *
 * 各メッセージは必要な検証メソッドのみオーバーライドする。
 * メッセージ受信時に全検証メソッドが実行され全てtrueの場合のみ反映される。
 * @author exceptiontenyu@gmail.com
 *
 */
public interface Validatable {
/*
	public final boolean validate() {
		if (isValidSignature()
		  && isNotDuplication()
		  && isProvedNeighbor()
		  && isByAdmin()
		  && isProvedNeighbor()
		  && isUser()
		  && isApplicant()
		  && isEnoughFlow()
		  && isNotStorageAttack()
		  && isValidVersion()
		  && isValidEtc())
			return true;
		return false;
	}
*/
	/**
	 * validationの結果に応じて拡散するか決定する。
	 * メッセージによってvalid=falseでも拡散すべき場合がある。
	 * validはほかの全検証メソッドがtrueの場合のみtrue
	 *
	 * valid=falseなら常にinvalidとみなすことにした。
	 * そうでないメッセージを扱わない。
	 * これによってinvalidなメッセージを送ってくるノードを主観的にBANできる。
	 */
//	public boolean isSpread(boolean valid);

	/**
	 * そのメッセージに書かれた領域の管理者の公開鍵か
	 * @return
	 */
	default public boolean isValidSender() {
		return true;
	}

	/**
	 * CPU証明されているか
	 * @return
	 */
	default public boolean isProvedNeighbor() {
		return true;
	}

	/**
	 * Userか。登録されていない、登録されていてもapplicantならfalse
	 */
	default public boolean isUser() {
		return true;
	}
	public boolean isByAdmin();

	/**
	 * applicantならtrue
	 */
	default public boolean isApplicant() {
		return true;
	}

	/**
	 * 基盤からそのメッセージ作者への最低フロー値
	 * @return
	 */
	default public boolean isEnoughFlow() {
		return true;
	}

	/**
	 * 署名、公開鍵、署名対象データは整合性があるか
	 * @return
	 */
	default public boolean isValidSignature() {
		return true;
	}

	/**
	 * 1日あたりの送信容量制限以内か
	 */
	default public boolean isNotStorageAttack() {
		return true;
	}

	/**
	 * 現在の基盤ソフトウェアとメッセージのバージョンは一致するか
	 */
	default public boolean isValidVersion() {
		return true;
	}

	/**
	 * 既に受け取ったメッセージと同じか
	 */
	default public boolean isNotDuplication() {
		return true;
	}

	/**
	 * クラス固有の検証
	 */
	default public boolean isValidEtc() {
		return true;
	}

}

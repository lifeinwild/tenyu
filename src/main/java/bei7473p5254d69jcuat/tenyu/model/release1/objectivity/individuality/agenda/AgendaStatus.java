package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda;

/**
 * メモ：ALL_VOTEDという全員投票済みという終了状態を作ろうと一瞬考えたがやめた。
 * 投票期間中に全体運営者が増減する可能性を考慮する必要があるから。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public enum AgendaStatus {
	/**
	 * 開始直後の状態
	 */
	START(0),
	/**
	 * 期日を過ぎて終了し、可決されなかった。
	 */
	DENIED(1),
	/**
	 * 期日を過ぎて終了し、可決された。
	 * 可決はいずれかの投票値がその議題に設定された閾値を超える事。
	 */
	ACCEPTED(2),
	/**
	 * 執行済み
	 */
	PROCESSED(3),
	/**
	 * 何らかの異常終了
	 */
	INVALID_END(99);
	/**
	 * enumの定義順序に依存しない識別子
	 */
	private int num = 0;

	private AgendaStatus() {
	}

	private AgendaStatus(int num) {
		this.num = num;
	}

	public int getNum() {
		return num;
	}
}
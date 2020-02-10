package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

/**
 * エッジの種類
 * @author exceptiontenyu@gmail.com
 *
 */
public enum EdgeType {
	//as service
	/**
	 * 運営貢献
	 */
	ADMINISTRATOR,
	/**
	 * 問題報告。脆弱性報告やそのほか仕様上の問題等を報告した人等に作成される。
	 */
	PROBLEM_REPORT,
	//as in environment
	/**
	 * 基本的に共同主体があらゆるノードを代表して
	 * JVMやIPといった基礎的技術に作る
	 * その他環境を提供している存在へのエッジ
	 */
	ENVIRONMENT,
	//as animal
	/**
	 * ファン。各個人の判断で作成される
	 */
	FAN,
	/**
	 * 友人。各個人の判断で作成される
	 */
	FRIEND,
	/**
	 * 学習。講座サイト等に作成される
	 */
	LEARNING,
	/**
	 * その他
	 */
	OTHER,
	//as software
	/**
	 * 部品貢献。素材やライブラリ提供者
	 */
	PART_PROVIDER,
	/**
	 * 参加貢献。特別な貢献をしたプレイヤーや有名で宣伝効果があったプレイヤー等に作成。
	 * 参加者全体に作成することは性能的に困難なので出来ない。
	 */
	PARTICIPANT,
	//as human
	/**
	 * 尊敬。各個人の判断で作成される
	 */
	RESPECT,
	/**
	 * 制作貢献
	 */
	TOTAL_DESIGNER,
	/**
	 * 内部的な処理などでの何らかの経緯でエッジの種類が不明な場合
	 */
	UNKNOWN,
}
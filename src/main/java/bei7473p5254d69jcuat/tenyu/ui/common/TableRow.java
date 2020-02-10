package bei7473p5254d69jcuat.tenyu.ui.common;

/**
 * GUI表示はたびたび複数のモデルクラスにまたがるデータをまとめて表示する事があるが、
 * TableItemは1クラスでそれを実現する必要があり、
 * それを考慮するとモデルクラスに沿って抽象化するのは妥当ではない。
 * 一方で、～Gui系のクラスなどタブ上にモデルデータを表示するGUI部品は、
 * 複数のクラスにまたがる場合単に2つのクラスを用いて表示すればいいので、
 * 抽象化が有効である可能性が高い。
 * さらにTableItemはモデルのほんの一部のデータしか表示しない事が多い。
 * そこで、抽象化はせず、各具象クラス毎に表示するデータのためだけのメンバーを定義する。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <Src>
 */
public interface TableRow<Src> {
	/**
	 * @return	メインのモデルデータ
	 */
	Src getSrc();

	/**
	 * srcに基づいて表示を更新する
	 * 親クラスも更新
	 */
	void update();
}
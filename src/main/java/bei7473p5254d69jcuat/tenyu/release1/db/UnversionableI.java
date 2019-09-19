package bei7473p5254d69jcuat.tenyu.release1.db;

/**
 * バージョンアップ不可能であることを意味する空インターフェース。
 * このインターフェースがつけられていても
 * その意味は子クラスや親クラスに及ばない。
 * つけられているクラスだけがバージョンアップ不可能。
 *
 * 実際、このインターフェースがつけられていても
 * メソッドを修正する程度なら問題無い場合がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface UnversionableI {

}

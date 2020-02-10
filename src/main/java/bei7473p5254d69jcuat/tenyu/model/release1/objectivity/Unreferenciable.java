package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

/**
 * このインターフェースがつけられたクラスのインスタンスは
 * 頻繁に削除されるなどの事情があり、
 * そのIDを他のオブジェクトから参照する場合注意が必要。
 *
 * {@link Unreferenciable}から{@link Unreferenciable}を参照することは
 * 可能な場合がある。必ずAがBより先に削除される場合、AがBを参照しても問題無い。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface Unreferenciable {

}
